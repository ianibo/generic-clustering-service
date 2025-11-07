package gcs.app;

import gcs.app.embedding.OpenAIEmbeddingService;
import gcs.core.EmbeddingService;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

/**
 * Provides an in-memory embedding service for tests so that we do not need
 * external API keys or network access to instantiate {@code DefaultIngestService}.
 */
@Factory
@Requires(env = Environment.TEST)
public final class TestEmbeddingFactory {

    @Singleton
    @Named("openai")
    @Replaces(OpenAIEmbeddingService.class)
    EmbeddingService testEmbeddingService() {
        return new TestEmbeddingService();
    }

    private static final class TestEmbeddingService implements EmbeddingService {
        private static final int TEST_DIM = 1536;
        private final gcs.core.HashingEmbeddingService delegate = new gcs.core.HashingEmbeddingService();

        @Override
        public float[] embed(String text) {
            float[] base = delegate.embed(text);
            float[] padded = new float[TEST_DIM];
            for (int i = 0; i < TEST_DIM; i++) {
                padded[i] = base[i % base.length];
            }
            return padded;
        }

        @Override
        public int dim() {
            return TEST_DIM;
        }
    }
}
