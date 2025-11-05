package gcs.app;

import gcs.core.IngestService;
import gcs.core.InputRecord;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
class IngestControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    private IngestService ingestService;

    @Test
    void testIngest() {
        InputRecord record = new InputRecord("1", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord expectedResponse = new InputRecord("1", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 1);

        when(ingestService.ingest(record)).thenReturn(expectedResponse);

        InputRecord actualResponse = client.toBlocking().retrieve(HttpRequest.POST("/ingest", record), InputRecord.class);

        assertEquals(expectedResponse, actualResponse);
    }

    @MockBean(DefaultIngestService.class)
    IngestService ingestService() {
        return mock(IngestService.class);
    }
}
