package gcs.app;

import gcs.app.adapters.PgLineageResolver;
import gcs.core.ids.Ulid;
import gcs.core.lineage.Lineage;
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

import java.util.Collections;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
class LineageControllerTest {

	@Inject
	@Client("/")
	HttpClient client;

	@Inject
	private PgLineageResolver lineageResolver;

  private static final String accessToken = "lineage-test-controller-token";
  private static Authentication auth;

  public LineageControllerTest() {
    if (auth == null) {
      auth = TestStaticTokenValidator.add(accessToken, "test-admin-client", List.of("GCS-ADMIN"));
    }
  }

	@Test
	void testResolve() {
		String clusterId = Ulid.nextUlid();
		Lineage expectedLineage = Lineage.builder()
			.status(Lineage.Status.CURRENT)
			.current(Collections.singletonList(clusterId))
			.cfp("placeholder")
			.synth("placeholder")
			.recentHistory(Collections.emptyList())
			.build();

		when(lineageResolver.resolve(anyString())).thenReturn(expectedLineage);

		Lineage actualLineage = client.toBlocking().retrieve(HttpRequest.GET("/resolve/" + clusterId).bearerAuth(accessToken), Lineage.class);
		
		assertEquals(expectedLineage, actualLineage);
	}

	@MockBean(PgLineageResolver.class)
	PgLineageResolver lineageResolver() {
		return mock(PgLineageResolver.class);
	}

}
