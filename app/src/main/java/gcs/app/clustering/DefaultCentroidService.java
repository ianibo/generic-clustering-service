package gcs.app.clustering;

import com.pgvector.PGvector;
import gcs.app.pgvector.*;
import gcs.app.pgvector.storage.*;
import jakarta.inject.Singleton;
import jakarta.transaction.Transactional;
import java.util.UUID;
import gcs.app.clustering.*;

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
                updateClusterCentroid(cluster, memberEmbedding);
                workClusterRepository.update(cluster);
            });
        } else {
            instanceClusterRepository.findById(clusterId).ifPresent(cluster -> {
                updateClusterCentroid(cluster, memberEmbedding);
                instanceClusterRepository.update(cluster);
            });
        }
    }

    private <T extends gcs.app.pgvector.Cluster> void updateClusterCentroid(T cluster, PGvector memberEmbedding) {
        PGvector currentCentroid = cluster.getCentroid();
        if (currentCentroid == null) {
            cluster.setCentroid(memberEmbedding);
            cluster.setMemberCount(1);
            return;
        }
        int memberCount = cluster.getMemberCount();

        float[] currentCentroidArray = currentCentroid.toArray();
        float[] memberEmbeddingArray = memberEmbedding.toArray();
        int dimension = currentCentroidArray.length;
        float[] newCentroid = new float[dimension];

        for (int i = 0; i < dimension; i++) {
            newCentroid[i] = (currentCentroidArray[i] * memberCount + memberEmbeddingArray[i]) / (memberCount + 1);
        }

        cluster.setCentroid(new PGvector(newCentroid));
        cluster.setMemberCount(memberCount + 1);
    }
}
