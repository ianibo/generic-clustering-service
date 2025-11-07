package gcs.app.clustering;

import com.pgvector.PGvector;
import gcs.app.pgvector.storage.*;
import gcs.app.pgvector.*;
import gcs.core.ids.Ulid;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

class DefaultCentroidServiceTest {

    private WorkClusterRepository workClusterRepository;
    private InstanceClusterRepository instanceClusterRepository;
    private WorkClusterMemberRepository workClusterMemberRepository;
    private InstanceClusterMemberRepository instanceClusterMemberRepository;
    private DefaultCentroidService centroidService;

    @BeforeEach
    void setUp() {
        workClusterRepository = mock(WorkClusterRepository.class);
        instanceClusterRepository = mock(InstanceClusterRepository.class);
        workClusterMemberRepository = mock(WorkClusterMemberRepository.class);
        instanceClusterMemberRepository = mock(InstanceClusterMemberRepository.class);
        centroidService = new DefaultCentroidService(workClusterRepository, instanceClusterRepository, workClusterMemberRepository, instanceClusterMemberRepository);
    }

    @Test
    void testUpdateCentroid_newWorkCluster() {
        String clusterId = Ulid.nextUlid();
        PGvector embedding = new PGvector(new float[]{1.0f, 2.0f, 3.0f});
        WorkCluster cluster = new WorkCluster();
        cluster.setId(clusterId);

        when(workClusterRepository.findById(clusterId)).thenReturn(Optional.of(cluster));

        centroidService.updateCentroid(clusterId, "work", embedding);

        assertEquals(embedding, cluster.getCentroid());
    }

    @Test
    void testUpdateCentroid_existingWorkCluster() {
        String clusterId = Ulid.nextUlid();
        PGvector existingCentroid = new PGvector(new float[]{1.0f, 1.0f, 1.0f});
        PGvector newEmbedding = new PGvector(new float[]{3.0f, 3.0f, 3.0f});
        WorkCluster cluster = new WorkCluster();
        cluster.setId(clusterId);
        cluster.setCentroid(existingCentroid);
        cluster.setMemberCount(1);

        when(workClusterRepository.findById(clusterId)).thenReturn(Optional.of(cluster));

        centroidService.updateCentroid(clusterId, "work", newEmbedding);

        float[] expectedCentroid = new float[]{2.0f, 2.0f, 2.0f};
        assertArrayEquals(expectedCentroid, cluster.getCentroid().toArray(), 0.001f);
    }
}
