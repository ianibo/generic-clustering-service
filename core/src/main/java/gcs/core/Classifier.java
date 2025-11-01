package gcs.core;

public interface Classifier {
    ClassificationResult classify(InputRecord record);
}
