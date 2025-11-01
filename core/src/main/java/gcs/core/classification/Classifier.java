package gcs.core.classification;

import gcs.core.InputRecord;

public interface Classifier {
    ClassificationResult classify(InputRecord record);
}
