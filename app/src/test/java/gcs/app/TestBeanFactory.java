package gcs.app;

import gcs.core.Calibration;
import gcs.core.Canonicalizer;
import gcs.core.EmbeddingService;
import gcs.core.InputRecord;
import gcs.core.VectorIndex;
import gcs.core.classification.Classifier;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Singleton;

import static org.mockito.Mockito.mock;

@Factory
class TestBeanFactory {
    @Singleton
    @Replaces(EmbeddingService.class)
    EmbeddingService embeddingService() {
        return mock(EmbeddingService.class);
    }

    @Singleton
    @Replaces(VectorIndex.class)
    VectorIndex<InputRecord> vectorIndex() {
        return mock(VectorIndex.class);
    }

    @Singleton
    @Replaces(Canonicalizer.class)
    Canonicalizer canonicalizer() {
        return mock(Canonicalizer.class);
    }

    @Singleton
    @Replaces(Calibration.class)
    Calibration calibration() {
        return mock(Calibration.class);
    }

    @Singleton
    @Replaces(Classifier.class)
    Classifier classifier() {
        return mock(Classifier.class);
    }
}
