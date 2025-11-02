package gcs.app.pgvector;

import com.pgvector.PGvector;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Relation;
import io.micronaut.core.annotation.Creator;

import java.util.UUID;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.experimental.Accessors;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.experimental.SuperBuilder;

import io.micronaut.serde.annotation.Serdeable;


import java.util.Map;
import java.util.HashMap;

@Data
@Accessors(chain = true)
@Serdeable
@NoArgsConstructor
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
@lombok.experimental.SuperBuilder
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
