package gcs.app.pgvector;

import io.micronaut.data.annotation.*;

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
@MappedEntity("work_cluster")
public class WorkCluster {

    @Id
    @GeneratedValue
    private UUID id;

    @DateCreated
    private Instant dateCreated;

    @DateUpdated
    private Instant dateModified;

    private String status;

    private String label;
}
