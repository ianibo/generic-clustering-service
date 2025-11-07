package gcs.core;

import io.micronaut.core.annotation.Nullable;
import io.micronaut.serde.annotation.Serdeable;
import io.micronaut.core.annotation.Introspected;
import java.util.List;
import java.time.OffsetDateTime;
import lombok.Builder;

@Builder(toBuilder = true)
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
    @Nullable Integer classifierVersion,
    @Nullable String rawChecksum
) {
		@Builder(toBuilder = true)
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

		@Builder(toBuilder = true)
    @Serdeable
    public record LicenseDeclaration(
        String license,
        String licenseUri,
        PublisherAffirmation publisherAffirmation
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record PublisherAffirmation(
        String statement,
        boolean confirmed,
        OffsetDateTime confirmedAt,
        String signatory
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Identifier(
        String type,
        String value,
        @Nullable String status
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Title(
        String value,
        String type,
        @Nullable String language
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Contributor(
        String name,
        String role,
        String kind,
        @Nullable List<Identifier> identifiers,
        @Nullable String dates
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Edition(
        String statement
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Publication(
        List<String> place,
        List<String> publisher,
        String date,
        @Nullable Integer year,
        String countryCode
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Physical(
        String extent,
        @Nullable String illustrations,
        String dimensions,
        String contentType,
        String mediaType,
        String carrierType,
        @Nullable String format
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Subject(
        String value,
        @Nullable String scheme,
        @Nullable String type
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Series(
        String title
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Relations(
        List<String> otherFormats
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Classification(
        String scheme,
        String value
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Note(
        String type,
        String value
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Rights(
        String rightsStatement,
        String license
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Admin(
        OffsetDateTime created,
        Quality quality,
        List<String> flags
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Quality(
        boolean normalized
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Media(
        @Nullable String url,
        @Nullable String contentType
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record Ext(
        @Nullable SourceHints sourceHints
    ) {}

		@Builder(toBuilder = true)
    @Serdeable
    public record SourceHints(
        @Nullable List<String> titleTokens,
        @Nullable String nameTitlePair
    ) {}
}
