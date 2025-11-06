generic-clustering-service (gcs)

At a high level

A tool for creating a common metadata aggregations from the widest possible array of sources to create an open and shared resource that provides
shared identifiers for works and instances. Open data licensing is baked into the schemas to ensure that data provided can be reused and that
ownership stays within the library community. The tool is designed for the wide GLAM sector -not just for libraries (Although that is our initial use case).

Our aim is to solve the problem of clustering without creating yet another walled garden subscription service. We want to provide stable shared instance
and work identifiers can can be used under an open data license. We want to provide these primarily as bibframe (And other semantic web) resources.

As well as providing records, we will provide feaures that allows users to upload records (With a guarantee that we will not keep the data unless explicitly
allowed to do so) and have a match to our internal systems. Bulk matching may have to be pay for as the record matching can be expensive.

Technical Detail

gcs is a micronaut 4 application built for JDK 21 targeting virtual threads rather than reactive paradims. It makes extensive use of modern LLM 
technology to address the long standing problem of fuzzy matching in bibliographic data.

Project: Generic Clustering Service (GCS)
Runtime: Java 21, Micronaut 4.10+, Gradle (Kotlin DSL).
Use JDBC implementation of MicronautData - do not use JPA

Hard constraints:

No network calls, no external AI APIs, no containers, no Docker, no Testcontainers.

No Elasticsearch / OpenSearch / pgvector IN CORE for TESTS - These may be mocked for alternate implementations however.

All code must run locally and deterministically via pure JVM.

Prefer zero extra dependencies; if unavoidable, explain and keep to small, pure-Java libs.

Architecture rule: Ports & Adapters. Every external capability (embeddings, vector index, storage) is a port with at least one in-memory adapter.

DO not remove app/src/test/resources/application-test.yml it is necessary to run the integration tests - just because Jules can't run them this file must not be deleted.

ENSURE that for the APP module postgres is maintained as the source of truth / system of record. Objects in PG can be replicated to ES for fast and efficient vector match but ES must be

ENSURE that Postgres remains the "System of record" for all data

ENSURE that ElasticSearch is used as the core matching engine for vector opertations (Follwing a CQRS pattern where data is storred in Postgres, but vector ops are delegated to ES)

ENSURE that DefaultIngestServiceTest remains the primary end to end integration test that calls the full stack with no mocking. Do not mock components in this test.

fully re-creatable from the postgres system of record in a DR scenario.

ENSURE that any modifications to domain classes are also reflected in the database migrations scripts.

Deliverables per task:

Code (compilable).

DO NOT INCLUDE -PrunIT in ANY jules workflows - it invokes testcontainers and will not work.

FULL run of test suite (Excluding runIT) from top level - no module only tests - core modules must both pass. DO not run app tests unless you support testcontainers. app must BUILD. rely on github actions feedback for app test results.

Short README snippet or Javadoc for each public type. (Do not remove existing content from README)

Minimal test that runs in ~1s, no network, deterministic.

A runnable main or Micronaut controller to demo the feature.

Data model (minimum):

InputRecord (your canonical JSON).

WorkProfile, InstanceProfile.

Required ports (interfaces):

EmbeddingService { float[] embed(String text); int dim(); }

VectorIndex<T> { void add(String id, float[] vec, T payload); List<Neighbor<T>> topK(float[] query, int k); List<T> radius(float[] query, float threshold); }

Clusterer<T> { List<Cluster<T>> cluster(List<Item<T>> items, double threshold); }

Canonicalizer { String summarize(InputRecord r); }

Calibration { double scoreToProb(double s); void update(double s, boolean isDup); Thresholds thresholds(); }

Mandatory in-memory adapters:

HashingEmbeddingService (deterministic, no network).

InMemoryVectorIndex (brute-force cosine; optional HNSW later, still in-JVM).

SimpleThresholdClusterer (connected-components by similarity ≥ τ).

PlattCalibration (logistic fit from (score, label) stream; starts with sane defaults).

JsonStore (persist JSONL to ./data, no DB).

Determinism: use a fixed seed RNG.
Quality gates: cosine similarity unit tests with golden vectors; round-trip JSON tests.
HTTP API (Micronaut):

POST /ingest InputRecord → stores record, computes embedding, returns candidate duplicates (top-k and above τ).

POST /feedback {candidateId, isDup} → updates calibration.

GET /cluster/{workId} → returns cluster members.

GET /health → returns {"ok":true}.

Non-goals: no UI, no persistence beyond local files for this phase.

Output format expectations: tabs for indentation preferred; if spaces are required, use 2 spaces.

## Future Work

- **Re-processing Records:** The `classifierVersion` has been added to the `InputRecord` to support future work on re-processing records that have been classified by an older version of the `RuleBasedClassifier`.
