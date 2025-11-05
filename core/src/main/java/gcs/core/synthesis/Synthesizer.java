package gcs.core.synthesis;

import gcs.core.InputRecord;
import java.util.List;

/**
 * Creates a synthetic "anchor" record from a list of member records.
 */
public interface Synthesizer {
    /**
     * Creates a synthetic "anchor" record from a list of member records.
     *
     * @param members The list of member records.
     * @return The synthesized anchor record.
     */
    InputRecord synthesize(List<InputRecord> members);
}
