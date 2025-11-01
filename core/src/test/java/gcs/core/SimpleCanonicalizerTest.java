package gcs.core;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleCanonicalizerTest {
    @Test
    void testSummarize() {
        var canonicalizer = new SimpleCanonicalizer();
        var record = new InputRecord(
            "rec-001", // id
            null, // provenance
            null, // domain
            null, // licenseDeclaration
            List.of(new InputRecord.Identifier("ISBN", "978-0-471-05845-8", null)), // identifiers
            List.of(new InputRecord.Title("Brain of the Firm", "main", null)), // titles
            List.of(new InputRecord.Contributor("Stafford Beer", "author", "person", null, null)), // contributors
            null, // languages
            null, // edition
            new InputRecord.Publication(List.of("Chichester"), List.of("Wiley"), "1972", 1972, "GB"), // publication
            null, // physical
            null, // subjects
            null, // series
            null, // relations
            null, // classification
            null, // notes
            null, // rights
            null, // admin
            null, // media
            null, // ext
            null // classifierVersion
        );

        var expected = "title:Brain of the Firm; creators:Stafford Beer; pub:Wiley; year:1972; ids:ISBN:978-0-471-05845-8";
        var actual = canonicalizer.summarize(record);

        assertEquals(expected, actual);
    }
}
