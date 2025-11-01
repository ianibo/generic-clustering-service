package gcs.app;

import gcs.core.IngestService;
import gcs.core.InputRecord;
import gcs.core.classification.ClassificationResult;
import gcs.core.classification.Classifier;
import gcs.core.classification.WorkType;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@MicronautTest(startApplication = false)
class ClusteringPerformanceTest {

    @Inject
    ObjectMapper objectMapper;

    @Inject
    DefaultIngestService ingestService;

    @Inject
    InputRecordRepository inputRecordRepository;

    @Inject
    Classifier classifier;

    @BeforeEach
    void setUp() {
        when(classifier.classify(any(InputRecord.class))).thenReturn(new ClassificationResult(WorkType.BOOK_MONOGRAPH, null, Collections.emptyList(), "test", 1.0, 1));
    }

    private void processRecords(List<String> paths, Consumer<List<IngestService.Candidate>> resultConsumer) {
        paths.stream()
                .map(path -> {
                    try {
                        return loadRecord(path);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                })
                .forEach(record -> {
                    var result = ingestService.ingest(record);
                    assertNotNull(result);
                    resultConsumer.accept(result);
                });
    }

    private InputRecord loadRecord(String path) throws IOException {
        Path recordPath = Paths.get(path);
        String content = Files.readString(recordPath);
        return objectMapper.readValue(content, InputRecord.class);
    }

    @Test
    void collectedPoemsTest() {
        List<String> collectedPoemsPaths = List.of(
                "src/test/resources/testrecs/cs00000002m001/4bcc8bff-2de9-50db-86ea-af75a84de228",
                "src/test/resources/testrecs/cs00000002m001/4f4e511a-a03d-57b1-8132-6ce7e1617c38",
                "src/test/resources/testrecs/cs00000002m001/4f5de229-7df0-5f76-af84-03326e132f4a",
                "src/test/resources/testrecs/cs00000002m001/5161586e-04ea-53f2-b94c-8a29e9d05625",
                "src/test/resources/testrecs/cs00000002m001/657cdce5-66ab-58d1-9a36-42dc96229dca"
        );

        processRecords(collectedPoemsPaths, result -> {
            InputRecordEntity recordEntity = inputRecordRepository.findById(result.get(0).id()).orElseThrow();
            assertEquals(WorkType.BOOK_MONOGRAPH, recordEntity.getClassificationResult().workType());
            assertEquals("text", recordEntity.getRecord().physical().contentType());
            assertEquals("unmediated", recordEntity.getRecord().physical().mediaType());
            assertEquals("volume", recordEntity.getRecord().physical().carrierType());
        });
    }

    @Test
    void faustTest() {
        List<String> faustPath = List.of("src/test/resources/testrecs/cs00000002m001/c61d904c-bad1-5160-a510-9a0eeba4f9bf");

        processRecords(faustPath, result -> {
            InputRecordEntity recordEntity = inputRecordRepository.findById(result.get(0).id()).orElseThrow();
            assertEquals(WorkType.BOOK_MONOGRAPH, recordEntity.getClassificationResult().workType());
            assertEquals("text", recordEntity.getRecord().physical().contentType());
            assertEquals("unmediated", recordEntity.getRecord().physical().mediaType());
            assertEquals("volume", recordEntity.getRecord().physical().carrierType());
        });
    }
}
