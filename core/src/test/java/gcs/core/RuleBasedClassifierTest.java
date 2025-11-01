package gcs.core;

import gcs.core.classification.InstanceClassifier;
import gcs.core.classification.RuleBasedClassifier;
import gcs.core.classification.WorkType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

class RuleBasedClassifierTest {

    @Mock
    private InstanceClassifier instanceClassifier;

    @InjectMocks
    private RuleBasedClassifier classifier;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        when(instanceClassifier.classify(any(InputRecord.class))).thenReturn(null);
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
