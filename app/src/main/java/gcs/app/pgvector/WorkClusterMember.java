package gcs.app.pgvector;

import com.pgvector.PGvector;
import io.micronaut.core.annotation.*;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.data.model.DataType;
import io.micronaut.data.annotation.*;
import java.util.UUID;
import lombok.*;
import lombok.experimental.Accessors;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

@Data
@Builder
@Accessors(chain = true)
@Serdeable
@NoArgsConstructor
@AllArgsConstructor
@lombok.EqualsAndHashCode
@lombok.ToString
@MappedEntity("work_cluster_member")
public class WorkClusterMember {

  @Id
  private UUID id;

  @DateCreated
  private Instant dateCreated;

  @DateUpdated
  private Instant dateModified;

	private String recordId;

	private UUID clusterId;

	private Double score;

	private String role;

	@Builder.Default
	private boolean enabled = true;

	private String addedReason;

	private String summary;

	@TypeDef(type = DataType.JSON)
	private String facts;

	private PGvector blocking;

	private PGvector embedding;

	@Relation(value = Relation.Kind.MANY_TO_ONE)
	private WorkCluster workCluster;

	@io.micronaut.data.annotation.Transient
	public void setEmbeddingArr(float[] e) { this.embedding = new PGvector(e); }

	@io.micronaut.data.annotation.Transient
	public void setBlockingArr(float[] b) { this.blocking = new PGvector(b); }
}
