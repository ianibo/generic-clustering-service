package gcs.core.lineage;

import io.micronaut.serde.annotation.Serdeable;
import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents the lineage of a cluster, tracking its history of merges and splits.
 */
@Serdeable
@Data
@Builder
@Jacksonized
public class Lineage {
    /** The current status of the cluster. */
    private final Status status;

    /** The current successor CIDs. */
    private final List<String> current;

    /** The current content fingerprint. */
    private final String cfp;

    /** A minimal representation of the synth record. */
    private final String synth;

    /** A list of recent lineage edges. */
    @Builder.Default
    private final List<Edge> recentHistory = new ArrayList<>();

    public enum Status {
        /** The cluster is current and has not been merged or split. */
        CURRENT,
        /** The cluster has been merged into another cluster. */
        MERGED,
        /** The cluster has been superseded by one or more other clusters. */
        SUPERSEDED
    }

    @Serdeable
    @Data
    @Builder
    @Jacksonized
    public static class Edge {
        /** The type of lineage event. */
        private final Type type;

        /** The source cluster ID. */
        private final String source;

        /** The destination cluster ID. */
        private final String destination;

        public enum Type {
            /** A merge event. */
            MERGED_INTO,
            /** A split event. */
            SPLIT_INTO,
            /** A "same as" event. */
            SAME_AS
        }
    }
}
