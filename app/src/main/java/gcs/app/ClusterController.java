package gcs.app;

import gcs.core.Clusterer;
import gcs.core.InputRecord;
import gcs.core.SimpleThresholdClusterer;
import gcs.core.VectorIndex;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import jakarta.inject.Singleton;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller("/cluster")
public class ClusterController {

    private final Clusterer<InputRecord> clusterer;
    private final VectorIndex<InputRecord> vectorIndex;

    public ClusterController(Clusterer<InputRecord> clusterer, VectorIndex<InputRecord> vectorIndex) {
        this.clusterer = clusterer;
        this.vectorIndex = vectorIndex;
    }

    @Get("/{id}")
    public List<InputRecord> getCluster(String id) {
        // In a real application, this would retrieve the items from a persistent store
        List<Clusterer.Item<InputRecord>> items = vectorIndex.topK(new float[128], 100).stream()
                .map(neighbor -> new Clusterer.Item<>(neighbor.id(), new float[128], neighbor.payload()))
                .collect(Collectors.toList());

        List<Clusterer.Cluster<InputRecord>> clusters = clusterer.cluster(items, 0.8);

        return clusters.stream()
                .filter(cluster -> cluster.id().equals(id))
                .findFirst()
                .map(cluster -> cluster.members().stream().map(Clusterer.Item::payload).collect(Collectors.toList()))
                .orElse(Collections.emptyList());
    }

    @Singleton
    public Clusterer<InputRecord> clusterer() {
        return new SimpleThresholdClusterer<>();
    }
}
