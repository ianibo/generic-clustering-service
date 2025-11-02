package gcs.app.clustering;

import gcs.app.esvector.ESIndexStore;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

@Singleton
public class ESClusteringService {

    private final ESIndexStore esIndexStore;

    public ESClusteringService(ESIndexStore esIndexStore) {
        this.esIndexStore = esIndexStore;
    }

    public Optional<ESIndexStore.SearchResult> findClosestMatch(String indexName, float[] vector, String fieldName, double threshold) throws IOException {
        List<ESIndexStore.SearchResult> results = esIndexStore.search(indexName, vector, fieldName, threshold);
        if (results.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(results.get(0));
    }
}
