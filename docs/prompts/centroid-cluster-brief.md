The project current aims to cluster records by finding the KNN and then creating a new cluster if no close match is found, or by adopting the cluster record ID of the nearest match.

Refactor the code so that it uses a synthetic anchor in the form of a cluster record. As well as moving approach to synthetic anchors, we want to be able to present stable identifiers 
for cluster records. So it is necessary to track identifiers so split / deleted clusters can be resolved at a later date.

---

# Project brief — Synth-anchor clustering with ES vectors, Postgres SoR, stable public IDs & lineage

## Goals

* Online clustering driven by **synthetic anchor records** (“synths”) as centroids.
* **Postgres** remains **system of record** for records, clusters, synths, membership, scores, **and lineage**.
* **Elasticsearch** (ES 9.x) is used **only** for **vector candidate retrieval / kNN**.
* Publish **stable public Cluster IDs (CIDs)** and a **lineage resolver** so third parties can tag records permanently even as clusters split/merge.

## Hard constraints

* **Do not create new DDL or DTOs. unless absolutely necessary** Where possible reuse and extend the **existing migrations and DTO classes** from the project.
* **Module split is mandatory:**

  * **core/** — pure library code (deterministic scoring, fusion, policies, lineage graph ops, algorithms). Must compile and be fully unit-testable **without** PG/ES/LLMs.
  * **app/** — Micronaut 4.10+ runtime wiring and adapters to Postgres/Elasticsearch (repositories/clients/controllers). Jules must not introduce Testcontainers; keep integration points behind interfaces.

## Key concepts (unchanged where possible)

* **Anchor (cluster prototype):** a fused/synth record + provenance + stats + centroid embedding (stored in PG; mirrored to ES for search).
* **Scoring views:** `emb` (from ES cosine), `keys`, `ids`, `pubyr`, minus `conflict_penalty`.
* **Decision rule:** join best anchor if `total ≥ tauJoin` and `fieldAgreementOk`, else create new cluster.
* **Consolidation:** periodic split/merge; anchors re-indexed in ES.
* **in code documentation**:** where possible expand acronyms and make the code readable, add comments.

## Stability & public keys (new)

* **CID (Cluster ID):** opaque, permanent identifier minted at cluster creation (use existing DTO/ID strategy). **Never change or recycle.**
* **CFP (Content FingerPrint):** versioned hash of the *synth record* (canonical JSON + schema version); changes when synth changes. Use only for snapshot semantics; CID is the permalink.
* **Lineage edges:** append-only transitions for `merged-into`, `split-into`, `same-as` (use existing DTOs/tables if present; otherwise implement via the project’s lineage DTOs—no new schema in brief).
* **Resolver API (public):**

  * `GET /resolve/{cid}` → returns status (`current|merged|superseded`), `current[]` successor CIDs (0/1/N), current CFP, representation, minimal synth, and recent lineage edges.
  * Never 404 a known CID; always return lineage and “currentness”.

## Elasticsearch usage

* Indices: one **anchors** index per representation (`anchors-work`, `anchors-instance`). (Mapping bootstrap belongs in **app**. Reuse the existing code to create indexes and set types for specific fields, continue to allow dynamic schema for other props.)
* Anchor doc fields (suggested minimal mapping; align names with existing DTOs):

  * `clusterId` (keyword), `representation` (keyword),
  * `blocking` (blocking vector dim 64),
  * `embedding` (dense_vector dims=project dim, cosine, knn=true),
  * light text/keyword copies of keys (titleCore/primaryAuthor/publisherNorm/year/ids) for filters.
* Queries to implement (in **app**):

  * `nearestAnchors(embedding, representation, k, filters)` using ES `knn_search`.
  * `nearestAnchorsForAnchor(anchorCentroid, representation, k)` for merge candidates.

## Core/app responsibilities

### core/ (pure)

* **Policies:** `RepresentationPolicy` (Work vs Instance: field agreement, conflict penalty, pub/year scoring).
* **Scoring:** combine ES-provided cosine with local views; return `ScoreBreakdown`.
* **Fusion:** deterministic synth builder (majority vote + source weighting + provenance).
* **Algorithms:**

  * `AssignmentService` (assign or create) — *pure logic* that depends on two injected ports:

    * `AnchorPort` (read current anchor synth/stats by ID; update centroid/synth decisions as commands)
    * `CandidatePort` (returns top-K candidate anchors with ES cosine pre-score)
  * `ConsolidationService` (split/merge decisions) — uses injected ports:

    * `MemberPort` (list member embeddings/fields),
    * `AnchorPort`, `CandidatePort`
* **Lineage engine:** functions to propose lineage events (`merge(a,b→c)`, `split(x→y,z)`), compute resolver view (`resolve(cid)`) from a list of edges, and mark **current** heads. (No DB here.)

> All core services operate on DTOs already present in the project. If a DTO lacks a field the algorithm needs, expose it via adapter mapping in **app**.

### app/ (Micronaut runtime)

* **Adapters/ports:**

  * `PgAnchorAdapter` implements `AnchorPort` (load/save cluster, synth, centroid, stats, lineage edges) using existing repositories.
  * `PgMemberAdapter` implements `MemberPort` from existing membership repo.
  * `EsCandidateAdapter` implements `CandidatePort` (kNN to ES).
* **Index bootstrap:** create/verify ES anchor indices/mappings at startup (idempotent).
* **Pipelines/controllers:**

  * `POST /records/ingest` → normalize & persist record (existing DTO), ask `AssignmentService`, then:

    * if **joined**: persist membership, recompute centroid/synth in PG, **upsert** anchor to ES.
    * if **created**: mint **CID**, persist new cluster/synth, **upsert** anchor.
  * `POST /maintenance/consolidate` → run split/merge decisions; commit lineage; reindex anchors.
  * `GET /clusters/{cid}` → synth, stats, members (PG).
  * **NEW:** `GET /resolve/{cid}` → use app-level resolver that reads lineage edges from PG (system of record) and returns resolver DTO.
* **Reindex jobs:** backfill ES from PG; reconcile anchors flagged as “needs_reindex”.

**Merge**: pick survivor CID (policy: oldest), add lineage edges `{loser → merged-into → survivor}`, move members, recompute synth/centroid, upsert survivor, delete loser’s anchor doc.
**Split**: parent CID becomes non-current with edges `{parent → split-into → childA}`, `{parent → split-into → childB}`; persist two new child clusters (new CIDs), index both anchors.

## Resolver behavior

* `resolve(cid)` computes:

  * `status`: `current` if node has no outgoing `merged-into`/`split-into`; otherwise `merged` or `superseded`.
  * `current[]`: walk forward along lineage to current heads (may be 0, 1, or many after split).
  * include `representation`, latest `cfp`, minimal `synth` fields for UX preview.
* Always return a resolver DTO for any known `cid` (never a 404 for lineage nodes).

## Config (YAML, app)

* Keep existing config style; add only what’s needed:

  * ES hosts + index names, k (top-K).
  * Per-representation weights/thresholds.
  * Flags for split/merge checks.
* No schema in brief—use existing migration files.

## Testing

* **core/**: 100% unit-testable (no PG/ES). Cover scoring math, fusion/provenance, join/create decisions, split/merge decisions, resolver outcomes for merge/split chains.
* **app/**: lightweight tests that mock ports where possible. No Testcontainers. Provide manual profile docs for running against dev ES/PG if needed (outside Jules scope).

## Minimal ES mapping (app bootstrap; align names to DTOs)

* One anchors index per representation.
* `dense_vector` for `embedding` (dims = project config; cosine; knn enabled).
* Keyword fields for `clusterId`, `identifiers[]`; optional text/keyword for title/author filters.

## Checklist for Jules

1. **Use existing DTOs & repositories**; do not create new migrations unless creating new entities.
2. Implement **core** services/interfaces: `RepresentationPolicy`, `Scorer`, `Synthesizer`, `AssignmentService`, `ConsolidationService`, **LineageResolver**.
3. Implement **ports** in **app**: `AnchorPort`, `MemberPort`, `CandidatePort` wired to PG/ES using existing repos/clients.
4. Add **/resolve/{cid}** controller + service that reads lineage from PG and returns resolver DTO.
5. Add ES bootstrap for **anchors** indices; implement `nearestAnchors(...)`, `upsertAnchor(...)`, `deleteAnchor(...)`.
6. Ensure join/create paths:

   * PG writes (membership, synth refresh, stats) → ES upsert anchor
   * On merge/split, write lineage edges and keep old CIDs resolvable.
7. Provide unit tests in **core** for:
   * tie cases (below/above `tauJoin`)
   * Work/Instance conflict penalties
   * merge and split lineage; `resolve()` results for: current, merged, split-into-two.
8. Respect formatting: **tabs preferred**; if not possible, **2 spaces per tab**.

