package gcs.core.assignment;

import gcs.core.InputRecord;
import java.util.Optional;
import java.util.UUID;

/**
 * A port for reading and updating anchor records in the system of record.
 */
public interface AnchorPort {
    /**
     * Retrieves an anchor record by its cluster ID.
     *
     * @param clusterId The ID of the cluster.
     * @return An Optional containing the anchor record, or empty if not found.
     */
    Optional<InputRecord> getAnchor(UUID clusterId);

    /**
     * Creates a new cluster with the given anchor record.
     *
     * @param anchor The anchor record for the new cluster.
     * @param representation The representation of the record (e.g., "WORK", "INSTANCE").
     * @param label The label for the new cluster.
     * @param initialCentroid The initial centroid for the new cluster.
     * @return The ID of the newly created cluster.
     */
    UUID createCluster(InputRecord anchor, String representation, String label, float[] initialCentroid);

    /**
     * Updates the anchor record for an existing cluster.
     *
     * @param clusterId The ID of the cluster to update.
     * @param anchor The new anchor record.
     */
    void updateAnchor(UUID clusterId, InputRecord anchor);
}
