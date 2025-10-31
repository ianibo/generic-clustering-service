package gcs.app;

import gcs.core.Canonicalizer;
import gcs.core.EmbeddingService;
import gcs.core.InputRecord;
import gcs.core.VectorIndex;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

import java.util.List;

@Controller("/ingest")
public class IngestController {

    private final EmbeddingService embeddingService;
    private final VectorIndex<InputRecord> vectorIndex;
    private final Canonicalizer canonicalizer;

    public IngestController(EmbeddingService embeddingService, VectorIndex<InputRecord> vectorIndex, Canonicalizer canonicalizer) {
        this.embeddingService = embeddingService;
        this.vectorIndex = vectorIndex;
        this.canonicalizer = canonicalizer;
    }

    @Post
    public List<VectorIndex.Neighbor<InputRecord>> ingest(@Body InputRecord record) {
        String summary = canonicalizer.summarize(record);
        float[] embedding = embeddingService.embed(summary);
        List<VectorIndex.Neighbor<InputRecord>> neighbors = vectorIndex.topK(embedding, 5);
        vectorIndex.add(record.id(), embedding, record);
        return neighbors;
    }
}
