package gcs.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple clustering algorithm that uses a threshold to form clusters.
 * @param <T> The type of the items to cluster.
 */
public class SimpleThresholdClusterer<T> implements Clusterer<T> {

    @Override
    public List<Cluster<T>> cluster(List<Item<T>> items, double threshold) {
        Map<String, String> itemToCluster = new HashMap<>();
        Map<String, List<Item<T>>> clusters = new HashMap<>();

        for (int i = 0; i < items.size(); i++) {
            for (int j = i + 1; j < items.size(); j++) {
                Item<T> item1 = items.get(i);
                Item<T> item2 = items.get(j);

                if (CosineSimilarity.cosineSimilarity(item1.vector(), item2.vector()) >= threshold) {
                    String cluster1 = itemToCluster.get(item1.id());
                    String cluster2 = itemToCluster.get(item2.id());

                    if (cluster1 == null && cluster2 == null) {
                        String clusterId = "cluster-" + clusters.size();
                        itemToCluster.put(item1.id(), clusterId);
                        itemToCluster.put(item2.id(), clusterId);
                        clusters.put(clusterId, new ArrayList<>(List.of(item1, item2)));
                    } else if (cluster1 != null && cluster2 == null) {
                        itemToCluster.put(item2.id(), cluster1);
                        clusters.get(cluster1).add(item2);
                    } else if (cluster1 == null && cluster2 != null) {
                        itemToCluster.put(item1.id(), cluster2);
                        clusters.get(cluster2).add(item1);
                    } else if (cluster1 != null && cluster2 != null && !cluster1.equals(cluster2)) {
                        // Merge clusters
                        List<Item<T>> cluster2Items = clusters.remove(cluster2);
                        clusters.get(cluster1).addAll(cluster2Items);
                        for (Item<T> item : cluster2Items) {
                            itemToCluster.put(item.id(), cluster1);
                        }
                    }
                }
            }
        }

        return clusters.entrySet().stream()
                .map(entry -> new Cluster<>(entry.getKey(), entry.getValue()))
                .toList();
    }
}
