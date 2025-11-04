package gcs.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A simple clustering algorithm that uses a threshold to form clusters.
 * This implementation builds a graph where an edge exists between two items if their cosine
 * similarity is greater than or equal to the given threshold. The clusters are the connected
 * components of this graph.
 *
 * @param <T> The type of the items to cluster.
 * @deprecated Use ESClusteringService instead.
 */
@Deprecated
public class SimpleThresholdClusterer<T> implements Clusterer<T> {

    @Override
    public List<Cluster<T>> cluster(List<Item<T>> items, double threshold) {
        if (items.isEmpty()) {
            return new ArrayList<>();
        }

        var dsu = new DSU<Item<T>>();
        for (var item : items) {
            dsu.makeSet(item);
        }

        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                var item1 = items.get(i);
                var item2 = items.get(j);
                if (CosineSimilarity.cosineSimilarity(item1.vector(), item2.vector()) >= threshold) {
                    dsu.union(item1, item2);
                }
            }
        }

        return dsu.getComponents().entrySet().stream()
                .map(entry -> new Cluster<>(entry.getKey().id(), entry.getValue()))
                .collect(Collectors.toList());
    }

    private static class DSU<T> {
        private final Map<T, T> parent = new HashMap<>();
        private final Map<T, Integer> rank = new HashMap<>();

        public void makeSet(T item) {
            parent.put(item, item);
            rank.put(item, 0);
        }

        public T find(T item) {
            if (parent.get(item).equals(item)) {
                return item;
            }
            var root = find(parent.get(item));
            parent.put(item, root);
            return root;
        }

        public void union(T item1, T item2) {
            var root1 = find(item1);
            var root2 = find(item2);
            if (!root1.equals(root2)) {
                if (rank.get(root1) < rank.get(root2)) {
                    parent.put(root1, root2);
                } else if (rank.get(root1) > rank.get(root2)) {
                    parent.put(root2, root1);
                } else {
                    parent.put(root2, root1);
                    rank.put(root1, rank.get(root1) + 1);
                }
            }
        }

        public Map<T, List<T>> getComponents() {
            var components = new HashMap<T, List<T>>();
            for (var item : parent.keySet()) {
                var root = find(item);
                components.computeIfAbsent(root, k -> new ArrayList<>()).add(item);
            }
            return components;
        }
    }
}
