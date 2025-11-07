package gcs.app.adapters;

import com.pgvector.PGvector;
import gcs.app.pgvector.InstanceCluster;
import gcs.app.pgvector.WorkCluster;
import gcs.app.pgvector.storage.InstanceClusterRepository;
import gcs.app.pgvector.storage.WorkClusterRepository;
import gcs.core.InputRecord;
import gcs.core.assignment.AnchorPort;
import gcs.core.ids.Ulid;
import jakarta.inject.Singleton;
import java.util.Optional;

/**
 * An adapter that implements the AnchorPort interface for a PostgreSQL database.
 */
@Singleton
public class PgAnchorAdapter implements AnchorPort {

    private final WorkClusterRepository workClusterRepository;
    private final InstanceClusterRepository instanceClusterRepository;

    public PgAnchorAdapter(WorkClusterRepository workClusterRepository, InstanceClusterRepository instanceClusterRepository) {
        this.workClusterRepository = workClusterRepository;
        this.instanceClusterRepository = instanceClusterRepository;
    }

    @Override
    public Optional<InputRecord> getAnchor(String clusterId) {
        if (workClusterRepository.existsById(clusterId)) {
            return workClusterRepository.findById(clusterId).map(WorkCluster::getSyntheticAnchor);
        } else {
            return instanceClusterRepository.findById(clusterId).map(InstanceCluster::getSyntheticAnchor);
        }
    }

    @Override
    public String createCluster(InputRecord anchor, String representation, String label, float[] initialCentroid) {
        if ("work".equals(representation)) {
            WorkCluster cluster = new WorkCluster();
            cluster.setId(Ulid.nextUlid());
            cluster.setStatus("NEW");
            cluster.setSyntheticAnchor(anchor);
            cluster.setLabel(label);
            cluster.setCentroid(new PGvector(initialCentroid));
            return workClusterRepository.save(cluster).getId();
        } else {
            InstanceCluster cluster = new InstanceCluster();
            cluster.setId(Ulid.nextUlid());
            cluster.setSyntheticAnchor(anchor);
            cluster.setStatus("NEW");
            cluster.setLabel(label);
            cluster.setCentroid(new PGvector(initialCentroid));
            return instanceClusterRepository.save(cluster).getId();
        }
    }

    @Override
    public void updateAnchor(String clusterId, InputRecord anchor) {
        if (workClusterRepository.existsById(clusterId)) {
            workClusterRepository.findById(clusterId).ifPresent(c -> {
								c.setStatus("UPDATED");
                c.setSyntheticAnchor(anchor);
                workClusterRepository.update(c);
            });
        } else {
            instanceClusterRepository.findById(clusterId).ifPresent(c -> {
								c.setStatus("UPDATED");
                c.setSyntheticAnchor(anchor);
                instanceClusterRepository.update(c);
            });
        }
    }
}
