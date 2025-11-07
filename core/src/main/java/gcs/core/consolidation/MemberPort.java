package gcs.core.consolidation;

import gcs.core.InputRecord;
import java.util.List;

/**
 * A port for retrieving the members of a cluster.
 */
public interface MemberPort {
    /**
     * Retrieves the members of a cluster.
     *
     * @param clusterId The ID of the cluster.
     * @return A list of the cluster's members.
     */
    List<InputRecord> getMembers(String clusterId);
}
