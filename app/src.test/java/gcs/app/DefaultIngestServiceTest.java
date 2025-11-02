package gcs.app;

import gcs.app.clustering.BlockingRandomProjector;
import gcs.app.clustering.ESClusteringService;
import gcs.app.esvector.ESIndexStore;
import gcs.app.pgvector.storage.PGVectorStore;
import gcs.core.canonicalization.Canonicalizer;
import gcs.core.classification.Classifier;
import gcs.core.classification.ClassificationResult;
import gcs.core.classification.InstanceClassification;
import gcs.core.classification.ContentType;
import gcs.core.classification.MediaType;
import gcs.core.classification.CarrierType;
import gcs.core.EmbeddingService;
import gcs.core.InputRecord;
import gcs.core.classification.WorkType;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.ArrayList;

import static org.mockito.Mockito.*;

class DefaultIngestServiceTest {
    @Test
    void testIngest() {
        // Arrange
        var embeddingService = mock(EmbeddingService.class);
        var defaultCanonicalizer = mock(Canonicalizer.class);
        when(defaultCanonicalizer.forContentType()).thenReturn(null);
        var textCanonicalizer = mock(Canonicalizer.class);
        when(textCanonicalizer.forContentType()).thenReturn("TEXT");
        var canonicalizers = List.of(defaultCanonicalizer, textCanonicalizer);
        var classifier = mock(Classifier.class);
        var projector = mock(BlockingRandomProjector.class);
        var clusteringService = mock(ESClusteringService.class);
        var pgVectorStore = mock(PGVectorStore.class);
        var esIndexStore = mock(ESIndexStore.class);

        var service = new DefaultIngestService(embeddingService, canonicalizers, classifier, projector, clusteringService, pgVectorStore, esIndexStore);

        var physical = new InputRecord.Physical("extent", "dimensions", "TEXT", "UNMEDIATED", "VOLUME", "format");
        var record = new InputRecord("rec-001", null, null, null, null, null, null, null, null, null, physical, null, null, null, null, null, null, null, null, null, null);
        var summary = "test summary";
        var embedding = new float[]{1.0f, 2.0f, 3.0f};
        List<String> evidence = new ArrayList<String>();
        var instanceClassification = new InstanceClassification(ContentType.TEXT,MediaType.UNMEDIATED,CarrierType.VOLUME,null,null,null);
        var source = "";

        var classificationResult = new ClassificationResult(WorkType.BOOK_MONOGRAPH, instanceClassification, evidence, source, 0.5, 1);

        var versionedRecord = new InputRecord("rec-001", null, null, null, null, null, null, null, null, null, physical, null, null, null, null, null, null, null, null, null, 1);

        when(classifier.classify(record)).thenReturn(classificationResult);
        when(textCanonicalizer.summarize(versionedRecord, Canonicalizer.Intent.WORK)).thenReturn(summary);
        when(embeddingService.embed(summary)).thenReturn(embedding);


        // Act
        service.ingest(record);

        // Assert
        verify(classifier).classify(record);
    }
}
