package gcs.core.scoring;

import gcs.core.InputRecord;
import gcs.core.policy.RepresentationPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

class DefaultScorerTest {

    @Mock
    private RepresentationPolicy representationPolicy;

    private DefaultScorer scorer;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        scorer = new DefaultScorer(representationPolicy);
    }

    @Test
    void testScore() {
        InputRecord record1 = new InputRecord(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord record2 = new InputRecord(null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        when(representationPolicy.scorePublicationYear(record1, record2)).thenReturn(0.5);
        when(representationPolicy.conflictPenalty(record1, record2)).thenReturn(0.1);

        ScoreBreakdown scoreBreakdown = scorer.score(record1, record2, 0.9);

        assertEquals(1.3, scoreBreakdown.getTotal(), 0.001);
        assertEquals(0.9, scoreBreakdown.getEmbedding(), 0.001);
        assertEquals(0.5, scoreBreakdown.getPubyr(), 0.001);
        assertEquals(0.1, scoreBreakdown.getConflictPenalty(), 0.001);
    }
}
