package gcs.app;

import gcs.app.clustering.BlockingRandomProjector;
import gcs.app.clustering.ESClusteringService;
import gcs.app.esvector.ESIndexStore;
import gcs.app.pgvector.InstanceCluster;
import gcs.app.pgvector.InstanceClusterMember;
import gcs.app.pgvector.WorkCluster;
import gcs.app.pgvector.WorkClusterMember;
import gcs.app.pgvector.storage.PGVectorStore;
import gcs.core.InputRecord;
import gcs.core.classification.Classifier;
import gcs.core.canonicalization.Canonicalizer;
import jakarta.inject.Named;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

import gcs.app.util.TestRecordLoader;

class ClassificationTest {

    private DefaultIngestService service;
    private PGVectorStore pgVectorStore;
    private ESIndexStore esIndexStore;


    @BeforeEach
    void setup() {
        var classifier = new gcs.core.classification.RuleBasedClassifier(new gcs.core.classification.RuleBasedInstanceClassifier());
        var defaultCanonicalizer = mock(Canonicalizer.class);
        when(defaultCanonicalizer.forContentType()).thenReturn(null);
        var textCanonicalizer = mock(Canonicalizer.class);
        when(textCanonicalizer.forContentType()).thenReturn("TEXT");
        var canonicalizers = List.of(defaultCanonicalizer, textCanonicalizer);
        pgVectorStore = mock(PGVectorStore.class);
        esIndexStore = mock(ESIndexStore.class);

        when(pgVectorStore.saveWorkCluster(any(WorkCluster.class))).thenReturn(new WorkCluster(UUID.randomUUID()));
        when(pgVectorStore.saveInstanceCluster(any(InstanceCluster.class))).thenReturn(new InstanceCluster(UUID.randomUUID()));

        service = new DefaultIngestService(
                mock(gcs.core.EmbeddingService.class),
                canonicalizers,
                classifier,
                mock(BlockingRandomProjector.class),
                mock(ESClusteringService.class),
                pgVectorStore,
                esIndexStore
        );
    }

    @Test
    void testCollectedPoemsRecords() throws IOException {
        // Arrange
        InputRecord record1 = TestRecordLoader.loadRecord("4bcc8bff-2de9-50db-86ea-af75a84de228");
        InputRecord record2 = TestRecordLoader.loadRecord("4f4e511a-a03d-57b1-8132-6ce7e1617c38");
        InputRecord record3 = TestRecordLoader.loadRecord("4f5de229-7df0-5f76-af84-03326e132f4a");
        InputRecord record4 = TestRecordLoader.loadRecord("5161586e-04ea-53f2-b94c-8a29e9d05625");
        InputRecord record5 = TestRecordLoader.loadRecord("657cdce5-66ab-58d1-9a36-42dc96229dca");

        // Act
        service.ingest(record1);
        service.ingest(record2);
        service.ingest(record3);
        service.ingest(record4);
        service.ingest(record5);

        // Assert
        var workCaptor = ArgumentCaptor.forClass(WorkClusterMember.class);
        var instanceCaptor = ArgumentCaptor.forClass(InstanceClusterMember.class);
        verify(pgVectorStore, times(5)).saveWorkClusterMember(workCaptor.capture());
        verify(pgVectorStore, times(5)).saveInstanceClusterMember(instanceCaptor.capture());
    }

    @Test
    void testFaustRecord() throws IOException {
        // Arrange
        InputRecord record = TestRecordLoader.loadRecord("c61d904c-bad1-5160-a510-9a0eeba4f9bf");

        // Act
        service.ingest(record);

        // Assert
        var workCaptor = ArgumentCaptor.forClass(WorkClusterMember.class);
        var instanceCaptor = ArgumentCaptor.forClass(InstanceClusterMember.class);
        verify(pgVectorStore).saveWorkClusterMember(workCaptor.capture());
        verify(pgVectorStore).saveInstanceClusterMember(instanceCaptor.capture());
    }
}
