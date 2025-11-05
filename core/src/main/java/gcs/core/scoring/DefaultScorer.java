package gcs.core.scoring;

import gcs.core.InputRecord;
import gcs.core.policy.RepresentationPolicy;
import jakarta.inject.Singleton;

/**
 * Default implementation of the Scorer interface.
 */
@Singleton
public class DefaultScorer implements Scorer {

    private final RepresentationPolicy representationPolicy;

    public DefaultScorer(RepresentationPolicy representationPolicy) {
        this.representationPolicy = representationPolicy;
    }

    @Override
    public ScoreBreakdown score(InputRecord inputRecord, InputRecord candidateAnchor, double embeddingScore) {
        double keysScore = 0.0; // Placeholder
        double idsScore = 0.0; // Placeholder
        double pubyrScore = representationPolicy.scorePublicationYear(inputRecord, candidateAnchor);
        double conflictPenalty = representationPolicy.conflictPenalty(inputRecord, candidateAnchor);

        double total = embeddingScore + keysScore + idsScore + pubyrScore - conflictPenalty;

        return ScoreBreakdown.builder()
            .total(total)
            .embedding(embeddingScore)
            .keys(keysScore)
            .ids(idsScore)
            .pubyr(pubyrScore)
            .conflictPenalty(conflictPenalty)
            .build();
    }
}
