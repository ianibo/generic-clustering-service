package gcs.app.bootstrap;

import gcs.app.esvector.ESIndexStore;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.context.event.StartupEvent;
import jakarta.inject.Singleton;

/**
 * A component that bootstraps the Elasticsearch indices on application startup.
 */
@Singleton
public class ElasticsearchBootstrap implements ApplicationEventListener<StartupEvent> {

    private final ESIndexStore esIndexStore;

    public ElasticsearchBootstrap(ESIndexStore esIndexStore) {
        this.esIndexStore = esIndexStore;
    }

    @Override
    public void onApplicationEvent(StartupEvent event) {
        try {
            esIndexStore.createIndex("anchors-work", getWorkMapping());
            esIndexStore.createIndex("anchors-instance", getInstanceMapping());
        } catch (java.io.IOException e) {
            throw new RuntimeException("Failed to create Elasticsearch indices", e);
        }
    }

    private String getWorkMapping() {
        return """
            {
              "mappings": {
                "properties": {
                  "clusterId": { "type": "keyword" },
                  "representation": { "type": "keyword" },
                  "blocking": {
                    "type": "dense_vector",
                    "dims": 64,
                    "index": true,
                    "similarity": "cosine"
                  },
                  "embedding": {
                    "type": "dense_vector",
                    "dims": 1536,
                    "index": true,
                    "similarity": "cosine"
                  }
                }
              }
            }
            """;
    }

    private String getInstanceMapping() {
        return """
            {
              "mappings": {
                "properties": {
                  "clusterId": { "type": "keyword" },
                  "representation": { "type": "keyword" },
                  "blocking": {
                    "type": "dense_vector",
                    "dims": 64,
                    "index": true,
                    "similarity": "cosine"
                  },
                  "embedding": {
                    "type": "dense_vector",
                    "dims": 1536,
                    "index": true,
                    "similarity": "cosine"
                  }
                }
              }
            }
            """;
    }
}
