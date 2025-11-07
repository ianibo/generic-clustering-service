package gcs.app.pgvector;

import com.pgvector.PGvector;
import gcs.core.InputRecord;
import gcs.core.lineage.Lineage;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.DataType;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;

import lombok.*;
import lombok.experimental.Accessors;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor(onConstructor_ = @__(@Creator))
@Serdeable
@MappedEntity("instance_cluster")
public class InstanceCluster implements Cluster {

	@Id
	private String id;

	@DateCreated
	private Instant dateCreated;

	@DateUpdated
	private Instant dateModified;

	@Nullable
	private String status;

	@Nullable
	private String label;

	@TypeDef(type = DataType.JSON)
	private InputRecord syntheticAnchor;

	@Getter(onMethod_ = @TypeDef(type = DataType.OBJECT, converter = PGvectorAttributeConverter.class))
	@Setter(onMethod_ = @TypeDef(type = DataType.OBJECT, converter = PGvectorAttributeConverter.class))
	@MappedProperty(value = "centroid", definition = "VECTOR(1536)")
	private PGvector centroid;

	@Nullable
	private String contentFingerprint;

	@TypeDef(type = DataType.JSON)
	@Nullable
	private Lineage lineage;

	private int memberCount;
}
