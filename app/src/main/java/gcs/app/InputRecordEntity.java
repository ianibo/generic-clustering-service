package gcs.app;

import gcs.core.InputRecord;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;

import java.time.Instant;

import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;
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
@AllArgsConstructor
@NoArgsConstructor(onConstructor_ = @__(@Creator))
@Accessors(chain = true)
@ToString(onlyExplicitlyIncluded = false)
@MappedEntity(value = "input_record")
@Serdeable
public class InputRecordEntity {
    @Id
    private String id;
    @DateCreated
    private Instant dateCreated;
    @DateUpdated
    private Instant dateModified;
    @Nullable
    private String extractedResourceType;
    @Nullable
    private String contentType;
    @Nullable
    private String mediaType;
    @Nullable
    private String carrierType;

    @TypeDef(type = DataType.JSON)
    private InputRecord record;

    private ProcessingStatus processingStatus;
    @Nullable
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

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getMediaType() {
        return mediaType;
    }

    public void setMediaType(String mediaType) {
        this.mediaType = mediaType;
    }

    public String getCarrierType() {
        return carrierType;
    }

    public void setCarrierType(String carrierType) {
        this.carrierType = carrierType;
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
