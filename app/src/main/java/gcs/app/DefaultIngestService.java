package gcs.app;

import gcs.core.Calibration;
import gcs.core.Canonicalizer;
import gcs.core.classification.Classifier;
import gcs.core.EmbeddingService;
import gcs.core.IngestService;
import gcs.core.InputRecord;
import gcs.core.VectorIndex;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Stream;

@Singleton
public class DefaultIngestService implements IngestService {
    private static final int TOP_K = 5;
    private static final float RADIUS = 0.8f;
    private static final Logger LOG = LoggerFactory.getLogger(DefaultIngestService.class);


    private final EmbeddingService embeddingService;
    private final VectorIndex<InputRecord> vectorIndex;
    private final Canonicalizer canonicalizer;
    private final Calibration calibration;
    private final InputRecordRepository inputRecordRepository;
    private final Classifier classifier;

    public DefaultIngestService(
        EmbeddingService embeddingService,
        VectorIndex<InputRecord> vectorIndex,
        Canonicalizer canonicalizer,
        Calibration calibration,
        InputRecordRepository inputRecordRepository,
        Classifier classifier
    ) {
        this.embeddingService = embeddingService;
        this.vectorIndex = vectorIndex;
        this.canonicalizer = canonicalizer;
        this.calibration = calibration;
        this.inputRecordRepository = inputRecordRepository;
        this.classifier = classifier;
    }

    @Override
    public List<Candidate> ingest(InputRecord record) {
        var classification = classifier.classify(record);
        LOG.info("Classified record {} as {} with explanation: {}", record.id(), classification.workType(), classification);

        // Create a new InputRecord with the classifierVersion
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

        String summary = canonicalizer.summarize(versionedRecord);
        float[] embedding = embeddingService.embed(summary);

        var topKNeighbors = vectorIndex.topK(embedding, TOP_K);
        var radiusNeighbors = vectorIndex.radius(embedding, RADIUS);

        vectorIndex.add(versionedRecord.id(), embedding, versionedRecord);
        store(versionedRecord, classification.classifierVersion());

        return Stream.concat(topKNeighbors.stream(), radiusNeighbors.stream())
            .distinct()
            .map(neighbor -> new Candidate(neighbor.id(), neighbor.score(), calibration.scoreToProb(neighbor.score())))
            .toList();
    }

    private void store(InputRecord record, int classifierVersion) {
        var entity = new InputRecordEntity();
        entity.setId(record.id());
        entity.setRecord(record);
        entity.setProcessingStatus(ProcessingStatus.PENDING);
        entity.setClassifierVersion(classifierVersion);
        inputRecordRepository.save(entity);
    }
}
