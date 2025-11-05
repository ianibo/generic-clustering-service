package gcs.app;

import com.pgvector.PGvector;
import gcs.app.clustering.CentroidService;
import gcs.app.util.TestRecordLoader;
import gcs.core.InputRecord;
import gcs.core.assignment.Assignment;
import gcs.core.assignment.AssignmentService;
import io.micronaut.context.annotation.Replaces;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DefaultIngestServiceCentroidTest {

    private DefaultIngestService service;
    private AssignmentService assignmentService;
    private CentroidService centroidService;

    @BeforeEach
    void setUp() {
        assignmentService = mock(AssignmentService.class);
        centroidService = mock(CentroidService.class);
        service = new DefaultIngestService(
            mock(gcs.core.classification.Classifier.class),
            assignmentService,
            mock(gcs.app.adapters.PgMemberAdapter.class),
            mock(gcs.core.synthesis.Synthesizer.class),
            mock(gcs.core.assignment.AnchorPort.class),
            mock(gcs.app.esvector.ESIndexStore.class),
            mock(gcs.app.pgvector.storage.WorkClusterMemberRepository.class),
            mock(gcs.app.pgvector.storage.InstanceClusterMemberRepository.class),
            mock(gcs.core.EmbeddingService.class),
            java.util.List.of(mock(gcs.core.canonicalization.Canonicalizer.class)),
            mock(gcs.app.clustering.BlockingRandomProjector.class),
            centroidService
        );
    }

    @Test
    void testIngestCallsCentroidService() throws IOException {
        // Arrange
        InputRecord record = TestRecordLoader.loadRecord("4bcc8bff-2de9-50db-86ea-af75a84de228");
        UUID clusterId = UUID.randomUUID();
        Assignment assignment = Assignment.builder()
            .decision(Assignment.Decision.JOINED)
            .clusterId(clusterId)
            .build();
        when(assignmentService.assign(any(), any())).thenReturn(assignment);

        // Act
        service.ingest(record);

        // Assert
        ArgumentCaptor<UUID> clusterIdCaptor = ArgumentCaptor.forClass(UUID.class);
        ArgumentCaptor<String> representationCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<PGvector> embeddingCaptor = ArgumentCaptor.forClass(PGvector.class);

        verify(centroidService, times(2)).updateCentroid(clusterIdCaptor.capture(), representationCaptor.capture(), embeddingCaptor.capture());

        assertEquals(clusterId, clusterIdCaptor.getAllValues().get(0));
        assertEquals("work", representationCaptor.getAllValues().get(0));
        assertEquals(clusterId, clusterIdCaptor.getAllValues().get(1));
        assertEquals("instance", representationCaptor.getAllValues().get(1));
    }
}
