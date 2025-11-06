package gcs.app.pgvector;

import com.pgvector.PGvector;
import java.util.UUID;

public interface Cluster {
    UUID getId();
    PGvector getCentroid();
    void setCentroid(PGvector centroid);
    int getMemberCount();
    void setMemberCount(int count);
}
