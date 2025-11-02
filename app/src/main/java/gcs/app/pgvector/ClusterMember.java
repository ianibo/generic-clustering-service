package gcs.app.pgvector;

import com.pgvector.PGvector;
import io.micronaut.data.annotation.DateCreated;
import io.micronaut.data.annotation.DateUpdated;
import io.micronaut.data.annotation.GeneratedValue;
import io.micronaut.data.annotation.Id;
import io.micronaut.data.annotation.MappedEntity;
import io.micronaut.data.annotation.TypeDef;
import io.micronaut.data.model.DataType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;
import lombok.experimental.SuperBuilder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@SuperBuilder
@Data
@MappedEntity
public abstract class ClusterMember {
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

    @TypeDef(type = DataType.STRING, converter = PGvector.class)
    private PGvector blocking;

    @TypeDef(type = DataType.STRING, converter = PGvector.class)
    private PGvector embedding;
}
