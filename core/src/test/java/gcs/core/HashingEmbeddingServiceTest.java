package gcs.app;

import gcs.core.HashingEmbeddingService;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;

class HashingEmbeddingServiceTest {

    @Test
    void testDeterminism() {
        HashingEmbeddingService service = new HashingEmbeddingService();
        String text = "The quick brown fox jumps over the lazy dog.";
        float[] embedding1 = service.embed(text);
        float[] embedding2 = service.embed(text);
        assertArrayEquals(embedding1, embedding2, 1e-6f);
    }
}
