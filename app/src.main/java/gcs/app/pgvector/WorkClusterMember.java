package gcs.app.pgvector;

import com.pgvector.PGvector;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;

@MappedEntity("work_cluster_member")
public class WorkClusterMember extends ClusterMember {

    @Relation(value = Relation.Kind.MANY_TO_ONE)
    private WorkCluster workCluster;

    public void setWorkCluster(WorkCluster workCluster) {
        this.workCluster = workCluster;
    }

    public void setBlocking(float[] blocking) {
        super.setBlocking(new PGvector(blocking));
    }

    public void setEmbedding(float[] embedding) {
        super.setEmbedding(new PGvector(embedding));
    }
}
