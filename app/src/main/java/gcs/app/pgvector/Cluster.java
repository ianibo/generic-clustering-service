package gcs.app.pgvector;

import com.pgvector.PGvector;
public interface Cluster {
    String getId();
    PGvector getCentroid();
    void setCentroid(PGvector centroid);
    int getMemberCount();
    void setMemberCount(int count);
}
