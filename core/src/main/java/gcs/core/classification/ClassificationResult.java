package gcs.core.classification;

import io.micronaut.serde.annotation.Serdeable;
import java.util.List;

@Serdeable
public record ClassificationResult(
    WorkType workType,
    InstanceClassification instanceClassification,
    List<String> evidence,
    String source,
    double confidence,
    int classifierVersion
) {}
