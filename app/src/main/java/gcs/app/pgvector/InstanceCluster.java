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

@Builder
@AllArgsConstructor
@NoArgsConstructor
@lombok.EqualsAndHashCode
@lombok.ToString
@Serdeable
@MappedEntity("instance_cluster")
public class InstanceCluster implements Cluster {

    @Id
    @Getter @Setter
    private UUID id;

    @DateCreated
    @Getter @Setter
    private Instant dateCreated;

    @DateUpdated
    @Getter @Setter
    private Instant dateModified;

    @Getter @Setter
    private String status;

    @Getter @Setter
    private String label;

    @TypeDef(type = DataType.JSON)
    @Getter @Setter
    private InputRecord syntheticAnchor;

    @Getter @Setter
    private PGvector centroid;

    @Getter @Setter
    private String contentFingerprint;

    @TypeDef(type = DataType.JSON)
    @Getter @Setter
    private Lineage lineage;
}
