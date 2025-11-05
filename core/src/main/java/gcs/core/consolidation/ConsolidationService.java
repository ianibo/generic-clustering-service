package gcs.core.consolidation;

import java.util.UUID;

/**
 * Service responsible for consolidating clusters by merging or splitting them.
 */
public interface ConsolidationService {
    /**
     * Consolidates clusters.
     */
    void consolidate();

    /**
     * Merges two clusters into a single new cluster.
     *
     * @param clusterId1 The ID of the first cluster.
     * @param clusterId2 The ID of the second cluster.
     * @return The ID of the new, merged cluster.
     */
    UUID merge(UUID clusterId1, UUID clusterId2);

    /**
     * Splits a cluster into two or more new clusters.
     *
     * @param clusterId The ID of the cluster to split.
     */
    void split(UUID clusterId);
}
