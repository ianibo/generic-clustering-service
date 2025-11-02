package gcs.core.canonicalization;

import gcs.core.InputRecord;

/**
 * A service for canonicalizing input records.
 */
public interface Canonicalizer {
    enum Intent {
        WORK,
        INSTANCE
    }

    /**
     * Summarizes the given input record.
     * @param r The input record to summarize.
     * @return The summary of the input record.
     */
    default String summarize(InputRecord r) {
        return summarize(r, Intent.WORK);
    }

    /**
     * Summarizes the given input record for a specific intent.
     * @param r The input record to summarize.
     * @param intent The intent of the summary.
     * @return The summary of the input record.
     */
    String summarize(InputRecord r, Intent intent);

    /**
     * Returns the content type that this canonicalizer is for.
     * @return The content type.
     */
    String forContentType();
}
