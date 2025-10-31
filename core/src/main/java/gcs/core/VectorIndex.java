package gcs.core;

import java.util.List;

/**
 * A vector index for storing and searching vectors.
 * @param <T> The type of the payload associated with each vector.
 */
public interface VectorIndex<T> {

    /**
     * A neighbor in the vector index.
     * @param id The ID of the neighbor.
     * @param score The similarity score of the neighbor.
     * @param payload The payload of the neighbor.
     * @param <T> The type of the payload.
     */
    record Neighbor<T>(String id, double score, T payload) {}

    /**
     * Adds a vector to the index.
     * @param id The ID of the vector.
     * @param vec The vector to add.
     * @param payload The payload associated with the vector.
     */
    void add(String id, float[] vec, T payload);

    /**
     * Finds the top-k nearest neighbors to the given query vector.
     * @param query The query vector.
     * @param k The number of neighbors to find.
     * @return The list of top-k nearest neighbors.
     */
    List<Neighbor<T>> topK(float[] query, int k);

    /**
     * Finds all neighbors within the given radius of the query vector.
     * @param query The query vector.
     * @param threshold The radius.
     * @return The list of neighbors within the radius.
     */
    List<Neighbor<T>> radius(float[] query, float threshold);
}
