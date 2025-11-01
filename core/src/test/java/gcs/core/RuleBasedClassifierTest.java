package gcs.core;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.Collections;
import java.util.List;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RuleBasedClassifierTest {

    private RuleBasedClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new RuleBasedClassifier();
    }

    @Test
    void testClassifyThesisByNote() {
        var note = new InputRecord.Note("dissertation", "Thesis (Ph.D.)--University of Minnesota, 2023.");
        var record = new InputRecord("1", null, null, null, null, null, null, null, null, null, null, null, null, null, null, List.of(note), null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals(WorkType.THESIS, result.workType());
    }

    @Test
    void testClassifyThesisBySubject() {
        var subject = new InputRecord.Subject("Theses", null, null);
        var record = new InputRecord("2", null, null, null, null, null, null, null, null, null, null, List.of(subject), null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals(WorkType.THESIS, result.workType());
    }

    @Test
    void testClassifyArchivalByExtent() {
        var physical = new InputRecord.Physical("1.5 linear feet", null, null, null, null, null);
        var record = new InputRecord("3", null, null, null, null, null, null, null, null, null, physical, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals(WorkType.ARCHIVAL, result.workType());
    }

    @Test
    void testClassifyArchivalByTitle() {
        var title = new InputRecord.Title("Fonds de la famille...", "primary", null);
        var record = new InputRecord("4", null, null, null, null, List.of(title), null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals(WorkType.ARCHIVAL, result.workType());
    }

    @Test
    void testDefaultClassification() {
        var record = new InputRecord("5", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals(WorkType.BOOK_MONOGRAPH, result.workType());
    }
}
