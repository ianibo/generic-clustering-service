package gcs.core;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import java.util.List;
import java.time.OffsetDateTime;

@Serdeable
public record InputRecord(
    String id,
    Provenance provenance,
    String domain,
    LicenseDeclaration licenseDeclaration,
    List<Identifier> identifiers,
    List<Title> titles,
    List<Contributor> contributors,
    List<String> languages,
    Edition edition,
    Publication publication,
    Physical physical,
    List<Subject> subjects,
    List<Series> series,
    Relations relations,
    List<Classification> classification,
    List<Note> notes,
    Rights rights,
    Admin admin,
    Media media,
    Ext ext
) {
    @Serdeable
    public record Provenance(
        String authorityId,
        String authorityScheme,
        String sourceRecordId,
        String sourceRecordUri,
        String originalFormat,
        OffsetDateTime harvestedAt,
        String metadataLicense
    ) {}

    @Serdeable
    public record LicenseDeclaration(
        String license,
        String licenseUri,
        PublisherAffirmation publisherAffirmation
    ) {}

    @Serdeable
    public record PublisherAffirmation(
        String statement,
        boolean confirmed,
        OffsetDateTime confirmedAt,
        String signatory
    ) {}

    @Serdeable
    public record Identifier(
        String type,
        String value,
        @Nullable String status
    ) {}

    @Serdeable
    public record Title(
        String value,
        String type,
        String language
    ) {}

    @Serdeable
    public record Contributor(
        String name,
        String role,
        String kind,
        @Nullable List<Identifier> identifiers,
        @Nullable String dates
    ) {}

    @Serdeable
    public record Edition(
        String statement
    ) {}

    @Serdeable
    public record Publication(
        List<String> place,
        List<String> publisher,
        String date,
        int year,
        String countryCode
    ) {}

    @Serdeable
    public record Physical(
        String extent,
        String dimensions,
        String contentType,
        String mediaType,
        String carrierType,
        String format
    ) {}

    @Serdeable
    public record Subject(
        String value,
        String scheme,
        String type
    ) {}

    @Serdeable
    public record Series() {}

    @Serdeable
    public record Relations(
        List<String> otherFormats
    ) {}

    @Serdeable
    public record Classification(
        String scheme,
        String value
    ) {}

    @Serdeable
    public record Note(
        String type,
        String value
    ) {}

    @Serdeable
    public record Rights(
        String rightsStatement,
        String license
    ) {}

    @Serdeable
    public record Admin(
        OffsetDateTime created,
        Quality quality,
        List<String> flags
    ) {}

    @Serdeable
    public record Quality(
        boolean normalized
    ) {}

    @Serdeable
    public record Media() {} // Empty object in example

    @Serdeable
    public record Ext(
        SourceHints sourceHints
    ) {}

    @Serdeable
    public record SourceHints(
        List<String> titleTokens,
        String nameTitlePair
    ) {}
}