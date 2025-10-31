package gcs.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleCanonicalizerTest {
    @Test
    void testSummarize() {
        var canonicalizer = new SimpleCanonicalizer();
        var record = new InputRecord(
            "rec-001",
            null,
            null,
            null,
            List.of(new InputRecord.Identifier("ISBN", "978-0-471-05845-8", null)),
            List.of(new InputRecord.Title("Brain of the Firm", "main", null)),
            List.of(new InputRecord.Contributor("Stafford Beer", "author", "person", null, null)),
            null,
            null,
            new InputRecord.Publication(List.of("Chichester"), List.of("Wiley"), "1972", 1972, "GB"),
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null,
            null
        );

        var expected = "title:Brain of the Firm; creators:Stafford Beer; pub:Wiley; year:1972; ids:ISBN:978-0-471-05845-8";
        var actual = canonicalizer.summarize(record);

        assertEquals(expected, actual);
    }
}
