package gcs.core;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * A simple, deterministic embedding service that uses a hashing algorithm to generate embeddings.
 */
public class HashingEmbeddingService implements EmbeddingService {

    private static final int DIMENSION = 128;

    @Override
    public float[] embed(String text) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(text.getBytes(StandardCharsets.UTF_8));
            float[] vector = new float[DIMENSION];
            for (int i = 0; i < DIMENSION; i++) {
                vector[i] = (float) (hash[i % hash.length] & 0xFF) / 255.0f;
            }
            return vector;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int dim() {
        return DIMENSION;
    }
}
