package gcs.core.classification;

import gcs.core.InputRecord;

/**
 * A classifier that determines the detailed instance (Manifestation/Item) classification.
 */
public interface InstanceClassifier {
    /**
     * Classifies an input record into a detailed instance classification.
     *
     * @param record The input record to classify.
     * @return The instance classification.
     */
    InstanceClassification classify(InputRecord record);
}
