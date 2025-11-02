package gcs.core;

/**
 * The main entry point for the GCS service.
 */
public interface IngestService {
    /**
     * Ingests a single record into the system.
     * @param record The record to ingest.
     * @return The ingested record, possibly enriched with additional information.
     */
    InputRecord ingest(InputRecord record);
}
