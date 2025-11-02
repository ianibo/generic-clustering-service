package gcs.app.pgvector;

import com.pgvector.PGvector;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;

@MappedEntity("instance_cluster_member")
public class InstanceClusterMember extends ClusterMember {

    @Relation(value = Relation.Kind.MANY_TO_ONE)
    private InstanceCluster instanceCluster;

    public void setInstanceCluster(InstanceCluster instanceCluster) {
        this.instanceCluster = instanceCluster;
    }

    public void setBlocking(float[] blocking) {
        super.setBlocking(new PGvector(blocking));
    }

    public void setEmbedding(float[] embedding) {
        super.setEmbedding(new PGvector(embedding));
    }
}
