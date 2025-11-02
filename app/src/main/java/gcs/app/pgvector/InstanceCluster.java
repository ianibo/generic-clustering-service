package gcs.app.pgvector;

import io.micronaut.data.annotation.MappedEntity;

import java.util.UUID;

@MappedEntity("instance_cluster")
public class InstanceCluster extends Cluster {
    private String summary;

    public InstanceCluster() {
        super();
    }

    public InstanceCluster(UUID id) {
        super(id);
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
