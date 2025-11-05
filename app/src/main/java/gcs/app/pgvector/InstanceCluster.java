package gcs.app.pgvector;

import com.pgvector.PGvector;
import gcs.core.InputRecord;
import gcs.core.lineage.Lineage;
import io.micronaut.data.annotation.*;
import io.micronaut.data.model.DataType;

import java.util.UUID;
import lombok.*;
import lombok.experimental.Accessors;
import io.micronaut.serde.annotation.Serdeable;
import java.time.Instant;
import java.util.Map;
import java.util.HashMap;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Serdeable
@MappedEntity("instance_cluster")
public class InstanceCluster implements Cluster {

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

    private PGvector centroid;

    private String contentFingerprint;

    @TypeDef(type = DataType.JSON)
    private Lineage lineage;
}
