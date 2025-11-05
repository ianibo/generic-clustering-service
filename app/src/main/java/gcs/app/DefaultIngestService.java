package gcs.app;

import gcs.app.adapters.PgMemberAdapter;
import gcs.app.clustering.BlockingRandomProjector;
import gcs.app.clustering.CentroidService;
import gcs.app.esvector.ESIndexStore;
import gcs.app.pgvector.InstanceCluster;
import gcs.core.IngestService;
import gcs.app.pgvector.InstanceClusterMember;
import gcs.app.pgvector.WorkCluster;
import gcs.app.pgvector.WorkClusterMember;
import gcs.app.pgvector.storage.InstanceClusterMemberRepository;
import gcs.app.pgvector.storage.WorkClusterMemberRepository;
import gcs.core.EmbeddingService;
import gcs.core.InputRecord;
import gcs.core.assignment.AnchorPort;
import gcs.core.assignment.Assignment;
import gcs.core.assignment.AssignmentService;
import gcs.core.canonicalization.Canonicalizer;
import gcs.core.classification.Classifier;
import gcs.core.synthesis.Synthesizer;
import jakarta.inject.Singleton;
import jakarta.inject.Named;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * The primary service for processing and clustering incoming records.
 * This service acts as the main entry point for the ingestion pipeline.
 *
 * <h2>Orchestration Roadmap:</h2>
 * <ol>
 *   <li><b>Classification:</b> The incoming {@link InputRecord} is first passed to the {@link Classifier} to determine its {@code WorkType} and {@code InstanceClassification}.</li>
 *   <li><b>Assignment:</b> The classified record is then sent to the {@link AssignmentService} for both "work" and "instance" representations. The {@code AssignmentService} decides whether the record should join an existing cluster or create a new one.</li>
 *   <li><b>Handling the Assignment:</b>
 *     <ul>
 *       <li>If a record **joins** an existing cluster, its membership is persisted, the cluster's synthetic anchor is re-calculated using the {@link Synthesizer}, and the updated anchor is saved via the {@link AnchorPort} and upserted to Elasticsearch.</li>
 *       <li>If a record **creates** a new cluster, the new cluster and its first member are persisted, and the new anchor is indexed in Elasticsearch.</li>
 *     </ul>
 *   </li>
 *   <li><b>Result:</b> The service returns the final, versioned {@code InputRecord}.</li>
 * </ol>
 */
@Slf4j
@Singleton
public class DefaultIngestService implements IngestService {

    private final Classifier classifier;
    private final AssignmentService assignmentService;
    private final PgMemberAdapter memberAdapter;
    private final Synthesizer synthesizer;
    private final AnchorPort anchorPort;
    private final ESIndexStore esIndexStore;
    private final WorkClusterMemberRepository workClusterMemberRepository;
    private final InstanceClusterMemberRepository instanceClusterMemberRepository;
    private final EmbeddingService embeddingService;
    private final Map<String, Canonicalizer> canonicalizers;
    private final Canonicalizer defaultCanonicalizer;
    private final BlockingRandomProjector projector;
    private final CentroidService centroidService;

    public DefaultIngestService(
        Classifier classifier,
        AssignmentService assignmentService,
        PgMemberAdapter memberAdapter,
        Synthesizer synthesizer,
        AnchorPort anchorPort,
        ESIndexStore esIndexStore,
        WorkClusterMemberRepository workClusterMemberRepository,
        InstanceClusterMemberRepository instanceClusterMemberRepository,
        @Named("openai") EmbeddingService embeddingService,
        List<Canonicalizer> canonicalizerList,
        BlockingRandomProjector projector,
        CentroidService centroidService
    ) {
        this.classifier = classifier;
        this.assignmentService = assignmentService;
        this.memberAdapter = memberAdapter;
        this.synthesizer = synthesizer;
        this.anchorPort = anchorPort;
        this.esIndexStore = esIndexStore;
        this.workClusterMemberRepository = workClusterMemberRepository;
        this.instanceClusterMemberRepository = instanceClusterMemberRepository;
        this.embeddingService = embeddingService;
        this.canonicalizers = canonicalizerList.stream()
            .filter(c -> c.forContentType() != null)
            .collect(Collectors.toMap(Canonicalizer::forContentType, Function.identity()));
        this.defaultCanonicalizer = canonicalizerList.stream()
            .filter(c -> c.forContentType() == null)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No default canonicalizer found"));
        this.projector = projector;
        this.centroidService = centroidService;
    }

