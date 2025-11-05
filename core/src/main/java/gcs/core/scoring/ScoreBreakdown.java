package gcs.core.scoring;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

/**
 * Represents a detailed breakdown of the similarity score between two records.
 */
@Serdeable
@Data
@Builder
@Jacksonized
public class ScoreBreakdown {
    /** The final, total similarity score. */
    private final double total;

    /** The cosine similarity score from the vector index. */
    private final double embedding;

    /** The score based on key field similarity. */
    private final double keys;

    /** The score based on identifier similarity. */
    private final double ids;

    /** The score based on publication year similarity. */
    private final double pubyr;

    /** A penalty for conflicting fields. */
    private final double conflictPenalty;
}
