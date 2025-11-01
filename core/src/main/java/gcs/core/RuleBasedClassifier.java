package gcs.core;

import jakarta.inject.Singleton;
import java.util.List;
import java.util.Optional;

@Singleton
public class RuleBasedClassifier implements Classifier {
    private static final double HIGH_CONFIDENCE = 0.95;
    private static final double MEDIUM_CONFIDENCE = 0.80;
    private static final double LOW_CONFIDENCE = 0.50;

    @Override
    public ClassificationResult classify(InputRecord record) {
        return detectThesis(record)
            .or(() -> detectArchival(record))
            .orElse(new ClassificationResult(WorkType.BOOK_MONOGRAPH, "Default classification.", LOW_CONFIDENCE));
    }

    private Optional<ClassificationResult> detectThesis(InputRecord record) {
        if (record.notes() != null) {
            for (InputRecord.Note note : record.notes()) {
                if ("dissertation".equalsIgnoreCase(note.type()) ||
                    (note.value() != null && (note.value().toLowerCase().contains("thesis") || note.value().toLowerCase().contains("dissertation")))) {
                    return Optional.of(new ClassificationResult(WorkType.THESIS, "Dissertation note found.", HIGH_CONFIDENCE));
                }
            }
        }
        if (record.subjects() != null) {
            for (InputRecord.Subject subject : record.subjects()) {
                if (subject.value() != null && subject.value().toLowerCase().contains("theses")) {
                    return Optional.of(new ClassificationResult(WorkType.THESIS, "Subject 'Theses' found.", HIGH_CONFIDENCE));
                }
            }
        }
        return Optional.empty();
    }

    private Optional<ClassificationResult> detectArchival(InputRecord record) {
        if (record.physical() != null && record.physical().extent() != null) {
            String extent = record.physical().extent().toLowerCase();
            List<String> archivalTerms = List.of("linear feet", "fonds", "collection", "series", "file", "item");
            for (String term : archivalTerms) {
                if (extent.contains(term)) {
                    return Optional.of(new ClassificationResult(WorkType.ARCHIVAL, "Archival extent found: " + term, MEDIUM_CONFIDENCE));
                }
            }
        }
        if (record.titles() != null) {
            for (InputRecord.Title title : record.titles()) {
                if (title.value() != null) {
                    String titleValue = title.value().toLowerCase();
                    if (titleValue.contains("fonds") || titleValue.contains("collection")) {
                        return Optional.of(new ClassificationResult(WorkType.ARCHIVAL, "Archival title found.", MEDIUM_CONFIDENCE));
                    }
                }
            }
        }
        return Optional.empty();
    }
}
