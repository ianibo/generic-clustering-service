package gcs.core.assignment;

import gcs.core.InputRecord;
import gcs.core.EmbeddingService;

/**
 * Assigns an input record to an existing cluster or creates a new one.
 * This is the core of the clustering logic.
 */
public interface AssignmentService {

    /**
     * Assigns an input record to an existing cluster or creates a new one, using a pre-computed embedding.
     *
     * @param record The input record to assign.
     * @param representation The representation of the record (e.g., "WORK", "INSTANCE").
     * @param embedding The pre-computed embedding of the record.
     * @param summary The pre-computed summary of the record.
     * @return An Assignment object describing the result of the operation.
     */
    Assignment assign(InputRecord record, String representation, float[] embedding, String summary);
}
