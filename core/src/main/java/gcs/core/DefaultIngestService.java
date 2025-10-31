package gcs.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.stream.Stream;

@Singleton
public class DefaultIngestService implements IngestService {
    private static final int TOP_K = 5;
    private static final float RADIUS = 0.8f;
    private static final Path DATA_PATH = Path.of("./data/records.jsonl");

    private final EmbeddingService embeddingService;
    private final VectorIndex<InputRecord> vectorIndex;
    private final Canonicalizer canonicalizer;
    private final Calibration calibration;
    private final ObjectMapper objectMapper;

    public DefaultIngestService(
        EmbeddingService embeddingService,
        VectorIndex<InputRecord> vectorIndex,
        Canonicalizer canonicalizer,
        Calibration calibration,
        ObjectMapper objectMapper
    ) {
        this.embeddingService = embeddingService;
        this.vectorIndex = vectorIndex;
        this.canonicalizer = canonicalizer;
        this.calibration = calibration;
        this.objectMapper = objectMapper;
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
        try {
            Files.createDirectories(DATA_PATH.getParent());
            String json = objectMapper.writeValueAsString(record);
            Files.writeString(DATA_PATH, json + "\n", StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
