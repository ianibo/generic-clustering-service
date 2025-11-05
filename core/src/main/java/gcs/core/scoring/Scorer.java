package gcs.core.scoring;

import gcs.core.InputRecord;

/**
 * Calculates a detailed similarity score between an input record and a candidate anchor record.
 */
public interface Scorer {
    /**
     * Calculates a detailed similarity score between an input record and a candidate anchor record.
     *
     * @param inputRecord The input record.
     * @param candidateAnchor The candidate anchor record.
     * @param embeddingScore The cosine similarity score from the vector index.
     * @return A ScoreBreakdown object containing the detailed scoring information.
     */
    ScoreBreakdown score(InputRecord inputRecord, InputRecord candidateAnchor, double embeddingScore);
}
