package gcs.app;

import gcs.core.InputRecord;
import gcs.core.classification.Classifier;
import gcs.core.classification.ClassificationResult;
import gcs.core.classification.InstanceClassification;
import gcs.core.classification.ContentType;
import gcs.core.classification.MediaType;
import gcs.core.classification.CarrierType;
import gcs.core.classification.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class ClassificationTest {

    private ObjectMapper objectMapper;
    private DefaultIngestService service;
    private InputRecordRepository inputRecordRepository;
    private Classifier classifier;

    @BeforeEach
    void setup() {
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
        inputRecordRepository = mock(InputRecordRepository.class);
        classifier = new gcs.core.classification.RuleBasedClassifier(new gcs.core.classification.RuleBasedInstanceClassifier());
        service = new DefaultIngestService(
                mock(gcs.core.EmbeddingService.class),
                mock(gcs.core.VectorIndex.class),
                mock(gcs.core.Canonicalizer.class),
                mock(gcs.core.Calibration.class),
                inputRecordRepository,
                classifier
        );
    }

    private InputRecord loadRecord(String fileName) throws IOException {
        try (var in = getClass().getResourceAsStream("/testrecs/cs00000002m001/" + fileName)) {
            return objectMapper.readValue(in, InputRecord.class);
        }
    }

    @Test
    void testCollectedPoemsRecords() throws IOException {
        // Arrange
        InputRecord record1 = loadRecord("4bcc8bff-2de9-50db-86ea-af75a84de228");
        InputRecord record2 = loadRecord("4f4e511a-a03d-57b1-8132-6ce7e1617c38");
        InputRecord record3 = loadRecord("4f5de229-7df0-5f76-af84-03326e132f4a");
        InputRecord record4 = loadRecord("5161586e-04ea-53f2-b94c-8a29e9d05625");
        InputRecord record5 = loadRecord("657cdce5-66ab-58d1-9a36-42dc96229dca");

        // Act
        service.ingest(record1);
        service.ingest(record2);
        service.ingest(record3);
        service.ingest(record4);
        service.ingest(record5);

        // Assert
        var captor = ArgumentCaptor.forClass(InputRecordEntity.class);
        verify(inputRecordRepository, Mockito.times(5)).save(captor.capture());
        List<InputRecordEntity> savedEntities = captor.getAllValues();

        assertEquals("text", savedEntities.get(0).getRecord().physical().contentType());
        assertEquals("unmediated", savedEntities.get(0).getRecord().physical().mediaType());
        assertEquals("volume", savedEntities.get(0).getRecord().physical().carrierType());

        assertEquals(null, savedEntities.get(1).getRecord().physical().contentType());
        assertEquals(null, savedEntities.get(1).getRecord().physical().mediaType());
        assertEquals(null, savedEntities.get(1).getRecord().physical().carrierType());

        assertEquals(null, savedEntities.get(2).getRecord().physical().contentType());
        assertEquals(null, savedEntities.get(2).getRecord().physical().mediaType());
        assertEquals(null, savedEntities.get(2).getRecord().physical().carrierType());

        assertEquals("text", savedEntities.get(3).getRecord().physical().contentType());
        assertEquals("unmediated", savedEntities.get(3).getRecord().physical().mediaType());
        assertEquals("volume", savedEntities.get(3).getRecord().physical().carrierType());

        assertEquals(null, savedEntities.get(4).getRecord().physical().contentType());
        assertEquals(null, savedEntities.get(4).getRecord().physical().mediaType());
        assertEquals(null, savedEntities.get(4).getRecord().physical().carrierType());
    }

    @Test
    void testFaustRecord() throws IOException {
        // Arrange
        InputRecord record = loadRecord("c61d904c-bad1-5160-a510-9a0eeba4f9bf");

        // Act
        service.ingest(record);

        // Assert
        var captor = ArgumentCaptor.forClass(InputRecordEntity.class);
        verify(inputRecordRepository).save(captor.capture());
        InputRecordEntity savedEntity = captor.getValue();

        assertEquals("text", savedEntity.getRecord().physical().contentType());
        assertEquals("unmediated", savedEntity.getRecord().physical().mediaType());
        assertEquals("volume", savedEntity.getRecord().physical().carrierType());
    }
}
