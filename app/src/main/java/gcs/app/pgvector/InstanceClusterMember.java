package gcs.app.pgvector;

import com.pgvector.PGvector;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.DataType;
import java.util.UUID;
import lombok.*;
import lombok.experimental.Accessors;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

@Builder
@Data
@Accessors(chain = true)
@Serdeable
@NoArgsConstructor
@lombok.EqualsAndHashCode
@lombok.ToString
@MappedEntity("instance_cluster_member")
public class InstanceClusterMember {

  @Id
  @GeneratedValue
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
  private InstanceCluster instanceCluster;

  public void setEmbeddingArr(float[] e) { this.embedding = new PGvector(e); }
  public void setBlockingArr(float[] b) { this.blocking = new PGvector(b); }
}
