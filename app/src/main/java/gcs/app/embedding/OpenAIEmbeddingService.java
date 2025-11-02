package gcs.app.embedding;

import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.openai.OpenAiEmbeddingModel;
import gcs.core.EmbeddingService;
import io.micronaut.context.annotation.Factory;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("openai")
public class OpenAIEmbeddingService implements EmbeddingService {

    private final EmbeddingModel embeddingModel;

    // As of langchain4j v0.32.0, the dimension is not available directly from the model.
    private volatile Integer dimension;

    public OpenAIEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public float[] embed(String text) {
        return embeddingModel.embed(text).content().vector();
    }

    @Override
    public int dim() {
        if (dimension == null) {
            synchronized (this) {
                if (dimension == null) {
                    dimension = embeddingModel.embed("for dimension").content().vector().length;
                }
            }
        }
        return dimension;
    }
}

@Factory
class OpenAiEmbeddingModelFactory {
    @Singleton
    EmbeddingModel openAiEmbeddingModel() {
        return OpenAiEmbeddingModel.builder()
                .apiKey(System.getenv("OPENAI_API_KEY"))
                .modelName("text-embedding-3-small")
                .build();
    }
}
