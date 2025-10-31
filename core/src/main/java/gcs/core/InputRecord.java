package gcs.core;

import io.micronaut.serde.annotation.Serdeable;

/**
 * A placeholder for the canonical JSON input record.
 * @param id The unique identifier of the record.
 * @param text The text content of the record.
 */
@Serdeable
public record InputRecord(String id, String text) {}
