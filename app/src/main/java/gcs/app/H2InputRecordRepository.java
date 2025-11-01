package gcs.app;

import io.micronaut.data.annotation.Repository;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import io.micronaut.context.annotation.Requires;

import java.util.Optional;

@JdbcRepository(dialect = Dialect.H2)
@Requires(property = "datasources.default.dialect", value = "H2")
public interface H2InputRecordRepository extends InputRecordRepository {
}
