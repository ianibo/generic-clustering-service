package gcs.app;

import gcs.app.clustering.BlockingRandomProjector;
import gcs.app.clustering.ESClusteringService;
import gcs.app.esvector.ESIndexStore;
import gcs.app.pgvector.InstanceCluster;
import gcs.app.pgvector.InstanceClusterMember;
import gcs.app.pgvector.WorkCluster;
import gcs.app.pgvector.WorkClusterMember;
import gcs.app.pgvector.storage.PGVectorStore;
import gcs.core.EmbeddingService;
import gcs.core.IngestService;
import gcs.core.InputRecord;
import gcs.core.canonicalization.Canonicalizer;
import gcs.core.classification.Classifier;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import lombok.extern.slf4j.Slf4j;
import jakarta.transaction.Transactional;

@Slf4j
@Singleton
public class DefaultIngestService implements IngestService {
    private static final double WORK_THRESHOLD = 0.9;
    private static final double INSTANCE_THRESHOLD = 0.95;

    private final EmbeddingService embeddingService;
    private final Map<String, Canonicalizer> canonicalizers;
    private final Canonicalizer defaultCanonicalizer;
    private final Classifier classifier;
    private final BlockingRandomProjector projector;
    private final ESClusteringService clusteringService;
    private final PGVectorStore pgVectorStore;
    private final ESIndexStore esIndexStore;

    public DefaultIngestService(
        @Named("openai") EmbeddingService embeddingService,
        List<Canonicalizer> canonicalizerList,
        Classifier classifier,
        BlockingRandomProjector projector,
        ESClusteringService clusteringService,
        @Named("pgvector") PGVectorStore pgVectorStore,
        @Named("es") ESIndexStore esIndexStore
    ) {
        this.embeddingService = embeddingService;
        this.canonicalizers = canonicalizerList.stream()
            .filter(c -> c.forContentType() != null)
            .collect(Collectors.toMap(Canonicalizer::forContentType, Function.identity()));
        this.defaultCanonicalizer = canonicalizerList.stream()
            .filter(c -> c.forContentType() == null)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No default canonicalizer found"));
        this.classifier = classifier;
        this.projector = projector;
        this.clusteringService = clusteringService;
        this.pgVectorStore = pgVectorStore;
        this.esIndexStore = esIndexStore;
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

        var contentType = classification.instanceClassification().contentType().toString();
        var canonicalizer = canonicalizers.getOrDefault(contentType, defaultCanonicalizer);
        log.info("Using canonicalizer {} for content type {}", canonicalizer.getClass().getSimpleName(), contentType);

        processCluster(versionedRecord, canonicalizer, "work", Canonicalizer.Intent.WORK, WORK_THRESHOLD);
        processCluster(versionedRecord, canonicalizer, "instance", Canonicalizer.Intent.INSTANCE, INSTANCE_THRESHOLD);

        return versionedRecord;
    }

	private void processCluster(InputRecord record, Canonicalizer canonicalizer, String clusterType, Canonicalizer.Intent intent, double threshold) {

		log.info("processCluster(....)");

		String summary = canonicalizer.summarize(record, intent);
		float[] embedding = embeddingService.embed(summary);
		float[] blockingEmbedding = projector.project(embedding);
		String indexName = clusterType + "_index";

		try {
			esIndexStore.getOrCreate(indexName);

			log.info("clusteringService.findClosestMatch.... indexName:{}",indexName);

			Optional<ESIndexStore.SearchResult> closestMatch = clusteringService.findClosestMatch(indexName, blockingEmbedding, "blocking", threshold);
			
			log.info("Result of findClosestMatch = {}",closestMatch);

			if (closestMatch.isPresent() && closestMatch.get().score() >= threshold) {
				// Add to existing cluster
				UUID clusterId = UUID.fromString(closestMatch.get().id());
				log.info("Attempt to save {}", clusterId);
				saveClusterMember(clusterType, clusterId, embedding, blockingEmbedding, indexName);
			} else {
				// Create new cluster
				log.info("Create new cluster");
				UUID newClusterId = createNewCluster(clusterType);
				saveClusterMember(clusterType, newClusterId, embedding, blockingEmbedding, indexName);
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	private void saveClusterMember(String clusterType, UUID clusterId, float[] embedding, float[] blockingEmbedding, String indexName) throws IOException {
		if ("work".equals(clusterType)) {
			WorkClusterMember member = new WorkClusterMember();
			member.setWorkCluster(WorkCluster.builder().id(clusterId).build());
			member.setEmbedding(embedding);
			member.setBlocking(blockingEmbedding);
			pgVectorStore.saveWorkClusterMember(member);
			esIndexStore.store(indexName, member);
		} else {
			InstanceClusterMember member = new InstanceClusterMember();
			member.setInstanceCluster(InstanceCluster.builder().id(clusterId).build());
			member.setEmbedding(embedding);
			member.setBlocking(blockingEmbedding);
			pgVectorStore.saveInstanceClusterMember(member);
			esIndexStore.store(indexName, member);
		}
	}

	private UUID createNewCluster(String clusterType) {

		log.info("Create new cluster of type {}",clusterType);

		if ("work".equals(clusterType)) {
			WorkCluster wc = WorkCluster.builder()
				.id(java.util.UUID.randomUUID())
				.build();
			log.info("Saving work {} {}",wc,wc.getId());
            return pgVectorStore.saveWorkCluster(wc).getId();
		} else {
			InstanceCluster ic = InstanceCluster.builder()
				.id(java.util.UUID.randomUUID())
				.build();
			log.info("Saving instance {} {}",ic,ic.getId());
			return pgVectorStore.saveInstanceCluster(ic).getId();
		}
	}
}
