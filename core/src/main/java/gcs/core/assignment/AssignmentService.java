package gcs.core.assignment;

import gcs.core.InputRecord;
import gcs.core.EmbeddingService;

/**
 * Assigns an input record to an existing cluster or creates a new one.
 * This is the core of the clustering logic.
 */
public interface AssignmentService {
    /**
     * Assigns an input record to an existing cluster or creates a new one.
     *
     * @param record The input record to assign.
     * @param representation The representation of the record (e.g., "WORK", "INSTANCE").
     * @return An Assignment object describing the result of the operation.
     */
    Assignment assign(EmbeddingService embeddingService, InputRecord record, String representation);
}
