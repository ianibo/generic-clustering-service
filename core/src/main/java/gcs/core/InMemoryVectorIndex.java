package gcs.core;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.PriorityQueue;

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
        var pq = new PriorityQueue<Neighbor<T>>(Comparator.comparingDouble(Neighbor::score));
        for (var entry : entries) {
            var neighbor = new Neighbor<>(entry.id(), CosineSimilarity.cosineSimilarity(query, entry.vector()), entry.payload(), entry.vector());
            pq.add(neighbor);
            if (pq.size() > k) {
                pq.poll();
            }
        }
        var result = new ArrayList<>(pq);
        result.sort(Comparator.comparingDouble(Neighbor<T>::score).reversed().thenComparing(Neighbor::id));
        return result;
    }

    @Override
    public List<Neighbor<T>> radius(float[] query, float threshold) {
        return entries.stream()
                .map(entry -> new Neighbor<>(entry.id(), CosineSimilarity.cosineSimilarity(query, entry.vector()), entry.payload(), entry.vector()))
                .filter(neighbor -> neighbor.score() >= threshold)
                .sorted(Comparator.comparingDouble(Neighbor<T>::score).reversed().thenComparing(Neighbor::id))
                .toList();
    }
}
