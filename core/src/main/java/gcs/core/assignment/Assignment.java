package gcs.core.assignment;

import gcs.core.InputRecord;
import gcs.core.scoring.ScoreBreakdown;
import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import java.util.UUID;

/**
 * Represents the result of an assignment operation, indicating whether a record
 * was assigned to an existing cluster or a new one was created.
 */
@Serdeable
@Data
@Builder
@Jacksonized
public class Assignment {
    /** The decision made by the assignment service. */
    private final Decision decision;

    /** The ID of the cluster to which the record was assigned. */
    private final UUID clusterId;

    /** The anchor record of the assigned cluster. */
    private final InputRecord clusterAnchor;

    /** The score breakdown for the assignment. */
    private final ScoreBreakdown scoreBreakdown;

    public enum Decision {
        /** A new cluster was created for the record. */
        CREATED,
        /** The record was assigned to an existing cluster. */
        JOINED
    }
}
