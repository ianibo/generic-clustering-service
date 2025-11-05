package gcs.app;

import gcs.core.InputRecord;
import gcs.core.assignment.Assignment;
import gcs.core.assignment.AssignmentService;
import gcs.core.classification.ClassificationResult;
import gcs.core.classification.Classifier;
import gcs.core.classification.ContentType;
import gcs.core.classification.InstanceClassification;
import gcs.core.classification.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import gcs.app.util.TestRecordLoader;

class ClassificationTest {

    private DefaultIngestService service;
    private AssignmentService assignmentService;
    private Classifier classifier;


    @BeforeEach
    void setup() {
        classifier = mock(Classifier.class);
        assignmentService = mock(AssignmentService.class);

        service = new DefaultIngestService(
                classifier,
                assignmentService,
                mock(gcs.app.adapters.PgMemberAdapter.class),
                mock(gcs.core.synthesis.Synthesizer.class),
                mock(gcs.core.assignment.AnchorPort.class),
                mock(gcs.app.esvector.ESIndexStore.class),
                mock(gcs.app.pgvector.storage.WorkClusterMemberRepository.class),
                mock(gcs.app.pgvector.storage.InstanceClusterMemberRepository.class),
                mock(gcs.core.EmbeddingService.class),
                List.of(mock(gcs.core.canonicalization.Canonicalizer.class)),
                mock(gcs.app.clustering.BlockingRandomProjector.class)
        );
    }

    @Test
    void testCollectedPoemsRecords() throws IOException {
        // Arrange
        InputRecord record1 = TestRecordLoader.loadRecord("4bcc8bff-2de9-50db-86ea-af75a84de228");
        assertNotNull(record1);
        when(classifier.classify(any(InputRecord.class))).thenReturn(new ClassificationResult(WorkType.BOOK_MONOGRAPH, new InstanceClassification(ContentType.TEXT, null, null, null, null, null), List.of(), "test", 1.0, 1));
        when(assignmentService.assign(any(InputRecord.class), anyString())).thenReturn(Assignment.builder().decision(Assignment.Decision.CREATED).clusterId(UUID.randomUUID()).build());

        // Act
        service.ingest(record1);

        // Assert
        verify(assignmentService, times(2)).assign(any(InputRecord.class), anyString());
    }
}
