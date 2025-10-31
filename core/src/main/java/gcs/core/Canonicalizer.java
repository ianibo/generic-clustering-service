package gcs.core;

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
}
