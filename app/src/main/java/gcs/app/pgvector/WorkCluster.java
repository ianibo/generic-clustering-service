package gcs.app.pgvector;

import io.micronaut.data.annotation.MappedEntity;

import java.util.UUID;

@MappedEntity("work_cluster")
public class WorkCluster extends Cluster {
    private String label;

    public WorkCluster() {
        super();
    }

    public WorkCluster(UUID id) {
        super(id);
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
