package gcs.core;

/**
 * A service for embedding text into a vector space.
 */
public interface EmbeddingService {
    /**
     * Embeds the given text into a vector.
     * @param text The text to embed.
     * @return The vector embedding of the text.
     */
    float[] embed(String text);

    /**
     * The dimension of the embedding vector.
     * @return The dimension of the embedding vector.
     */
    int dim();
}
