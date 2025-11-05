package gcs.core.synthesis;

import gcs.core.InputRecord;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class DefaultSynthesizerTest {

    private final DefaultSynthesizer synthesizer = new DefaultSynthesizer();

    @Test
    void testSynthesize_majorityVote() {
        InputRecord.Title title1 = new InputRecord.Title("Title 1", "main", null);
        InputRecord.Title title2 = new InputRecord.Title("Title 2", "main", null);

        InputRecord record1 = new InputRecord("1", null, null, null, null, List.of(title1), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord record2 = new InputRecord("2", null, null, null, null, List.of(title1), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        InputRecord record3 = new InputRecord("3", null, null, null, null, List.of(title2), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);

        InputRecord result = synthesizer.synthesize(List.of(record1, record2, record3));

        assertEquals(title1, result.titles().get(0));
    }

    @Test
    void testSynthesize_emptyList() {
        InputRecord result = synthesizer.synthesize(Collections.emptyList());

        assertNull(result);
    }

    @Test
    void testSynthesize_nullList() {
        InputRecord result = synthesizer.synthesize(null);

        assertNull(result);
    }
}
