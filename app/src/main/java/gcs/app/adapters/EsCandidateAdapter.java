package gcs.app.adapters;

import com.pgvector.PGvector;
import gcs.app.esvector.ESIndexStore;
import gcs.app.pgvector.storage.*;
import gcs.app.pgvector.*;
import gcs.core.InputRecord;
import gcs.core.assignment.CandidatePort;
import jakarta.inject.Singleton;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * An adapter that implements the CandidatePort interface using Elasticsearch.
 */
@Singleton
public class EsCandidateAdapter implements CandidatePort {

    private final ESIndexStore esIndexStore;
    private final PgAnchorAdapter pgAnchorAdapter;
    private final WorkClusterRepository workClusterRepository;
    private final InstanceClusterRepository instanceClusterRepository;

    public EsCandidateAdapter(ESIndexStore esIndexStore, PgAnchorAdapter pgAnchorAdapter, WorkClusterRepository workClusterRepository, InstanceClusterRepository instanceClusterRepository) {
        this.esIndexStore = esIndexStore;
        this.pgAnchorAdapter = pgAnchorAdapter;
        this.workClusterRepository = workClusterRepository;
        this.instanceClusterRepository = instanceClusterRepository;
    }

    @Override
    public List<Candidate> findCandidates(float[] embedding, String representation, int k, Optional<Map<String, String>> filters) {
        List<UUID> candidateIds = findCandidateIds(embedding, representation, k);
        if (candidateIds.isEmpty()) {
            return List.of();
        }
        return searchAnchors(embedding, representation, k, filters, candidateIds);
    }

    private List<UUID> findCandidateIds(float[] embedding, String representation, int k) {
        if ("work".equals(representation)) {
            return workClusterRepository.findNearestNeighbors(new PGvector(embedding), k).stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());
        } else {
            return instanceClusterRepository.findNearestNeighbors(new PGvector(embedding), k).stream()
                .map(c -> c.getId())
                .collect(Collectors.toList());
        }
    }

    private List<Candidate> searchAnchors(float[] embedding, String representation, int k, Optional<Map<String, String>> filters, List<UUID> candidateIds) {
        String indexName = "anchors-" + representation;
        try {
            return esIndexStore.search(indexName, embedding, "embedding", 0.0, k, filters, candidateIds).stream()
                .map(searchResult -> {
                    Optional<InputRecord> anchor = pgAnchorAdapter.getAnchor(UUID.fromString(searchResult.id()));
                    if (anchor.isEmpty()) {
                        return null;
                    }
                    return new Candidate() {
                        @Override
                        public UUID getClusterId() {
                            return UUID.fromString(searchResult.id());
                        }

                        @Override
                        public InputRecord getAnchor() {
                            return anchor.get();
                        }

                        @Override
                        public double getScore() {
                            return searchResult.score();
                        }
                    };
                })
                .filter(java.util.Objects::nonNull)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
