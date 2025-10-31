package gcs.core;

/**
 * A simple canonicalizer that returns the text of the input record.
 */
public class SimpleCanonicalizer implements Canonicalizer {

    @Override
    public String summarize(InputRecord r) {
        return r.text();
    }
}
