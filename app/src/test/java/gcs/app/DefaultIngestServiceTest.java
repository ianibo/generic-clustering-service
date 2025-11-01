package gcs.app;

import gcs.core.Calibration;
import gcs.core.Canonicalizer;
import gcs.core.EmbeddingService;
import gcs.core.InputRecord;
import gcs.core.VectorIndex;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class DefaultIngestServiceTest {
    @Test
    void testIngest() {
        // Arrange
        var embeddingService = mock(EmbeddingService.class);
        var vectorIndex = mock(VectorIndex.class);
        var canonicalizer = mock(Canonicalizer.class);
        var calibration = mock(Calibration.class);
        var inputRecordRepository = mock(InputRecordRepository.class);

        var service = new DefaultIngestService(embeddingService, vectorIndex, canonicalizer, calibration, inputRecordRepository);

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

        var captor = ArgumentCaptor.forClass(InputRecordEntity.class);
        verify(inputRecordRepository).save(captor.capture());
        assertEquals("rec-001", captor.getValue().getId());
        assertEquals(ProcessingStatus.PENDING, captor.getValue().getProcessingStatus());
    }
}
