package gcs.core.policy;

import gcs.core.InputRecord;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultRepresentationPolicyTest {

    private final DefaultRepresentationPolicy policy = new DefaultRepresentationPolicy();

    @Test
    void testConflictPenalty_mismatchedContentType() {
        InputRecord.Physical physical1 = new InputRecord.Physical(null, null, null, "TEXT", null, null, null);
        InputRecord.Physical physical2 = new InputRecord.Physical(null, null, null, "IMAGE", null, null, null);
        InputRecord record1 = new InputRecord(null, null, null, null, null, null, null, null, null, null, physical1, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord record2 = new InputRecord(null, null, null, null, null, null, null, null, null, null, physical2, null, null, null, null, null, null, null, null, null, null, null);

        assertEquals(0.1, policy.conflictPenalty(record1, record2), 0.001);
    }

    @Test
    void testScorePublicationYear_sameYear() {
        InputRecord.Publication publication1 = new InputRecord.Publication(null, null, null, 2020, null);
        InputRecord.Publication publication2 = new InputRecord.Publication(null, null, null, 2020, null);
        InputRecord record1 = new InputRecord(null, null, null, null, null, null, null, null, null, publication1, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord record2 = new InputRecord(null, null, null, null, null, null, null, null, null, publication2, null, null, null, null, null, null, null, null, null, null, null, null);

        assertEquals(1.0, policy.scorePublicationYear(record1, record2), 0.001);
    }

    @Test
    void testScorePublicationYear_closeYear() {
        InputRecord.Publication publication1 = new InputRecord.Publication(null, null, null, 2020, null);
        InputRecord.Publication publication2 = new InputRecord.Publication(null, null, null, 2022, null);
        InputRecord record1 = new InputRecord(null, null, null, null, null, null, null, null, null, publication1, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord record2 = new InputRecord(null, null, null, null, null, null, null, null, null, publication2, null, null, null, null, null, null, null, null, null, null, null, null);

        assertEquals(0.5, policy.scorePublicationYear(record1, record2), 0.001);
    }

    @Test
    void testScorePublicationYear_farYear() {
        InputRecord.Publication publication1 = new InputRecord.Publication(null, null, null, 2020, null);
        InputRecord.Publication publication2 = new InputRecord.Publication(null, null, null, 2023, null);
        InputRecord record1 = new InputRecord(null, null, null, null, null, null, null, null, null, publication1, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord record2 = new InputRecord(null, null, null, null, null, null, null, null, null, publication2, null, null, null, null, null, null, null, null, null, null, null, null);

        assertEquals(0.0, policy.scorePublicationYear(record1, record2), 0.001);
    }
}
