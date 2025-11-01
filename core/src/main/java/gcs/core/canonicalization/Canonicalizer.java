package gcs.core.canonicalization;

import gcs.core.InputRecord;

/**
 * A service for canonicalizing input records.
 */
public interface Canonicalizer {
    /**
     * Summarizes the given input record.
     * @param r The input record to summarize.
     * @return The summary of the input record.
     */
    String summarize(InputRecord r);

    /**
     * Returns the content type that this canonicalizer is for.
     * @return The content type.
     */
    String forContentType();
}
