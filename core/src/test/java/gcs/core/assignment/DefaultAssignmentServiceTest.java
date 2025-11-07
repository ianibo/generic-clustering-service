package gcs.core.assignment;

import gcs.core.EmbeddingService;
import gcs.core.InputRecord;
import gcs.core.canonicalization.Canonicalizer;
import gcs.core.policy.RepresentationPolicy;
import gcs.core.scoring.Scorer;
import gcs.core.scoring.ScoreBreakdown;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

class DefaultAssignmentServiceTest {

    @Mock
    private CandidatePort candidatePort;

    @Mock
    private AnchorPort anchorPort;

    @Mock
    private Scorer scorer;

    @Mock
    private RepresentationPolicy representationPolicy;

    @Mock
    private Canonicalizer canonicalizer;

    private DefaultAssignmentService assignmentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(canonicalizer.forContentType()).thenReturn(null);
        assignmentService = new DefaultAssignmentService(candidatePort, anchorPort, scorer, representationPolicy, List.of(canonicalizer));
    }

    @Test
    void testAssign_join() {
        InputRecord record = new InputRecord("1", null, null, null, null, null, null, null, null, null, new InputRecord.Physical(null, null, null, "TEXT", null, null, null), null, null, null, null, null, null, null, null, null, null);
        InputRecord anchor = new InputRecord("2", null, null, null, null, null, null, null, null, null, new InputRecord.Physical(null, null, null, "TEXT", null, null, null), null, null, null, null, null, null, null, null, null, null);
        UUID clusterId = UUID.randomUUID();

        CandidatePort.Candidate candidate = new CandidatePort.Candidate() {
            @Override
            public UUID getClusterId() {
                return clusterId;
            }

            @Override
            public InputRecord getAnchor() {
                return anchor;
            }

            @Override
            public double getScore() {
                return 0.9;
            }
        };

        when(candidatePort.findCandidates(any(), anyString(), anyInt(), any())).thenReturn(Collections.singletonList(candidate));
        when(representationPolicy.fieldAgreementOk(record, anchor)).thenReturn(true);
        when(scorer.score(record, anchor, 0.9)).thenReturn(ScoreBreakdown.builder().total(0.9).build());

        Assignment assignment = assignmentService.assign(record, "work", new float[0], "summary");

        assertEquals(Assignment.Decision.JOINED, assignment.getDecision());
        assertEquals(clusterId, assignment.getClusterId());
    }

    @Test
    void testAssign_create() {
        InputRecord record = new InputRecord("1", null, null, null, null, null, null, null, null, null, new InputRecord.Physical(null, null, null, "TEXT", null, null, null), null, null, null, null, null, null, null, null, null, null);
        UUID newClusterId = UUID.randomUUID();

        when(candidatePort.findCandidates(any(), anyString(), anyInt(), any())).thenReturn(Collections.emptyList());
        when(anchorPort.createCluster(record, "work", "summary", new float[0])).thenReturn(newClusterId);

        Assignment assignment = assignmentService.assign(record, "work", new float[0], "summary");

        assertEquals(Assignment.Decision.CREATED, assignment.getDecision());
        assertEquals(newClusterId, assignment.getClusterId());
    }
}
