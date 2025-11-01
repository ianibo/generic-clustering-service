CREATE TABLE input_record (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    dateCreated TIMESTAMP NOT NULL,
    dateModified TIMESTAMP NOT NULL,
    extractedResourceType VARCHAR(255),
    record ${json_type} NOT NULL,
    processingStatus VARCHAR(255) NOT NULL,
    classifierVersion INT DEFAULT 1,
    classificationResult ${json_type}
);
