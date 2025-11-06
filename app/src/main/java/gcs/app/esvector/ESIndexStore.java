package gcs.app.esvector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.elasticsearch._types.HealthStatus;
import co.elastic.clients.elasticsearch._types.WaitForActiveShards;
import co.elastic.clients.elasticsearch.indices.CreateIndexResponse;
import gcs.app.pgvector.InstanceClusterMember;
import gcs.app.pgvector.WorkClusterMember;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import co.elastic.clients.elasticsearch.core.search.FieldSuggester;
import co.elastic.clients.elasticsearch.core.search.Suggester;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.HashMap;
import java.util.Objects;
import java.util.stream.Collectors;
import co.elastic.clients.elasticsearch._types.mapping.DenseVectorSimilarity;
import jakarta.inject.Named;

@Singleton
@Named("es")
public class ESIndexStore {
    private static final Logger log = LoggerFactory.getLogger(ESIndexStore.class);
    private final ElasticsearchClient esClient;

    public record SearchResult(String id, double score) {}

    public ESIndexStore(ElasticsearchClient esClient) {
        this.esClient = esClient;
    }

	/**
	 * Create an Elasticsearch index if it doesn't already exist.
	 *
	 * @param indexName The name of the index.
	 * @param mapping   The mapping for the index.
	 * @throws IOException If an I/O error occurs.
	 */
	public void createIndex(String indexName, String mapping) throws IOException {

		boolean exists = esClient.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();

		if (exists) {
			log.info("Index [{}] already exists.", indexName);
			return;
		}

		log.info("Index [{}] does not exist. Creating...", indexName);

		esClient.indices().create(c -> c
			.index(indexName)
			.withJson(new java.io.StringReader(mapping))
		);

		log.info("Index [{}] created.", indexName);

		// 2) Wait until the index is searchable (primary allocated)
		esClient.cluster().health(h -> h
			.index(indexName)
			.waitForStatus(HealthStatus.Yellow)          // OK for single node w/ 0 replicas
			.waitForNoInitializingShards(true)           // ensures allocation finished
			.timeout(t -> t.time("30s"))
		);

		log.info("Completed wait for index present");
	}

    /**
     * Store a WorkClusterMember in the vector store.
     *
     * @param indexName The name of the index.
     * @param member    The ClusterMember to store.
     * @throws IOException If an I/O error occurs.
     */
    public void store(String indexName, Map<String,Object> esrec) throws IOException {

        log.info("Storing record in index {}: {}", indexName, esrec);

        Object clusterIdObj = esrec.get("clusterId");
        if (clusterIdObj == null) {
            throw new IllegalArgumentException("The 'clusterId' key is missing or null in the record map.");
        }
        String clusterId = clusterIdObj.toString();

        IndexRequest<Map<String,Object>> request = IndexRequest.of(i -> i
            .index(indexName)
            .id(clusterId)
            .document(esrec)
        );
        esClient.index(request);
    }

    /**
     * Search for similar vectors in the index.
     *
     * @param indexName The name of the index.
     * @param vector    The vector to search for.
     * @param fieldName The name of the vector field.
     * @param threshold The similarity threshold.
     * @param k         The number of results to return.
     * @param filters   Optional filters to apply to the search.
     * @return A list of search results.
     * @throws IOException If an I/O error occurs.
     */
	public List<SearchResult> search(String indexName, float[] vector, String fieldName, double threshold, int k, Optional<Map<String, String>> filters, List<UUID> candidateIds) throws IOException {

		List<Float> queryVector = new ArrayList<>(vector.length);
		for (float v : vector) {
			queryVector.add(v);
		}

		SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder()
            .index(indexName)
            .knn(knn -> {
                knn.field(fieldName)
                    .queryVector(queryVector)
                    .k(k)
                    .numCandidates(50);
                if (candidateIds != null && !candidateIds.isEmpty()) {
                    knn.filter(f -> f.ids(i -> i.values(candidateIds.stream().map(UUID::toString).collect(Collectors.toList()))));
                }
                return knn;
            })
            .minScore(threshold); // Filter by score

        filters.ifPresent(f -> {
            // This is a simplified filter implementation. A real implementation would need to handle different types of filters.
            f.forEach((key, value) -> {
                searchRequestBuilder.query(q -> q.term(t -> t.field(key).value(v -> v.stringValue(value))));
            });
        });

		try {
			SearchResponse<Void> searchResponse = esClient.search(searchRequestBuilder.build(), Void.class);

			return searchResponse.hits().hits().stream()
				.map(hit -> {
					if (hit.score() == null) {
						return null;
					}
					return new SearchResult(hit.id(), hit.score());
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		}
		catch ( co.elastic.clients.elasticsearch._types.ElasticsearchException e) {
			// New Java API client wraps the server’s error – print it
			var r = e.response();
			log.error("ES error: " + (r != null ? r.error() : e.getMessage()), e);
			throw e;
		}
	}
}
