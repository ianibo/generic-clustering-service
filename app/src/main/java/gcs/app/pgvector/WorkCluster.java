package gcs.app.pgvector;

import io.micronaut.data.annotation.MappedEntity;

@MappedEntity("work_cluster")
public class WorkCluster extends Cluster {
    private String label;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
