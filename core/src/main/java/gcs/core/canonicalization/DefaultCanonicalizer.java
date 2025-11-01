package gcs.core.canonicalization;

import gcs.core.InputRecord;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class DefaultCanonicalizer implements Canonicalizer {
    @Override
    public String summarize(InputRecord record) {
        return List.of(
                "title:" + getTitle(record),
                "creators:" + getCreators(record),
                "pub:" + getPublisher(record),
                "year:" + getYear(record),
                "ids:" + getIds(record)
            ).stream()
            .collect(Collectors.joining("; "));
    }

    private String getTitle(InputRecord record) {
        if (record.titles() == null || record.titles().isEmpty()) {
            return "";
        }
        return record.titles().get(0).value();
    }

    private String getCreators(InputRecord record) {
        if (record.contributors() == null || record.contributors().isEmpty()) {
            return "";
        }
        return record.contributors().stream()
            .map(InputRecord.Contributor::name)
            .collect(Collectors.joining(", "));
    }

    private String getPublisher(InputRecord record) {
        if (record.publication() == null || record.publication().publisher() == null || record.publication().publisher().isEmpty()) {
            return "";
        }
        return record.publication().publisher().get(0);
    }

    private String getYear(InputRecord record) {
        if (record.publication() == null || record.publication().year() == null) {
            return "";
        }
        return record.publication().year().toString();
    }

    private String getIds(InputRecord record) {
        if (record.identifiers() == null || record.identifiers().isEmpty()) {
            return "";
        }
        return record.identifiers().stream()
            .map(id -> id.type() + ":" + id.value())
            .collect(Collectors.joining(", "));
    }

    @Override
    public String forContentType() {
        return null;
    }
}
