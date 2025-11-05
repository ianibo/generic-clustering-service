package gcs.app.clustering;

import com.pgvector.PGvector;
import gcs.app.pgvector.InstanceClusterRepository;
import gcs.app.pgvector.WorkClusterRepository;
import gcs.app.pgvector.WorkClusterMemberRepository;
import gcs.app.pgvector.InstanceClusterMemberRepository;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.util.UUID;

@Singleton
public class DefaultCentroidService implements CentroidService {

    private final WorkClusterRepository workClusterRepository;
    private final InstanceClusterRepository instanceClusterRepository;
    private final WorkClusterMemberRepository workClusterMemberRepository;
    private final InstanceClusterMemberRepository instanceClusterMemberRepository;

    public DefaultCentroidService(WorkClusterRepository workClusterRepository, InstanceClusterRepository instanceClusterRepository, WorkClusterMemberRepository workClusterMemberRepository, InstanceClusterMemberRepository instanceClusterMemberRepository) {
        this.workClusterRepository = workClusterRepository;
        this.instanceClusterRepository = instanceClusterRepository;
        this.workClusterMemberRepository = workClusterMemberRepository;
        this.instanceClusterMemberRepository = instanceClusterMemberRepository;
    }

    @Override
    @Transactional
    public void updateCentroid(UUID clusterId, String representation, PGvector memberEmbedding) {
        if ("work".equals(representation)) {
            workClusterRepository.findById(clusterId).ifPresent(cluster -> {
                updateClusterCentroid(cluster, memberEmbedding, workClusterMemberRepository.countByWorkClusterId(clusterId));
                workClusterRepository.update(cluster);
            });
        } else {
            instanceClusterRepository.findById(clusterId).ifPresent(cluster -> {
                updateClusterCentroid(cluster, memberEmbedding, instanceClusterMemberRepository.countByInstanceClusterId(clusterId));
                instanceClusterRepository.update(cluster);
            });
        }
    }

    private <T extends gcs.app.pgvector.Cluster> void updateClusterCentroid(T cluster, PGvector memberEmbedding, long memberCount) {
        PGvector currentCentroid = cluster.getCentroid();
        if (currentCentroid == null) {
            cluster.setCentroid(memberEmbedding);
        } else {
            // Simple incremental update
            float[] currentCentroidArray = currentCentroid.toArray();
            float[] memberEmbeddingArray = memberEmbedding.toArray();
            int dimension = currentCentroidArray.length;
            float[] newCentroid = new float[dimension];
            for (int i = 0; i < dimension; i++) {
                newCentroid[i] = (currentCentroidArray[i] * (memberCount - 1) + memberEmbeddingArray[i]) / memberCount;
            }
            cluster.setCentroid(new PGvector(newCentroid));
        }
    }
}
