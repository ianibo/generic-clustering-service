package gcs.app;

import gcs.app.adapters.PgMemberAdapter;
import gcs.app.clustering.BlockingRandomProjector;
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
        BlockingRandomProjector projector
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

        handleAssignment(assignmentService.assign(versionedRecord, "work"), "work", versionedRecord);
        handleAssignment(assignmentService.assign(versionedRecord, "instance"), "instance", versionedRecord);

        return versionedRecord;
    }

    private void handleAssignment(Assignment assignment, String representation, InputRecord record) {
        if (assignment.getDecision() == Assignment.Decision.JOINED) {
            addMemberToCluster(assignment.getClusterId(), record.id(), representation);
            List<InputRecord> members = memberAdapter.getMembers(assignment.getClusterId());
            InputRecord newAnchor = synthesizer.synthesize(members);
            anchorPort.updateAnchor(assignment.getClusterId(), newAnchor);
            upsertAnchorToEs(assignment.getClusterId(), newAnchor, representation);
        } else {
            addMemberToCluster(assignment.getClusterId(), record.id(), representation);
            upsertAnchorToEs(assignment.getClusterId(), assignment.getClusterAnchor(), representation);
        }
    }

    private void addMemberToCluster(UUID clusterId, String recordId, String representation) {
        if ("work".equals(representation)) {
            WorkClusterMember member = new WorkClusterMember();
            member.setId(UUID.randomUUID());
            member.setWorkCluster(WorkCluster.builder().id(clusterId).build());
            member.setRecordId(recordId);
            workClusterMemberRepository.save(member);
        } else {
            InstanceClusterMember member = new InstanceClusterMember();
            member.setId(UUID.randomUUID());
            member.setInstanceCluster(InstanceCluster.builder().id(clusterId).build());
            member.setRecordId(recordId);
            instanceClusterMemberRepository.save(member);
        }
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
