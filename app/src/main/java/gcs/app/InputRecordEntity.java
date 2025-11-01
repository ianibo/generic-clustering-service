package gcs.app;

import gcs.core.InputRecord;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;

import java.time.Instant;

import io.micronaut.core.annotation.Creator;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.Transient;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import io.micronaut.serde.annotation.Serdeable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.HashMap;

@Builder
@Data
@NoArgsConstructor(onConstructor_ = @Creator())
@AllArgsConstructor
@Accessors(chain = true)
@ToString(onlyExplicitlyIncluded = false)
@MappedEntity(value = "mt_tenant_info")
@Serdeable
public class InputRecordEntity {
    @Id
    private String id;
    @DateCreated
    private Instant dateCreated;
    @DateUpdated
    private Instant dateModified;
    private String extractedResourceType;

    @TypeDef(type = DataType.JSON)
    private InputRecord record;

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
