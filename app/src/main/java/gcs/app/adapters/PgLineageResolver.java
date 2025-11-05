package gcs.app.adapters;

import gcs.app.pgvector.storage.InstanceClusterRepository;
import gcs.app.pgvector.storage.WorkClusterRepository;
import gcs.core.lineage.Lineage;
import gcs.core.lineage.LineageResolver;
import jakarta.inject.Singleton;

import java.util.UUID;

@Singleton
public class PgLineageResolver implements LineageResolver {

    private final WorkClusterRepository workClusterRepository;
    private final InstanceClusterRepository instanceClusterRepository;

    public PgLineageResolver(WorkClusterRepository workClusterRepository, InstanceClusterRepository instanceClusterRepository) {
        this.workClusterRepository = workClusterRepository;
        this.instanceClusterRepository = instanceClusterRepository;
    }

    @Override
    public Lineage resolve(UUID clusterId) {
        if (workClusterRepository.existsById(clusterId)) {
            return workClusterRepository.findById(clusterId).map(c -> c.getLineage()).orElse(null);
        } else {
            return instanceClusterRepository.findById(clusterId).map(c -> c.getLineage()).orElse(null);
        }
    }
}
