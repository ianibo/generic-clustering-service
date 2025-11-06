package gcs.core.consolidation;

import gcs.core.InputRecord;
import gcs.core.assignment.AnchorPort;
import gcs.core.assignment.CandidatePort;
import gcs.core.synthesis.Synthesizer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultConsolidationServiceTest {

    @Mock
    private AnchorPort anchorPort;

    @Mock
    private MemberPort memberPort;

    @Mock
    private Synthesizer synthesizer;

    private DefaultConsolidationService service;

    @Mock
    private CandidatePort candidatePort;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new DefaultConsolidationService(anchorPort, memberPort, synthesizer, candidatePort);
    }

    @Test
    void testMerge() {
        UUID clusterId1 = UUID.randomUUID();
        UUID clusterId2 = UUID.randomUUID();
        UUID newClusterId = UUID.randomUUID();
        InputRecord record1 = new InputRecord("1", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord record2 = new InputRecord("2", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord newAnchor = new InputRecord("3", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(memberPort.getMembers(clusterId1)).thenReturn(Collections.singletonList(record1));
        when(memberPort.getMembers(clusterId2)).thenReturn(Collections.singletonList(record2));
        when(synthesizer.synthesize(List.of(record1, record2))).thenReturn(newAnchor);
        when(anchorPort.createCluster(newAnchor, "work", "Consolidated Cluster", new float[1536])).thenReturn(newClusterId);

        UUID result = service.merge(clusterId1, clusterId2);

        assertEquals(newClusterId, result);
    }

    @Test
    void testSplit() {
        UUID clusterId = UUID.randomUUID();
        InputRecord record1 = new InputRecord("1", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord record2 = new InputRecord("2", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord newAnchor1 = new InputRecord("3", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord newAnchor2 = new InputRecord("4", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(memberPort.getMembers(clusterId)).thenReturn(List.of(record1, record2));
        when(synthesizer.synthesize(List.of(record1))).thenReturn(newAnchor1);
        when(synthesizer.synthesize(List.of(record2))).thenReturn(newAnchor2);

        service.split(clusterId);

        verify(anchorPort).createCluster(newAnchor1, "work", "Split Cluster 1", new float[1536]);
        verify(anchorPort).createCluster(newAnchor2, "work", "Split Cluster 2", new float[1536]);
    }
}
