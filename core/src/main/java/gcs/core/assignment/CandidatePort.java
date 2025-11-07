package gcs.core.assignment;

import gcs.core.InputRecord;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * A port for retrieving candidate anchors from a vector index.
 */
public interface CandidatePort {
    /**
     * Retrieves the top-K candidate anchors for a given embedding.
     *
     * @param embedding The embedding to search for.
     * @param representation The representation of the record (e.g., "WORK", "INSTANCE").
     * @param k The number of candidates to retrieve.
     * @param filters Optional filters to apply to the search.
     * @return A list of candidate anchors.
     */
    List<Candidate> findCandidates(float[] embedding, String representation, int k, Optional<Map<String, String>> filters);

    /**
     * A candidate anchor retrieved from the vector index.
     */
    interface Candidate {
        /**
         * @return The ID of the cluster.
         */
        String getClusterId();

        /**
         * @return The anchor record of the cluster.
         */
        InputRecord getAnchor();

        /**
         * @return The cosine similarity score of the candidate.
         */
        double getScore();
    }
}
