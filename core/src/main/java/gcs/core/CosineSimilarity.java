package gcs.core;

/**
 * A utility class for calculating cosine similarity.
 */
public final class CosineSimilarity {

    private CosineSimilarity() {
        // Private constructor to prevent instantiation
    }

    /**
     * Calculates the cosine similarity between two vectors.
     * @param v1 The first vector.
     * @param v2 The second vector.
     * @return The cosine similarity between the two vectors.
     */
    public static double cosineSimilarity(float[] v1, float[] v2) {
        if (v1.length != v2.length) {
            throw new IllegalArgumentException("Vectors must have the same dimension");
        }
        double dotProduct = 0.0;
        double norm1 = 0.0;
        double norm2 = 0.0;
        for (int i = 0; i < v1.length; i++) {
            dotProduct += v1[i] * v2[i];
            norm1 += v1[i] * v1[i];
            norm2 += v2[i] * v2[i];
        }
        return dotProduct / (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
}
