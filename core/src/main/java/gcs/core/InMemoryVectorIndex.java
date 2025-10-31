package gcs.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * An in-memory vector index that performs a brute-force search.
 * @param <T> The type of the payload associated with each vector.
 */
public class InMemoryVectorIndex<T> implements VectorIndex<T> {

    private final List<Entry<T>> entries = new ArrayList<>();

    private record Entry<T>(String id, float[] vector, T payload) {}

    @Override
    public void add(String id, float[] vec, T payload) {
        entries.add(new Entry<>(id, vec, payload));
    }

    @Override
    public List<Neighbor<T>> topK(float[] query, int k) {
        return entries.stream()
                .map(entry -> new Neighbor<>(entry.id(), CosineSimilarity.cosineSimilarity(query, entry.vector()), entry.payload(), entry.vector()))
                .sorted(Comparator.comparingDouble((Neighbor<T> n) -> n.score()).reversed())
                .limit(k)
                .toList();
    }

    @Override
    public List<Neighbor<T>> radius(float[] query, float threshold) {
        return entries.stream()
                .map(entry -> new Neighbor<>(entry.id(), CosineSimilarity.cosineSimilarity(query, entry.vector()), entry.payload(), entry.vector()))
                .filter(neighbor -> neighbor.score() >= threshold)
                .sorted(Comparator.comparingDouble((Neighbor<T> n) -> n.score()).reversed())
                .toList();
    }
}
