package gcs.core.canonicalization;

import gcs.core.InputRecord;
import jakarta.inject.Singleton;

@Singleton
public class UnknownCanonicalizer implements Canonicalizer {
    @Override
    public String summarize(InputRecord r) {
        return "";
    }

    @Override
    public String forContentType() {
        return "UNKNOWN";
    }
}
