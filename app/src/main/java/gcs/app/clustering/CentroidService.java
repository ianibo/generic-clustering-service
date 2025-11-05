package gcs.app.clustering;

import com.pgvector.PGvector;
import java.util.UUID;

public interface CentroidService {
    void updateCentroid(UUID clusterId, String representation, PGvector memberEmbedding);
}
