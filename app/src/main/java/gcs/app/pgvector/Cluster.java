package gcs.app.pgvector;

import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;

import java.time.Instant;
import java.util.UUID;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;
import lombok.Data;

@NoArgsConstructor
@SuperBuilder
@Data
@MappedEntity
public abstract class Cluster {

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
