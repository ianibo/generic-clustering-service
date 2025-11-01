CREATE TABLE input_record (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    date_created TIMESTAMP NOT NULL,
    date_modified TIMESTAMP NOT NULL,
    extracted_resource_type VARCHAR(255),
    record JSONB NOT NULL,
    processing_status VARCHAR(255) NOT NULL,
    classifier_version INT DEFAULT 1
);
