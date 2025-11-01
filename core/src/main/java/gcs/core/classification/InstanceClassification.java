package gcs.core.classification;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

/**
 * Represents the detailed classification of a specific instance (Manifestation/Item).
 *
 * @param contentType The RDA content type (e.g., text, still image).
 * @param mediaType The RDA media type (e.g., unmediated, computer).
 * @param carrierType The RDA carrier type (e.g., volume, online resource).
 * @param formatDetail A specific format name (e.g., Blu-ray, EPUB, PDF/A).
 * @param modality The accessibility feature (e.g., large print, braille).
 * @param objectClass The CIDOC-CRM or AAT term for physical objects.
 */
@Serdeable
public record InstanceClassification(
    ContentType contentType,
    MediaType mediaType,
    CarrierType carrierType,
    String formatDetail,
    String modality,
    String objectClass
) {}
