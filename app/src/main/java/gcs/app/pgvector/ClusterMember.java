package gcs.app.pgvector;

import com.pgvector.PGvector;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;

import java.time.Instant;
import java.util.UUID;

@MappedEntity
public abstract class ClusterMember {
    @Id
    @GeneratedValue
    private UUID id;

    @DateCreated
    private Instant dateCreated;

    @DateUpdated
    private Instant dateModified;

    private String recordId;

    private UUID clusterId;

    private Double score;

    private String role;

    private boolean enabled = true;

    private String addedReason;

    private String summary;

    @TypeDef(type = DataType.JSON)
    private String facts;

    @TypeDef(type = DataType.STRING, converter = PGvector.class)
    private PGvector blocking;

    @TypeDef(type = DataType.STRING, converter = PGvector.class)
    private PGvector embedding;

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public Instant getDateCreated() {
        return dateCreated;
    }

    public void setDateCreated(Instant dateCreated) {
        this.dateCreated = dateCreated;
    }

    public Instant getDateModified() {
        return dateModified;
    }

    public void setDateModified(Instant dateModified) {
        this.dateModified = dateModified;
    }

    public String getRecordId() {
        return recordId;
    }

    public void setRecordId(String recordId) {
        this.recordId = recordId;
    }

    public UUID getClusterId() {
        return clusterId;
    }

    public void setClusterId(UUID clusterId) {
        this.clusterId = clusterId;
    }

    public Double getScore() {
        return score;
    }

    public void setScore(Double score) {
        this.score = score;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getAddedReason() {
        return addedReason;
    }

    public void setAddedReason(String addedReason) {
        this.addedReason = addedReason;
    }

    public String getSummary() {
        return summary;
    }

    public void setSummary(String summary) {
        this.summary = summary;
    }

    public String getFacts() {
        return facts;
    }

    public void setFacts(String facts) {
        this.facts = facts;
    }

    public PGvector getBlocking() {
        return blocking;
    }

    public void setBlocking(PGvector blocking) {
        this.blocking = blocking;
    }

    public PGvector getEmbedding() {
        return embedding;
    }

    public void setEmbedding(PGvector embedding) {
        this.embedding = embedding;
    }
}
