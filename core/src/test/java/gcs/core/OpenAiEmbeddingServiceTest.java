package gcs.core;

import dev.langchain4j.data.embedding.Embedding;
import dev.langchain4j.model.embedding.EmbeddingModel;
import dev.langchain4j.model.output.Response;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Primary;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
class OpenAiEmbeddingServiceTest {
    @Inject
    private EmbeddingModel embeddingModel;

    @Inject
    @jakarta.inject.Named("openai")
    private EmbeddingService openAiEmbeddingService;

    @Test
    void testEmbed() {
        String text = "hello";
        float[] expectedEmbedding = new float[]{1.0f, 2.0f, 3.0f};
        Response<Embedding> response = Response.from(Embedding.from(expectedEmbedding));

        when(embeddingModel.embed(text)).thenReturn(response);

        float[] actualEmbedding = openAiEmbeddingService.embed(text);

        assertArrayEquals(expectedEmbedding, actualEmbedding);
    }

    @Test
    void testDim() {
        float[] expectedEmbedding = new float[]{1.0f, 2.0f, 3.0f};
        Response<Embedding> response = Response.from(Embedding.from(expectedEmbedding));
        when(embeddingModel.embed(anyString())).thenReturn(response);

        int actualDimension = openAiEmbeddingService.dim();

        assertEquals(3, actualDimension);
    }

    @Bean
    @Primary
    @Replaces(EmbeddingModel.class)
    EmbeddingModel embeddingModel() {
        return mock(EmbeddingModel.class);
    }
}
