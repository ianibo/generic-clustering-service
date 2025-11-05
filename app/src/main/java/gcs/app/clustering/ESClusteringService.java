package gcs.app.clustering;

import gcs.app.esvector.ESIndexStore;
import jakarta.inject.Singleton;
import java.io.IOException;
import java.util.List;
import java.util.Optional;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Singleton
public class ESClusteringService {

    private final ESIndexStore esIndexStore;

    public ESClusteringService(ESIndexStore esIndexStore) {
        this.esIndexStore = esIndexStore;
    }

	public Optional<ESIndexStore.SearchResult> findClosestMatch(String indexName, float[] vector, String fieldName, double threshold) throws IOException {

		log.info("findClosestMatch({},....)",indexName);

		List<ESIndexStore.SearchResult> results = esIndexStore.search(indexName, vector, fieldName, threshold, 10, Optional.empty(), List.of());

		if (results.isEmpty()) {
			return Optional.empty();
		}

		return Optional.of(results.get(0));
	}
}
