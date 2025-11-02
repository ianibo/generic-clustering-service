package gcs.app.pgvector.storage;

import gcs.app.pgvector.WorkCluster;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface WorkClusterRepository extends CrudRepository<WorkCluster, UUID> {
}
