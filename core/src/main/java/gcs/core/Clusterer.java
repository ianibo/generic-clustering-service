package gcs.core;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

/**
 * A clustering algorithm.
 * @param <T> The type of the items to cluster.
 * @deprecated Use ESClusteringService instead.
 */
@Deprecated
public interface Clusterer<T> {

    /**
     * An item to be clustered.
     * @param id The ID of the item.
     * @param vector The vector representation of the item.
     * @param payload The payload of the item.
     * @param <T> The type of the payload.
     */
    @Serdeable
    record Item<T>(String id, float[] vector, T payload) {}

    /**
     * A cluster of items.
     * @param id The ID of the cluster.
     * @param members The members of the cluster.
     * @param <T> The type of the items in the cluster.
     */
    record Cluster<T>(String id, List<Item<T>> members) {}

    /**
     * Clusters the given items.
     * @param items The items to cluster.
     * @param threshold The clustering threshold.
     * @return The list of clusters.
     */
    List<Cluster<T>> cluster(List<Item<T>> items, double threshold);
}
