package gcs.app.esvector;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.IndexRequest;
import co.elastic.clients.elasticsearch.indices.CreateIndexRequest;
import co.elastic.clients.elasticsearch.indices.ExistsRequest;
import gcs.app.pgvector.ClusterMember;
import io.micronaut.context.annotation.Bean;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.UUID;

@Singleton
public class ESVectorStore {
    private static final Logger log = LoggerFactory.getLogger(ESVectorStore.class);
    private final ElasticsearchClient esClient;

    public ESVectorStore(ElasticsearchClient esClient) {
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
                        .dims(128)
                    )
                )
                .properties("embedding", p -> p
                    .denseVector(d -> d
                        .dims(1536)
                    )
                )
            )
            .build());
        log.info("Index [{}] created.", indexName);
    }

    /**
     * Store a ClusterMember in the vector store.
     *
     * @param indexName The name of the index.
     * @param member    The ClusterMember to store.
     * @throws IOException If an I/O error occurs.
     */
    public void store(String indexName, ClusterMember member) throws IOException {
        IndexRequest<ClusterMember> request = IndexRequest.of(i -> i
            .index(indexName)
            .id(member.getId().toString())
            .document(member)
        );
        esClient.index(request);
    }
}
