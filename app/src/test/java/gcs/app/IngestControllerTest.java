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
import io.micronaut.security.authentication.Authentication;
import gcs.app.security.TestStaticTokenValidator;
import java.util.List;

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

  private static final String accessToken = "test-admin-client-token";
  private static Authentication auth;

	public IngestControllerTest() {
    if (auth == null) {
      auth = TestStaticTokenValidator.add(accessToken, "test-admin-client", List.of("GCS-ADMIN"));
    }
	}


	@Test
	void testIngest() {
		InputRecord record = new InputRecord("1", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
		InputRecord expectedResponse = new InputRecord("1", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, 1, null);

		when(ingestService.ingest(record)).thenReturn(expectedResponse);

		InputRecord actualResponse = client.toBlocking().retrieve(HttpRequest.POST("/ingest", record).bearerAuth(accessToken), InputRecord.class);

		assertEquals(expectedResponse, actualResponse);
	}

	@MockBean(DefaultIngestService.class)
	IngestService ingestService() {
		return mock(IngestService.class);
	}
}
