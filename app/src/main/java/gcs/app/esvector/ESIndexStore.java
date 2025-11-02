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
import gcs.app.pgvector.ClusterMember;
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
	 * Get or create an Elasticsearch index.
	 *
	 * @param indexName The name of the index.
	 * @throws IOException If an I/O error occurs.
	 */
	public void getOrCreate(String indexName) throws IOException {

		boolean exists = esClient.indices().exists(ExistsRequest.of(e -> e.index(indexName))).value();

		if (exists) {
			log.info("Index [{}] already exists.", indexName);
			return;
		}

		log.info("Index [{}] does not exist. Creating...", indexName);
		// Note: Dimensions are assumed. 128 for blocking, 1536 for embedding.
		esClient.indices().create(new CreateIndexRequest.Builder()
			.index(indexName)
			.mappings(m -> m
				.properties("blocking", p -> p
					.denseVector(d -> d
                        .dims(64)
                        .similarity(DenseVectorSimilarity.Cosine) // Use cosine similarity
                        .index(true)
					)
				)
				.properties("embedding", p -> p
					.denseVector(d -> d
						.dims(1536)
						.similarity(DenseVectorSimilarity.Cosine) // Use cosine similarity
						.index(true)
					)
				)
			)
			.build());

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
     * Store a ClusterMember in the vector store.
     *
     * @param indexName The name of the index.
     * @param member    The ClusterMember to store.
     * @throws IOException If an I/O error occurs.
     */
    public void store(String indexName, ClusterMember member) throws IOException {

			log.info("Store({},{})",indexName,member);

        IndexRequest<ClusterMember> request = IndexRequest.of(i -> i
            .index(indexName)
            .id(member.getId().toString())
            .document(member)
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
     * @return A list of search results.
     * @throws IOException If an I/O error occurs.
     */
	public List<SearchResult> search(String indexName, float[] vector, String fieldName, double threshold) throws IOException {

		List<Float> queryVector = new ArrayList<>(vector.length);
		for (float v : vector) {
			queryVector.add(v);
		}

		SearchRequest searchRequest = new SearchRequest.Builder()
            .index(indexName)
            .knn(k -> k
                .field(fieldName)
                .queryVector(queryVector)
                .k(10)
                .numCandidates(50)
            )
            .minScore(threshold) // Filter by score
            .build();

		try {
			SearchResponse<Void> searchResponse = esClient.search(searchRequest, Void.class);

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
