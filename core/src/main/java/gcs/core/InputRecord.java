package gcs.core;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.core.annotation.Introspected;
import java.util.List;
import java.time.OffsetDateTime;

@Introspected
@Serdeable
public record InputRecord(
    String id,
    Provenance provenance,
    String domain,
    LicenseDeclaration licenseDeclaration,
    @Nullable List<Identifier> identifiers,
    @Nullable List<Title> titles,
    @Nullable List<Contributor> contributors,
    @Nullable List<String> languages,
    @Nullable Edition edition,
    Publication publication,
    Physical physical,
    @Nullable List<Subject> subjects,
    @Nullable List<Series> series,
    @Nullable Relations relations,
    @Nullable List<Classification> classification,
    @Nullable List<Note> notes,
    @Nullable Rights rights,
    @Nullable Admin admin,
    @Nullable Media media,
    @Nullable Ext ext,
    @Nullable Integer classifierVersion
) {
    @Serdeable
    public record Provenance(
        String authorityId,
        String authorityScheme,
        String sourceRecordId,
        String sourceRecordUri,
        String originalFormat,
        OffsetDateTime harvestedAt,
        @Nullable String metadataLicense
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
        @Nullable String language
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
        @Nullable Integer year,
        String countryCode
    ) {}

    @Serdeable
    public record Physical(
        String extent,
        String dimensions,
        String contentType,
        String mediaType,
        String carrierType,
        @Nullable String format
    ) {}

    @Serdeable
    public record Subject(
        String value,
        @Nullable String scheme,
        @Nullable String type
    ) {}

    @Serdeable
    public record Series(
        String title
    ) {}

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
    public record Media(
        @Nullable String url,
        @Nullable String contentType
    ) {}

    @Serdeable
    public record Ext(
        @Nullable SourceHints sourceHints
    ) {}

    @Serdeable
    public record SourceHints(
        @Nullable List<String> titleTokens,
        @Nullable String nameTitlePair
    ) {}
}
