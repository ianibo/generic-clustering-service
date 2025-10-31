package gcs.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DefaultIngestServiceTest {
    @Test
    void testIngest() throws IOException {
        // Arrange
        var embeddingService = mock(EmbeddingService.class);
        var vectorIndex = mock(VectorIndex.class);
        var canonicalizer = mock(Canonicalizer.class);
        var calibration = mock(Calibration.class);
        var objectMapper = new ObjectMapper();

        var service = new DefaultIngestService(embeddingService, vectorIndex, canonicalizer, calibration, objectMapper);

        var record = new InputRecord("rec-001", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var summary = "test summary";
        var embedding = new float[]{1.0f, 2.0f, 3.0f};
        var topKNeighbors = List.of(new VectorIndex.Neighbor<>("rec-002", 0.9, null, new float[0]));
        var radiusNeighbors = List.of(new VectorIndex.Neighbor<>("rec-003", 0.85, null, new float[0]));

        when(canonicalizer.summarize(record)).thenReturn(summary);
        when(embeddingService.embed(summary)).thenReturn(embedding);
        when(vectorIndex.topK(embedding, 5)).thenReturn(topKNeighbors);
        when(vectorIndex.radius(embedding, 0.8f)).thenReturn(radiusNeighbors);
        when(calibration.scoreToProb(0.9)).thenReturn(0.95);
        when(calibration.scoreToProb(0.85)).thenReturn(0.90);

        // Act
        var candidates = service.ingest(record);

        // Assert
        assertEquals(2, candidates.size());
        assertEquals("rec-002", candidates.get(0).id());
        assertEquals(0.9, candidates.get(0).score(), 1e-6);
        assertEquals(0.95, candidates.get(0).pDup(), 1e-6);
        assertEquals("rec-003", candidates.get(1).id());
        assertEquals(0.85, candidates.get(1).score(), 1e-6);
        assertEquals(0.90, candidates.get(1).pDup(), 1e-6);

        verify(vectorIndex).add("rec-001", embedding, record);

        var dataPath = Path.of("./data/records.jsonl");
        var storedJson = Files.readString(dataPath);
        var storedRecord = objectMapper.readValue(storedJson, InputRecord.class);
        assertEquals(record.id(), storedRecord.id());

        Files.deleteIfExists(dataPath);
        Files.deleteIfExists(dataPath.getParent());
    }
}
