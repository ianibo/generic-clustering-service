package gcs.core;

import java.util.List;

public interface IngestService {
    List<Candidate> ingest(InputRecord record);

    record Candidate(String id, double score, double pDup) {
    }
}
