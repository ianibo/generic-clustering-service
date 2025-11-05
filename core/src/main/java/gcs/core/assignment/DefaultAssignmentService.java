package gcs.core.assignment;

import gcs.core.EmbeddingService;
import gcs.core.InputRecord;
import gcs.core.canonicalization.Canonicalizer;
import gcs.core.policy.RepresentationPolicy;
import gcs.core.scoring.Scorer;
import gcs.core.scoring.ScoreBreakdown;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of the AssignmentService interface.
 */
@Singleton
public class DefaultAssignmentService implements AssignmentService {

    private final CandidatePort candidatePort;
    private final AnchorPort anchorPort;
    private final Scorer scorer;
    private final RepresentationPolicy representationPolicy;
    private final EmbeddingService embeddingService;
    private final Map<String, Canonicalizer> canonicalizers;
    private final Canonicalizer defaultCanonicalizer;
    private final double tauJoin = 0.8; // Placeholder

    public DefaultAssignmentService(
        CandidatePort candidatePort,
        AnchorPort anchorPort,
        Scorer scorer,
        RepresentationPolicy representationPolicy,
        @Named("openai") EmbeddingService embeddingService,
        List<Canonicalizer> canonicalizerList
    ) {
        this.candidatePort = candidatePort;
        this.anchorPort = anchorPort;
        this.scorer = scorer;
        this.representationPolicy = representationPolicy;
        this.embeddingService = embeddingService;
        this.canonicalizers = canonicalizerList.stream()
            .filter(c -> c.forContentType() != null)
            .collect(Collectors.toMap(Canonicalizer::forContentType, Function.identity()));
        this.defaultCanonicalizer = canonicalizerList.stream()
            .filter(c -> c.forContentType() == null)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No default canonicalizer found"));
    }

    @Override
    public Assignment assign(InputRecord record, String representation) {
        var canonicalizer = canonicalizers.getOrDefault(record.physical().contentType(), defaultCanonicalizer);
        String summary = canonicalizer.summarize(record, "work".equals(representation) ? Canonicalizer.Intent.WORK : Canonicalizer.Intent.INSTANCE);
        float[] embedding = embeddingService.embed(summary);

        List<CandidatePort.Candidate> candidates = candidatePort.findCandidates(embedding, representation, 10, Optional.empty());

        CandidatePort.Candidate bestCandidate = null;
        ScoreBreakdown bestScore = null;

        for (CandidatePort.Candidate candidate : candidates) {
            if (representationPolicy.fieldAgreementOk(record, candidate.getAnchor())) {
                ScoreBreakdown score = scorer.score(record, candidate.getAnchor(), candidate.getScore());
                if (bestScore == null || score.getTotal() > bestScore.getTotal()) {
                    bestScore = score;
                    bestCandidate = candidate;
                }
            }
        }

        if (bestCandidate != null && bestScore.getTotal() >= tauJoin) {
            return Assignment.builder()
                .decision(Assignment.Decision.JOINED)
                .clusterId(bestCandidate.getClusterId())
                .clusterAnchor(bestCandidate.getAnchor())
                .scoreBreakdown(bestScore)
                .build();
        } else {
            return Assignment.builder()
                .decision(Assignment.Decision.CREATED)
                .clusterId(anchorPort.createCluster(record, representation))
                .clusterAnchor(record)
                .scoreBreakdown(null)
                .build();
        }
    }
}
