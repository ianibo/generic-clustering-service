package gcs.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CosineSimilarityTest {

    @Test
    void testCosineSimilarity() {
        float[] v1 = {1.0f, 2.0f, 3.0f};
        float[] v2 = {1.0f, 2.0f, 3.0f};
        assertEquals(1.0, CosineSimilarity.cosineSimilarity(v1, v2), 1e-6);

        float[] v3 = {1.0f, 0.0f, 0.0f};
        float[] v4 = {0.0f, 1.0f, 0.0f};
        assertEquals(0.0, CosineSimilarity.cosineSimilarity(v3, v4), 1e-6);

        float[] v5 = {1.0f, 1.0f, 1.0f};
        float[] v6 = {2.0f, 2.0f, 2.0f};
        assertEquals(1.0, CosineSimilarity.cosineSimilarity(v5, v6), 1e-6);
    }
}
