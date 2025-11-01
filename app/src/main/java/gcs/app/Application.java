package gcs.app;

import gcs.core.Calibration;
import gcs.core.Clusterer;
import gcs.core.EmbeddingService;
import gcs.core.HashingEmbeddingService;
import gcs.core.InMemoryVectorIndex;
import gcs.core.InputRecord;
import gcs.core.PlattCalibration;
import gcs.core.SimpleThresholdClusterer;
import gcs.core.VectorIndex;
import io.micronaut.runtime.Micronaut;
import jakarta.inject.Singleton;

/**
 * The main entry point for the Micronaut application.
 */
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }

    @Singleton
    public EmbeddingService embeddingService() {
        return new HashingEmbeddingService();
    }

    @Singleton
    public VectorIndex<InputRecord> vectorIndex() {
        return new InMemoryVectorIndex<>();
    }

    @Singleton
    public Calibration calibration() {
        return new PlattCalibration();
    }

    @Singleton
    public Clusterer<InputRecord> clusterer() {
        return new SimpleThresholdClusterer<>();
    }
}
