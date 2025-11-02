package gcs.core.canonicalization;

import gcs.core.InputRecord;
import jakarta.inject.Singleton;

import java.util.List;
import java.util.stream.Collectors;

@Singleton
public class UnknownCanonicalizer implements Canonicalizer {

    @Override
    public String summarize(InputRecord record, Intent intent) {
        switch (intent) {
            case INSTANCE:
                return summarizeInstance(record);
            case WORK:
            default:
                return summarizeWork(record);
        }
    }

    private String summarizeWork(InputRecord record) {
        return List.of(
                "title:" + getTitle(record),
                "creators:" + getCreators(record),
                "pub:" + getPublisher(record),
                "year:" + getYear(record),
                "ids:" + getIds(record)
            ).stream()
            .collect(Collectors.joining("; "));
    }

    private String summarizeInstance(InputRecord record) {
        return List.of(
            "title:" + getTitle(record),
            "creators:" + getCreators(record),
            "pub_full:" + getFullPublication(record),
            "physical:" + getPhysical(record),
            "ids:" + getIds(record)
        ).stream()
        .collect(Collectors.joining("; "));
    }

    private String getFullPublication(InputRecord record) {
        if (record.publication() == null) {
            return "";
        }
        var pub = record.publication();
        var parts = new java.util.ArrayList<String>();
        if (pub.place() != null && !pub.place().isEmpty()) {
            parts.add(String.join(", ", pub.place()));
        }
        if (pub.publisher() != null && !pub.publisher().isEmpty()) {
            parts.add(String.join(", ", pub.publisher()));
        }
        if (pub.date() != null && !pub.date().isBlank()) {
            parts.add(pub.date());
        }
        return String.join(" : ", parts);
    }

    private String getPhysical(InputRecord record) {
        if (record.physical() == null) {
            return "";
        }
        var p = record.physical();
        return List.of(
            "extent:" + p.extent(),
            "dims:" + p.dimensions(),
            "contentType:" + p.contentType(),
            "mediaType:" + p.mediaType(),
            "carrierType:" + p.carrierType(),
            "format:" + (p.format() != null ? p.format() : "")
        ).stream().collect(Collectors.joining(", "));
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
        return "UNKNOWN";
    }
}
