package gcs.core.assignment;

import gcs.core.EmbeddingService;
import gcs.core.InputRecord;
import gcs.core.canonicalization.Canonicalizer;
import gcs.core.policy.RepresentationPolicy;
import gcs.core.scoring.Scorer;
import gcs.core.scoring.ScoreBreakdown;
import jakarta.inject.Singleton;
import jakarta.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Default implementation of the {@link AssignmentService}. This class contains the core logic
 * for deciding whether an incoming record should be assigned to an existing cluster or
 * should start a new one.
 *
 * <h2>Orchestration Roadmap:</h2>
 * <ol>
 *   <li><b>Generate Embedding:</b>
 *     <ul>
 *       <li>Selects the appropriate {@link Canonicalizer} based on the record's content type.</li>
 *       <li>Generates a textual summary of the record.</li>
 *       <li>Calls the {@link EmbeddingService} to convert the summary into a vector embedding.</li>
 *     </ul>
 *   </li>
 *   <li><b>Find Candidates:</b> The embedding is passed to the {@link CandidatePort} to perform a k-NN search for the most similar existing cluster anchors.</li>
 *   <li><b>Score Candidates:</b> Each candidate is evaluated:
 *     <ul>
 *       <li>First, a coarse check is done using {@link RepresentationPolicy#fieldAgreementOk}.</li>
 *       <li>If compatible, the {@link Scorer} calculates a detailed {@link ScoreBreakdown}, combining the initial vector score with scores from other business rules (e.g., publication year).</li>
 *     </ul>
 *   </li>
 *   <li><b>Make Decision:</b>
 *     <ul>
 *       <li>The candidate with the highest total score is selected.</li>
 *       <li>If this score is at or above the join threshold ({@code tauJoin}), a {@link Assignment.Decision#JOINED} decision is returned.</li>
 *       <li>Otherwise, the {@link AnchorPort} is called to create a new cluster, and a {@link Assignment.Decision#CREATED} decision is returned.</li>
 *     </ul>
 *   </li>
 * </ol>
 */
@Singleton
public class DefaultAssignmentService implements AssignmentService {

    private final CandidatePort candidatePort;
    private final AnchorPort anchorPort;
    private final Scorer scorer;
    private final RepresentationPolicy representationPolicy;
    private final Map<String, Canonicalizer> canonicalizers;
    private final Canonicalizer defaultCanonicalizer;
    private final double tauJoin = 0.8; // Placeholder

    public DefaultAssignmentService(
        CandidatePort candidatePort,
        AnchorPort anchorPort,
        Scorer scorer,
        RepresentationPolicy representationPolicy,
        List<Canonicalizer> canonicalizerList
    ) {
        this.candidatePort = candidatePort;
        this.anchorPort = anchorPort;
        this.scorer = scorer;
        this.representationPolicy = representationPolicy;
        this.canonicalizers = canonicalizerList.stream()
            .filter(c -> c.forContentType() != null)
            .collect(Collectors.toMap(Canonicalizer::forContentType, Function.identity()));
        this.defaultCanonicalizer = canonicalizerList.stream()
            .filter(c -> c.forContentType() == null)
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No default canonicalizer found"));
    }

    @Override
    public Assignment assign(InputRecord record, String representation, float[] embedding) {
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
