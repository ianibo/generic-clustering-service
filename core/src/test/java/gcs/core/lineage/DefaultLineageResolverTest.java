package gcs.core.lineage;

import gcs.core.ids.Ulid;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultLineageResolverTest {

    private final DefaultLineageResolver resolver = new DefaultLineageResolver();

    @Test
    void testResolve() {
        String clusterId = Ulid.nextUlid();
        Lineage lineage = resolver.resolve(clusterId);

        assertEquals(Lineage.Status.CURRENT, lineage.getStatus());
        assertEquals(1, lineage.getCurrent().size());
        assertEquals(clusterId, lineage.getCurrent().get(0));
        assertEquals("placeholder", lineage.getCfp());
        assertEquals("placeholder", lineage.getSynth());
        assertEquals(0, lineage.getRecentHistory().size());
    }
}
