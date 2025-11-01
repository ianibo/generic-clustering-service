package gcs.core.classification;

import gcs.core.InputRecord;
import jakarta.inject.Singleton;

@Singleton
public class RuleBasedInstanceClassifier implements InstanceClassifier {

    @Override
    public InstanceClassification classify(InputRecord record) {
        if (record.physical() == null) {
            // Basic fallback for a print book
            return new InstanceClassification(
                ContentType.TEXT,
                MediaType.UNMEDIATED,
                CarrierType.VOLUME,
                "UNKNOWN",
                "UNKNOWN",
                "UNKNOWN"
            );
        }

        InputRecord.Physical physical = record.physical();

        ContentType contentType = parseContentType(physical.contentType());
        MediaType mediaType = parseMediaType(physical.mediaType());
        CarrierType carrierType = parseCarrierType(physical.carrierType());
        String formatDetail = detectFormatDetail(physical.format());
        String modality = detectModality(record.edition());
        String objectClass = detectObjectClass(physical.contentType());

        return new InstanceClassification(contentType, mediaType, carrierType, formatDetail, modality, objectClass);
    }

    private ContentType parseContentType(String s) {
        if (s == null) return ContentType.UNKNOWN;
        return switch (s.toLowerCase()) {
            case "text" -> ContentType.TEXT;
            case "two-dimensional moving image" -> ContentType.TWO_DIMENSIONAL_MOVING_IMAGE;
            case "tactile text" -> ContentType.TACTILE_TEXT;
            case "still image" -> ContentType.STILL_IMAGE;
            case "audio" -> ContentType.AUDIO;
            default -> ContentType.UNKNOWN;
        };
    }

    private MediaType parseMediaType(String s) {
        if (s == null) return MediaType.UNKNOWN;
        return switch (s.toLowerCase()) {
            case "unmediated" -> MediaType.UNMEDIATED;
            case "audio" -> MediaType.AUDIO;
            case "computer" -> MediaType.COMPUTER;
            case "video" -> MediaType.VIDEO;
            case "microform" -> MediaType.MICROFORM;
            case "tactile" -> MediaType.TACTILE;
            default -> MediaType.UNKNOWN;
        };
    }

    private CarrierType parseCarrierType(String s) {
        if (s == null) return CarrierType.UNKNOWN;
        return switch (s.toLowerCase()) {
            case "volume" -> CarrierType.VOLUME;
            case "audio disc" -> CarrierType.AUDIO_DISC;
            case "videodisc" -> CarrierType.VIDEODISC;
            case "online resource" -> CarrierType.ONLINE_RESOURCE;
            case "microfiche" -> CarrierType.MICROFICHE;
            case "sheet" -> CarrierType.SHEET;
            default -> CarrierType.UNKNOWN;
        };
    }

    private String detectFormatDetail(String format) {
        if (format == null) return "UNKNOWN";
        String lowerFormat = format.toLowerCase();
        if (lowerFormat.contains("epub")) return "EPUB";
        if (lowerFormat.contains("pdf")) return "PDF";
        if (lowerFormat.contains("dvd-video")) return "DVD-Video";
        if (lowerFormat.contains("dvd")) return "DVD-Video";
        if (lowerFormat.contains("blu-ray")) return "Blu-ray";
        return format;
    }

    private String detectModality(InputRecord.Edition edition) {
        if (edition == null || edition.statement() == null) return "UNKNOWN";
        String statement = edition.statement().toLowerCase();
        if (statement.contains("large print")) return "largePrint";
        if (statement.contains("braille")) return "braille";
        return "UNKNOWN";
    }

    private String detectObjectClass(String contentType) {
        if (contentType != null && contentType.equalsIgnoreCase("human-made object")) {
            return "E22 Human-Made Object";
        }
        return "UNKNOWN";
    }
}
