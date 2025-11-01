package gcs.app;

import gcs.core.Calibration;
import gcs.core.Canonicalizer;
import gcs.core.EmbeddingService;
import gcs.core.IngestService;
import gcs.core.InputRecord;
import gcs.core.VectorIndex;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Stream;

@Singleton
public class DefaultIngestService implements IngestService {
    private static final int TOP_K = 5;
    private static final float RADIUS = 0.8f;

    private final EmbeddingService embeddingService;
    private final VectorIndex<InputRecord> vectorIndex;
    private final Canonicalizer canonicalizer;
    private final Calibration calibration;
    private final InputRecordRepository inputRecordRepository;

    public DefaultIngestService(
        EmbeddingService embeddingService,
        VectorIndex<InputRecord> vectorIndex,
        Canonicalizer canonicalizer,
        Calibration calibration,
        InputRecordRepository inputRecordRepository
    ) {
        this.embeddingService = embeddingService;
        this.vectorIndex = vectorIndex;
        this.canonicalizer = canonicalizer;
        this.calibration = calibration;
        this.inputRecordRepository = inputRecordRepository;
    }

    @Override
    public List<Candidate> ingest(InputRecord record) {
        String summary = canonicalizer.summarize(record);
        float[] embedding = embeddingService.embed(summary);

        var topKNeighbors = vectorIndex.topK(embedding, TOP_K);
        var radiusNeighbors = vectorIndex.radius(embedding, RADIUS);

        vectorIndex.add(record.id(), embedding, record);
        store(record);

        return Stream.concat(topKNeighbors.stream(), radiusNeighbors.stream())
            .distinct()
            .map(neighbor -> new Candidate(neighbor.id(), neighbor.score(), calibration.scoreToProb(neighbor.score())))
            .toList();
    }

    private void store(InputRecord record) {
        var entity = new InputRecordEntity();
        entity.setId(record.id());
        entity.setRecord(record);
        entity.setProcessingStatus(ProcessingStatus.PENDING);
        inputRecordRepository.save(entity);
    }
}