    @Override
    @Transactional
    public InputRecord ingest(InputRecord record) {
        var classification = classifier.classify(record);
        log.info("Classified record {} as {} with explanation: {}", record.id(), classification.workType(), classification);

        var versionedRecord = new InputRecord(
            record.id(),
            record.provenance(),
            record.domain(),
            record.licenseDeclaration(),
            record.identifiers(),
            record.titles(),
            record.contributors(),
            record.languages(),
            record.edition(),
            record.publication(),
            record.physical(),
            record.subjects(),
            record.series(),
            record.relations(),
            record.classification(),
            record.notes(),
            record.rights(),
            record.admin(),
            record.media(),
            record.ext(),
            classification.classifierVersion()
        );

        var workCanonicalizer = canonicalizers.getOrDefault(versionedRecord.physical().contentType(), defaultCanonicalizer);
        String workSummary = workCanonicalizer.summarize(versionedRecord, Canonicalizer.Intent.WORK);
        float[] workEmbedding = embeddingService.embed(workSummary);

        var instanceCanonicalizer = canonicalizers.getOrDefault(versionedRecord.physical().contentType(), defaultCanonicalizer);
        String instanceSummary = instanceCanonicalizer.summarize(versionedRecord, Canonicalizer.Intent.INSTANCE);
        float[] instanceEmbedding = embeddingService.embed(instanceSummary);

        handleAssignment(assignmentService.assign(versionedRecord, "work", workEmbedding), "work", versionedRecord, workEmbedding);
        handleAssignment(assignmentService.assign(versionedRecord, "instance", instanceEmbedding), "instance", versionedRecord, instanceEmbedding);

        return versionedRecord;
    }

    /**
     * Acts on the decision from the {@link AssignmentService}.
     *
     * @param assignment The assignment decision.
     * @param representation The representation type ("work" or "instance").
     * @param record The record that was processed.
     */
    private void handleAssignment(Assignment assignment, String representation, InputRecord record, float[] embedding) {
        if (assignment.getDecision() == Assignment.Decision.JOINED) {
            // If the record joined an existing cluster:
            // 1. Persist the new membership link.
            addMemberToCluster(assignment.getClusterId(), record, representation, embedding);
            // 2. Re-calculate the synthetic anchor with the new member.
            List<InputRecord> members = memberAdapter.getMembers(assignment.getClusterId());
            InputRecord newAnchor = synthesizer.synthesize(members);
            // 3. Update the anchor in the database, if synthesis was successful.
            if (newAnchor != null) {
                anchorPort.updateAnchor(assignment.getClusterId(), newAnchor);
                // 4. Upsert the updated anchor to Elasticsearch.
                upsertAnchorToEs(assignment.getClusterId(), newAnchor, representation);
            } else {
                log.warn("Synthesizer returned null for cluster {}, not updating anchor.", assignment.getClusterId());
            }
        } else {
            // If a new cluster was created:
            // 1. Persist the new membership link.
            addMemberToCluster(assignment.getClusterId(), record, representation, embedding);
            // 2. Upsert the new anchor to Elasticsearch.
            upsertAnchorToEs(assignment.getClusterId(), assignment.getClusterAnchor(), representation);
        }
    }

    private void addMemberToCluster(UUID clusterId, InputRecord record, String representation, float[] embedding) {
        if ("work".equals(representation)) {
            WorkClusterMember member = new WorkClusterMember();
            member.setId(UUID.randomUUID());
            member.setWorkCluster(WorkCluster.builder().id(clusterId).build());
            member.setRecordId(record.id());
            workClusterMemberRepository.save(member);
        } else {
            InstanceClusterMember member = new InstanceClusterMember();
            member.setId(UUID.randomUUID());
            member.setInstanceCluster(InstanceCluster.builder().id(clusterId).build());
            member.setRecordId(record.id());
            instanceClusterMemberRepository.save(member);
        }
        centroidService.updateCentroid(clusterId, representation, new com.pgvector.PGvector(embedding));
    }

    private void upsertAnchorToEs(UUID clusterId, InputRecord anchor, String representation) {
        String indexName = "anchors-" + representation;
        var canonicalizer = canonicalizers.getOrDefault(anchor.physical().contentType(), defaultCanonicalizer);
        String summary = canonicalizer.summarize(anchor, "work".equals(representation) ? Canonicalizer.Intent.WORK : Canonicalizer.Intent.INSTANCE);
        float[] embedding = embeddingService.embed(summary);
        float[] blockingEmbedding = projector.project(embedding);

        Map<String, Object> esRecord = new HashMap<>();
        esRecord.put("clusterId", clusterId.toString());
        esRecord.put("representation", representation);
        esRecord.put("embedding", embedding);
        esRecord.put("blocking", blockingEmbedding);
        try {
            esIndexStore.store(indexName, esRecord);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
