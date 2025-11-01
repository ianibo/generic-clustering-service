package gcs.core.classification;

import gcs.core.InputRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class RuleBasedInstanceClassifierTest {

    private RuleBasedInstanceClassifier classifier;

    @BeforeEach
    void setUp() {
        classifier = new RuleBasedInstanceClassifier();
    }

    @Test
    void testClassifyPrintBook() {
        var physical = new InputRecord.Physical(null, null, "text", "unmediated", "volume", null);
        var record = new InputRecord("1", null, null, null, null, null, null, null, null, null, physical, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals(ContentType.TEXT, result.contentType());
        assertEquals(MediaType.UNMEDIATED, result.mediaType());
        assertEquals(CarrierType.VOLUME, result.carrierType());
        assertNull(result.formatDetail());
    }

    @Test
    void testClassifyEbook() {
        var physical = new InputRecord.Physical(null, null, "text", "computer", "online resource", "epub");
        var record = new InputRecord("2", null, null, null, null, null, null, null, null, null, physical, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals(ContentType.TEXT, result.contentType());
        assertEquals(MediaType.COMPUTER, result.mediaType());
        assertEquals(CarrierType.ONLINE_RESOURCE, result.carrierType());
        assertEquals("EPUB", result.formatDetail());
    }

    @Test
    void testClassifyDvd() {
        var physical = new InputRecord.Physical(null, null, "two-dimensional moving image", "video", "videodisc", "dvd");
        var record = new InputRecord("3", null, null, null, null, null, null, null, null, null, physical, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals(ContentType.TWO_DIMENSIONAL_MOVING_IMAGE, result.contentType());
        assertEquals(MediaType.VIDEO, result.mediaType());
        assertEquals(CarrierType.VIDEODISC, result.carrierType());
        assertEquals("DVD-Video", result.formatDetail());
    }

    @Test
    void testClassifyBluray() {
        var physical = new InputRecord.Physical(null, null, "two-dimensional moving image", "video", "videodisc", "blu-ray");
        var record = new InputRecord("4", null, null, null, null, null, null, null, null, null, physical, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals(ContentType.TWO_DIMENSIONAL_MOVING_IMAGE, result.contentType());
        assertEquals(MediaType.VIDEO, result.mediaType());
        assertEquals(CarrierType.VIDEODISC, result.carrierType());
        assertEquals("Blu-ray", result.formatDetail());
    }

    @Test
    void testDefaultClassification() {
        var record = new InputRecord("5", null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals(ContentType.TEXT, result.contentType());
        assertEquals(MediaType.UNMEDIATED, result.mediaType());
        assertEquals(CarrierType.VOLUME, result.carrierType());
        assertNull(result.formatDetail());
    }

    @Test
    void testClassifyLargePrint() {
        var physical = new InputRecord.Physical(null, null, "text", "unmediated", "volume", null);
        var edition = new InputRecord.Edition("Large print edition");
        var record = new InputRecord("6", null, null, null, null, null, null, null, edition, null, physical, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals("largePrint", result.modality());
    }

    @Test
    void testClassifyBraille() {
        var physical = new InputRecord.Physical(null, null, "tactile text", "tactile", "volume", null);
        var edition = new InputRecord.Edition("Braille edition");
        var record = new InputRecord("7", null, null, null, null, null, null, null, edition, null, physical, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals("braille", result.modality());
    }

    @Test
    void testClassifyPhysicalObject() {
        var physical = new InputRecord.Physical(null, null, "human-made object", null, null, null);
        var record = new InputRecord("8", null, null, null, null, null, null, null, null, null, physical, null, null, null, null, null, null, null, null, null, null);
        var result = classifier.classify(record);
        assertEquals("E22 Human-Made Object", result.objectClass());
    }
}
