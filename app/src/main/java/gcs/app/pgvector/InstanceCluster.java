package gcs.app.pgvector;

import io.micronaut.data.annotation.MappedEntity;

import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.Accessors;
import io.micronaut.serde.annotation.Serdeable;


import java.util.Map;
import java.util.HashMap;

@Data
@Accessors(chain = true)
@Serdeable
@NoArgsConstructor
@lombok.EqualsAndHashCode(callSuper = true)
@lombok.ToString(callSuper = true)
@lombok.experimental.SuperBuilder
@MappedEntity("instance_cluster")
public class InstanceCluster extends Cluster {
}
