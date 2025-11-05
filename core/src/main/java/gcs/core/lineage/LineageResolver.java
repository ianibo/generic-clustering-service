package gcs.core.lineage;

import java.util.UUID;

/**
 * Resolves the lineage of a cluster, providing its history and current status.
 */
public interface LineageResolver {
    /**
     * Resolves the lineage of a cluster.
     *
     * @param clusterId The ID of the cluster to resolve.
     * @return A Lineage object describing the cluster's history.
     */
    Lineage resolve(UUID clusterId);
}
