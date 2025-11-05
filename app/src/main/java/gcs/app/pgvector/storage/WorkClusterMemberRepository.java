package gcs.app.pgvector.storage;

import gcs.app.pgvector.WorkClusterMember;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface WorkClusterMemberRepository extends CrudRepository<WorkClusterMember, UUID> {
    List<WorkClusterMember> findByWorkClusterId(UUID workClusterId);
    long countByWorkClusterId(UUID workClusterId);
    boolean existsByWorkClusterId(UUID workClusterId);
}
