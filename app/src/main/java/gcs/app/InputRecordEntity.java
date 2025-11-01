package gcs.app;

import gcs.core.InputRecord;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;

@Entity
@Table(name = "input_record")
public class InputRecordEntity {
    @Id
    private String id;
    @DateCreated
    private Instant dateCreated;
    @DateUpdated
    private Instant dateModified;
    private String extractedResourceType;
    @JdbcTypeCode(SqlTypes.JSON)
    private InputRecord record;
    @Enumerated(EnumType.STRING)
    private ProcessingStatus processingStatus;
    private Integer classifierVersion;

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    public String getExtractedResourceType() {
        return extractedResourceType;
    }

    public void setExtractedResourceType(String extractedResourceType) {
        this.extractedResourceType = extractedResourceType;
    }

    public InputRecord getRecord() {
        return record;
    }

    public void setRecord(InputRecord record) {
        this.record = record;
    }

    public ProcessingStatus getProcessingStatus() {
        return processingStatus;
    }

    public void setProcessingStatus(ProcessingStatus processingStatus) {
        this.processingStatus = processingStatus;
    }

    public Integer getClassifierVersion() {
        return classifierVersion;
    }

    public void setClassifierVersion(Integer classifierVersion) {
        this.classifierVersion = classifierVersion;
    }
}
