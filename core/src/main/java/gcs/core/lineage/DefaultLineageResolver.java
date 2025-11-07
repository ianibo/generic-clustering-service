package gcs.core.lineage;

import jakarta.inject.Singleton;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Default implementation of the LineageResolver interface.
 * This implementation is a placeholder and uses an in-memory map to store lineage information.
 */
@Singleton
public class DefaultLineageResolver implements LineageResolver {

    private final Map<String, Lineage> lineageMap = new HashMap<>();

    @Override
    public Lineage resolve(String clusterId) {
        return lineageMap.getOrDefault(clusterId, Lineage.builder()
            .status(Lineage.Status.CURRENT)
            .current(Collections.singletonList(clusterId))
            .cfp("placeholder")
            .synth("placeholder")
            .recentHistory(Collections.emptyList())
            .build());
    }

    public void addLineage(String clusterId, Lineage lineage) {
        lineageMap.put(clusterId, lineage);
    }
}
