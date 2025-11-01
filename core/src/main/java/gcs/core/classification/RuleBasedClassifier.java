package gcs.core.classification;

import gcs.core.InputRecord;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class RuleBasedClassifier implements Classifier {
    public static final int CLASSIFIER_VERSION = 1;
    private static final double HIGH_CONFIDENCE = 0.95;
    private static final double MEDIUM_CONFIDENCE = 0.80;
    private static final double LOW_CONFIDENCE = 0.50;

    private final InstanceClassifier instanceClassifier;

    @Inject
    public RuleBasedClassifier(InstanceClassifier instanceClassifier) {
        this.instanceClassifier = instanceClassifier;
    }

    @Override
    public ClassificationResult classify(InputRecord record) {
        InstanceClassification instanceClassification = instanceClassifier.classify(record);

        return detectThesis(record, instanceClassification)
            .or(() -> detectArchival(record, instanceClassification))
            .orElse(new ClassificationResult(WorkType.BOOK_MONOGRAPH, instanceClassification, List.of("Default classification."), "rules", LOW_CONFIDENCE, CLASSIFIER_VERSION));
    }

    private Optional<ClassificationResult> detectThesis(InputRecord record, InstanceClassification instanceClassification) {
        if (record.notes() != null) {
            for (InputRecord.Note note : record.notes()) {
                if ("dissertation".equalsIgnoreCase(note.type()) ||
                    (note.value() != null && (note.value().toLowerCase().contains("thesis") || note.value().toLowerCase().contains("dissertation")))) {
                    return Optional.of(new ClassificationResult(WorkType.THESIS, instanceClassification, List.of("Dissertation note found."), "rules", HIGH_CONFIDENCE, CLASSIFIER_VERSION));
                }
            }
        }
        if (record.subjects() != null) {
            for (InputRecord.Subject subject : record.subjects()) {
                if (subject.value() != null && subject.value().toLowerCase().contains("theses")) {
                    return Optional.of(new ClassificationResult(WorkType.THESIS, instanceClassification, List.of("Subject 'Theses' found."), "rules", HIGH_CONFIDENCE, CLASSIFIER_VERSION));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<ClassificationResult> detectArchival(InputRecord record, InstanceClassification instanceClassification) {
        if (record.physical() != null && record.physical().extent() != null) {
            String extent = record.physical().extent().toLowerCase();
            List<String> archivalTerms = List.of("linear feet", "fonds", "collection", "series", "file", "item");
            for (String term : archivalTerms) {
                if (extent.contains(term)) {
                    return Optional.of(new ClassificationResult(WorkType.ARCHIVAL, instanceClassification, List.of("Archival extent found: " + term), "rules", MEDIUM_CONFIDENCE, CLASSIFIER_VERSION));
                }
            }
        }
        if (record.titles() != null) {
            for (InputRecord.Title title : record.titles()) {
                if (title.value() != null) {
                    String titleValue = title.value().toLowerCase();
                    if (titleValue.contains("fonds") || titleValue.contains("collection")) {
                        return Optional.of(new ClassificationResult(WorkType.ARCHIVAL, instanceClassification, List.of("Archival title found."), "rules", MEDIUM_CONFIDENCE, CLASSIFIER_VERSION));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
