package gcs.examples;

import gcs.core.EmbeddingService;
import gcs.core.HashingEmbeddingService;
import gcs.core.InMemoryVectorIndex;
import gcs.core.InputRecord;
import gcs.core.VectorIndex;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;


public class Cli {

    public static void main(String[] args) throws Exception {
        EmbeddingService embeddingService = new HashingEmbeddingService();
        VectorIndex<InputRecord> vectorIndex = new InMemoryVectorIndex<>();

        InputStream inputStream = Cli.class.getResourceAsStream("/data/input.jsonl");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            ObjectMapper objectMapper = new ObjectMapper();
            String line;
            while ((line = reader.readLine()) != null) {
                InputRecord record = objectMapper.readValue(line, new TypeReference<InputRecord>() {});
                String title = record.titles().stream()
                    .filter(t -> "main".equals(t.type()))
                    .findFirst()
                    .map(InputRecord.Title::value)
                    .orElse("");
                float[] embedding = embeddingService.embed(title);
                vectorIndex.add(record.id(), embedding, record);
            }
        }

        // Search for the top 3 candidates for the first record
        InputStream searchStream = Cli.class.getResourceAsStream("/data/input.jsonl");
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(searchStream, StandardCharsets.UTF_8))) {
            String firstLine = reader.readLine();
            ObjectMapper objectMapper = new ObjectMapper();
            InputRecord firstRecord = objectMapper.readValue(firstLine, new TypeReference<InputRecord>() {});
            String firstTitle = firstRecord.titles().stream()
                .filter(t -> "main".equals(t.type()))
                .findFirst()
                .map(InputRecord.Title::value)
                .orElse("");
            float[] queryVector = embeddingService.embed(firstTitle);
            List<VectorIndex.Neighbor<InputRecord>> neighbors = vectorIndex.topK(queryVector, 3);

            System.out.println("Top 3 candidates for: " + firstTitle);
            for (VectorIndex.Neighbor<InputRecord> neighbor : neighbors) {
                System.out.println(neighbor);
            }
        }
    }
}
