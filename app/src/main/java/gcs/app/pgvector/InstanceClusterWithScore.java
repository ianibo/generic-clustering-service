package gcs.app.pgvector;

import io.micronaut.data.annotation.MappedEntity;

@MappedEntity
public class InstanceClusterWithScore extends InstanceCluster {
    private double score;

    public double getScore() {
        return score;
    }

    public void setScore(double score) {
        this.score = score;
    }
}
