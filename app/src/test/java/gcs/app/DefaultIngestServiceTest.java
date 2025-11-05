package gcs.app;

import gcs.app.pgvector.storage.InstanceClusterRepository;
import gcs.app.pgvector.storage.WorkClusterRepository;
import gcs.core.assignment.Assignment;
import gcs.core.assignment.AssignmentService;
import gcs.core.classification.ClassificationResult;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;

import gcs.app.util.TestRecordLoader;

import java.util.List;
import java.util.ArrayList;
import java.util.UUID;
import jakarta.inject.Inject;

import static org.mockito.Mockito.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@MicronautTest
class DefaultIngestServiceTest {

	@Inject
  @Client("/")
  HttpClient client;

  @Inject
	DefaultIngestService service;

  @Inject
    Classifier classifier;

  @Inject
    AssignmentService assignmentService;


    @BeforeEach
    void setUp() {
        when(classifier.classify(any())).thenReturn(ClassificationResult.builder().build());
        when(assignmentService.assign(any(), any(), any())).thenReturn(Assignment.builder().decision(Assignment.Decision.CREATED).clusterId(UUID.randomUUID()).build());
    }

	@Test
	void testIngest() throws java.io.IOException {
		InputRecord record1 = TestRecordLoader.loadRecord("4bcc8bff-2de9-50db-86ea-af75a84de228");
		var result = service.ingest(record1);
		log.info("Result = {}",result);
		assert result != null;
	}

    @MockBean(WorkClusterRepository.class)
    WorkClusterRepository workClusterRepository() {
        return mock(WorkClusterRepository.class);
    }

    @MockBean(InstanceClusterRepository.class)
    InstanceClusterRepository instanceClusterRepository() {
        return mock(InstanceClusterRepository.class);
    }

    @MockBean(Classifier.class)
    Classifier classifier() {
        return mock(Classifier.class);
    }

    @MockBean(AssignmentService.class)
    AssignmentService assignmentService() {
        return mock(AssignmentService.class);
    }
}
