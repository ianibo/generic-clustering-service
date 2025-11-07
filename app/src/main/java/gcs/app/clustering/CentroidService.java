package gcs.app.clustering;

import com.pgvector.PGvector;
public interface CentroidService {
    void updateCentroid(String clusterId, String representation, PGvector memberEmbedding);
}
