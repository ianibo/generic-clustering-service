package gcs.core;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public record ClassificationResult(
    WorkType workType,
    String explanation,
    double confidence
) {}
