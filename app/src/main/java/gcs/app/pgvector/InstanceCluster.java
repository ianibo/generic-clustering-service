package gcs.app.pgvector;

import io.micronaut.data.annotation.MappedEntity;

@MappedEntity("instance_cluster")
public class InstanceCluster extends Cluster {
    private String summary;

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }
}
