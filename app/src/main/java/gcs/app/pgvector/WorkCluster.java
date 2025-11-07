package gcs.app.pgvector;

import com.pgvector.PGvector;
import gcs.core.InputRecord;
import gcs.core.lineage.Lineage;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.DataType;
import io.micronaut.data.annotation.MappedProperty;
import io.micronaut.core.annotation.Creator;
import io.micronaut.core.annotation.Nullable;

import java.util.UUID;
import lombok.*;
import lombok.experimental.Accessors;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

@Data
@Builder
@NoArgsConstructor(onConstructor_ = @__(@Creator))
@AllArgsConstructor
@Serdeable
@MappedEntity("work_cluster")
public class WorkCluster implements Cluster {

    @Id
    private UUID id;

    @DateCreated
    private Instant dateCreated;

    @DateUpdated
    private Instant dateModified;

    private String status;

    private String label;

    @TypeDef(type = DataType.JSON)
    private InputRecord syntheticAnchor;

		@Getter(onMethod_ = @TypeDef(type = DataType.OBJECT, converter = PGvectorAttributeConverter.class))
		@Setter(onMethod_ = @TypeDef(type = DataType.OBJECT, converter = PGvectorAttributeConverter.class))
		@MappedProperty(value = "centroid", definition = "VECTOR(1536)")
    private PGvector centroid;

    private String contentFingerprint;

    @TypeDef(type = DataType.JSON)
    @Nullable
    private Lineage lineage;

    private int memberCount;
}
