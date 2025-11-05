package gcs.core.policy;

import gcs.core.InputRecord;

/**
 * Defines policies for different representations of records, such as "Work" and "Instance".
 * This includes rules for field agreement, conflict penalties, and scoring adjustments.
 */
public interface RepresentationPolicy {
    /**
     * Checks if two records have sufficient field agreement to be considered for clustering.
     *
     * @param record1 The first record.
     * @param record2 The second record.
     * @return True if the records have sufficient field agreement, false otherwise.
     */
    boolean fieldAgreementOk(InputRecord record1, InputRecord record2);

    /**
     * Calculates a conflict penalty based on the differences between two records.
     *
     * @param record1 The first record.
     * @param record2 The second record.
     * @return A penalty score to be subtracted from the total similarity score.
     */
    double conflictPenalty(InputRecord record1, InputRecord record2);

    /**
     * Scores the similarity of publication years between two records.
     *
     * @param record1 The first record.
     * @param record2 The second record.
     * @return A score from 0.0 to 1.0 indicating the similarity of the publication years.
     */
    double scorePublicationYear(InputRecord record1, InputRecord record2);
}
