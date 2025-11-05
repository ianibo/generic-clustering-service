package gcs.app.pgvector;

import com.pgvector.PGvector;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface InstanceClusterRepository extends CrudRepository<InstanceCluster, UUID> {
    @Query(value = "SELECT *, 1 - (centroid <=> :embedding) AS score FROM instance_cluster ORDER BY centroid <=> :embedding LIMIT :limit", nativeQuery = true)
    List<InstanceClusterWithScore> findNearestNeighbors(PGvector embedding, int limit);
}
