package gcs.app.pgvector;

import io.micronaut.data.annotation.*;
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
@AllArgsConstructor
@lombok.EqualsAndHashCode
@lombok.ToString
@MappedEntity("instance_cluster_member")
public class InstanceClusterMember {

  @Id
  private UUID id;

  @DateCreated
  private Instant dateCreated;

  @DateUpdated
  private Instant dateModified;

  private String recordId;

  private Double score;

  private String role;

  @Builder.Default
  private boolean enabled = true;

  private String addedReason;

  private String summary;

  @TypeDef(type = DataType.JSON)
  private String facts;

	@Getter(onMethod_ = @TypeDef(type = DataType.OBJECT, converter = PGvectorAttributeConverter.class))
	@Setter(onMethod_ = @TypeDef(type = DataType.OBJECT, converter = PGvectorAttributeConverter.class))
	@MappedProperty(value = "blocking", definition = "VECTOR(64)")
  private PGvector blocking;

	@Getter(onMethod_ = @TypeDef(type = DataType.OBJECT, converter = PGvectorAttributeConverter.class))
	@Setter(onMethod_ = @TypeDef(type = DataType.OBJECT, converter = PGvectorAttributeConverter.class))
	@MappedProperty(value = "embedding", definition = "VECTOR(1536)")
  private PGvector embedding;

  @Relation(value = Relation.Kind.MANY_TO_ONE)
  private InstanceCluster instanceCluster;

	@io.micronaut.data.annotation.Transient
  public void setEmbeddingArr(float[] e) { this.embedding = new PGvector(e); }

	@io.micronaut.data.annotation.Transient
  public void setBlockingArr(float[] b) { this.blocking = new PGvector(b); }
}
