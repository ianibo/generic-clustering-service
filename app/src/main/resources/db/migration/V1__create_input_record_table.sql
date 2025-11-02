CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE input_record (
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    date_created TIMESTAMP NOT NULL default now(),
    date_modified TIMESTAMP NOT NULL,
    extracted_resource_type VARCHAR(255),
    content_type VARCHAR(255),
    media_type VARCHAR(255),
    carrier_type VARCHAR(255),
    record ${json_type} NOT NULL,
    processing_status VARCHAR(255) NOT NULL,
    classifier_version INT DEFAULT 1
);

CREATE TABLE work_cluster (
	id UUID NOT NULL PRIMARY KEY,
  date_created TIMESTAMP NOT NULL default now(),
  date_modified TIMESTAMP NOT NULL,
  status VARCHAR(16) NOT NULL,
	label VARCHAR(256)
);

CREATE TABLE work_cluster_member (
	id UUID NOT NULL PRIMARY KEY,
  date_created TIMESTAMP NOT NULL default now(),
  date_modified TIMESTAMP NOT NULL,
	record_id VARCHAR(255) unique foreign key references input_record,
  cluster_id UUID foreign key references work_cluster,
	score	DOUBLE PRECISION,              -- similarity/affinity at assignment time
	role VARCHAR(16),                    -- 'core'|'bridge'|'noise' if you use it
	enabled			BOOLEAN NOT NULL DEFAULT true,  -- soft toggle on reassignment
	added_reason	TEXT,                  -- audit/human note
	summary VARCHAR(256),
  facts JSONB,
  blocking VECTOR(64) NOT NULL
  embedding VECTOR(1536) NOT NULL
);

CREATE INDEX idx_work_cm_blocking ON work_cluster_member USING hnsw (blocking vector_cosine_ops);
CREATE INDEX idx_work_cm_embedding ON work_cluster_member USING hnsw (embedding vector_cosine_ops);

CREATE TABLE instance_cluster (
  id UUID NOT NULL PRIMARY KEY,
  date_created TIMESTAMP NOT NULL default now(),
  date_modified TIMESTAMP NOT NULL,
  status VARCHAR(16) NOT NULL,
	label VARCHAR(256)
);

CREATE TABLE instance_cluster_member (
	id UUID NOT NULL PRIMARY KEY,
  date_created TIMESTAMP NOT NULL default now(),
  date_modified TIMESTAMP NOT NULL,
	record_id VARCHAR(255) unique foreign key references input_record,
  cluster_id UUID foreign key references instance_cluster
	score	DOUBLE PRECISION,              -- similarity/affinity at assignment time
	role VARCHAR(16),                    -- 'core'|'bridge'|'noise' if you use it
	enabled			BOOLEAN NOT NULL DEFAULT true,  -- soft toggle on reassignment
	added_reason	TEXT,                  -- audit/human note
	summary VARCHAR(256),
  facts JSONB,
  blocking VECTOR(64) NOT NULL
  embedding VECTOR(1536) NOT NULL
);

CREATE INDEX idx_instance_cm_blocking ON work_instance_member USING hnsw (blocking vector_cosine_ops);
CREATE INDEX idx_instance_cm_embedding ON instance_cluster_member USING hnsw (embedding vector_cosine_ops);
