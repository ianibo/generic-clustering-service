package gcs.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HashingEmbeddingServiceTest {

    @Test
    void testStability() {
        HashingEmbeddingService service = new HashingEmbeddingService();
        String text = "This is a test sentence.";
        float[] v1 = service.embed(text);
        float[] v2 = service.embed(text);
        assertArrayEquals(v1, v2, 1e-6f);
    }

    @Test
    void testSimilarity() {
        HashingEmbeddingService service = new HashingEmbeddingService();
        String text1 = "This is a test sentence.";
        String text2 = "This is a test sentence, but with a small change.";
        float[] v1 = service.embed(text1);
        float[] v2 = service.embed(text2);
        double similarity = CosineSimilarity.cosineSimilarity(v1, v2);
        assertTrue(similarity > 0.8, "Expected high cosine similarity, but got " + similarity);
    }
}
