package gcs.app.pgvector.storage;

import com.pgvector.PGvector;
import io.micronaut.data.annotation.Query;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;
import java.util.List;
import gcs.app.pgvector.*;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface WorkClusterRepository extends CrudRepository<WorkCluster, UUID> {

	@Query(value = "SELECT *, 1 - (centroid <=> :embedding) AS score FROM work_cluster ORDER BY centroid <=> :embedding LIMIT :limit", nativeQuery = true)
	List<WorkClusterWithScore> findNearestNeighbors(PGvector embedding, int limit);

}
