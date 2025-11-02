CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE input_record (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    date_created TIMESTAMP NOT NULL default now(),
    date_modified TIMESTAMP NOT NULL default now(),
    extracted_resource_type VARCHAR(255),
    content_type VARCHAR(255),
    media_type VARCHAR(255),
    carrier_type VARCHAR(255),
    record JSONB NOT NULL,
    processing_status VARCHAR(255) NOT NULL,
    classifier_version INT DEFAULT 1
);

CREATE TABLE work_cluster (
	id UUID NOT NULL PRIMARY KEY,
  date_created TIMESTAMP NOT NULL default now(),
  date_modified TIMESTAMP NOT NULL default now(),
  status VARCHAR(16) NOT NULL,
	label VARCHAR(256)
);

CREATE TABLE work_cluster_member (
	id UUID NOT NULL PRIMARY KEY,
  date_created TIMESTAMP NOT NULL default now(),
  date_modified TIMESTAMP NOT NULL default now(),
	record_id VARCHAR(255),
  cluster_id UUID,
	score	DOUBLE PRECISION,
	role VARCHAR(16),
	enabled	BOOLEAN NOT NULL DEFAULT true,
	added_reason	TEXT,
	summary VARCHAR(256),
  facts JSONB,
  blocking VECTOR(64) NOT NULL,
  embedding VECTOR(1536) NOT NULL,
  CONSTRAINT fk_wcm_record FOREIGN KEY (record_id) REFERENCES input_record(id) ON DELETE CASCADE,
  CONSTRAINT fk_wcm_cluster FOREIGN KEY (cluster_id) REFERENCES work_cluster(id) ON DELETE CASCADE
);

CREATE INDEX idx_work_cm_blocking ON work_cluster_member USING hnsw (blocking vector_cosine_ops);
CREATE INDEX idx_work_cm_embedding ON work_cluster_member USING hnsw (embedding vector_cosine_ops);

CREATE TABLE instance_cluster (
  id UUID NOT NULL PRIMARY KEY,
  date_created TIMESTAMP NOT NULL default now(),
  date_modified TIMESTAMP NOT NULL default now(),
  status VARCHAR(16) NOT NULL,
	label VARCHAR(256)
);

CREATE TABLE instance_cluster_member (
	id UUID NOT NULL PRIMARY KEY,
  date_created TIMESTAMP NOT NULL default now(),
  date_modified TIMESTAMP NOT NULL default now(),
	record_id VARCHAR(255),
  cluster_id UUID,
	score	DOUBLE PRECISION,
	role VARCHAR(16),
	enabled	BOOLEAN NOT NULL DEFAULT true,
	added_reason TEXT,
	summary VARCHAR(256),
  facts JSONB,
  blocking VECTOR(64) NOT NULL,
  embedding VECTOR(1536) NOT NULL,
  CONSTRAINT fk_icm_record FOREIGN KEY (record_id) REFERENCES input_record(id) ON DELETE CASCADE,
  CONSTRAINT fk_icm_cluster FOREIGN KEY (cluster_id) REFERENCES instance_cluster(id) ON DELETE CASCADE
);

CREATE INDEX idx_instance_cm_blocking ON work_cluster_member USING hnsw (blocking vector_cosine_ops);
CREATE INDEX idx_instance_cm_embedding ON instance_cluster_member USING hnsw (embedding vector_cosine_ops);
