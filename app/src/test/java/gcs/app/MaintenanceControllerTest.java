package gcs.app;

import gcs.core.consolidation.ConsolidationService;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
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
import static org.mockito.Mockito.verify;

@MicronautTest
class MaintenanceControllerTest {

	@Inject
	@Client("/")
	HttpClient client;

  private static final String accessToken = "maint-controller-test-token";
  private static Authentication auth;

	@Inject
	private ConsolidationService consolidationService;

  public MaintenanceControllerTest() {
    if (auth == null) {
      auth = TestStaticTokenValidator.add(accessToken, "test-admin-client", List.of("GCS-ADMIN"));
    }
  }

	@Test
	void testConsolidate() {
		HttpResponse<String> response = client.toBlocking().exchange(HttpRequest.POST("/maintenance/consolidate", "").bearerAuth(accessToken));
		assertEquals(200, response.getStatus().getCode());
		verify(consolidationService).consolidate();
	}

	@MockBean(gcs.core.consolidation.DefaultConsolidationService.class)
	ConsolidationService consolidationService() {
		return mock(ConsolidationService.class);
	}
}
