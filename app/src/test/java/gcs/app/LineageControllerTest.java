package gcs.app;

import gcs.app.adapters.PgLineageResolver;
import gcs.core.lineage.Lineage;
import gcs.core.lineage.LineageResolver;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.annotation.MockBean;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@MicronautTest
class LineageControllerTest {

    @Inject
    @Client("/")
    HttpClient client;

    @Inject
    private PgLineageResolver lineageResolver;

    @Test
    void testResolve() {
        UUID clusterId = UUID.randomUUID();
        Lineage expectedLineage = Lineage.builder()
            .status(Lineage.Status.CURRENT)
            .current(Collections.singletonList(clusterId))
            .cfp("placeholder")
            .synth("placeholder")
            .recentHistory(Collections.emptyList())
            .build();

        when(lineageResolver.resolve(any(UUID.class))).thenReturn(expectedLineage);

        Lineage actualLineage = client.toBlocking().retrieve(HttpRequest.GET("/resolve/" + clusterId), Lineage.class);

        assertEquals(expectedLineage, actualLineage);
    }

    @MockBean(PgLineageResolver.class)
    PgLineageResolver lineageResolver() {
        return mock(PgLineageResolver.class);
    }
}
