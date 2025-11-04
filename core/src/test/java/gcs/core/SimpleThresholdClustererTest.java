package gcs.core;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Deprecated
class SimpleThresholdClustererTest {

    @Test
    void testClustering() {
        var clusterer = new SimpleThresholdClusterer<String>();
        var items = List.of(
                new Clusterer.Item<>("a", new float[]{1.0f, 0.0f}, "a"),
                new Clusterer.Item<>("b", new float[]{0.9f, 0.1f}, "b"),
                new Clusterer.Item<>("c", new float[]{0.0f, 1.0f}, "c"),
                new Clusterer.Item<>("d", new float[]{0.1f, 0.9f}, "d"),
                new Clusterer.Item<>("e", new float[]{0.5f, 0.5f}, "e")
        );

        var clusters = clusterer.cluster(items, 0.9);
        assertEquals(3, clusters.size());

        var cluster1 = findClusterContaining(clusters, "a");
        assertEquals(2, cluster1.members().size());
        assertTrue(cluster1.members().stream().anyMatch(m -> m.id().equals("b")));

        var cluster2 = findClusterContaining(clusters, "c");
        assertEquals(2, cluster2.members().size());
        assertTrue(cluster2.members().stream().anyMatch(m -> m.id().equals("d")));

        var cluster3 = findClusterContaining(clusters, "e");
        assertEquals(1, cluster3.members().size());
    }

    private Clusterer.Cluster<String> findClusterContaining(List<Clusterer.Cluster<String>> clusters, String id) {
        return clusters.stream()
                .filter(c -> c.members().stream().anyMatch(m -> m.id().equals(id)))
                .findFirst()
                .orElseThrow();
    }
}
