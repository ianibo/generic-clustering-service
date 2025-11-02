package gcs.core;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import jakarta.inject.Named;
import jakarta.inject.Singleton;

@Singleton
@Named("openai")
public class OpenAiEmbeddingService implements EmbeddingService {

    private final EmbeddingModel embeddingModel;
    private Integer dimension;

    public OpenAiEmbeddingService(EmbeddingModel embeddingModel) {
        this.embeddingModel = embeddingModel;
    }

    @Override
    public float[] embed(String text) {
        Response<Embedding> response = embeddingModel.embed(text);
        Embedding embedding = response.content();
        if (this.dimension == null) {
            this.dimension = embedding.dimension();
        }
        return embedding.vector();
    }

    @Override
    public int dim() {
        if (dimension == null) {
            // Embed a dummy non-empty string to get the dimension.
            // Some models may not handle empty strings well.
            // This is a one-time operation.
            dimension = embeddingModel.embed(" ").content().dimension();
        }
        return dimension;
    }
}
