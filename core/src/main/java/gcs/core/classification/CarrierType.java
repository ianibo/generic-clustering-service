package gcs.core.classification;

/**
 * Represents the RDA Carrier Type (MARC 338), a categorization reflecting the format of the
 * storage medium and housing of a carrier.
 *
 * @see <a href="https://www.loc.gov/standards/sourcelist/marc-source-list.html#carrier-type">RDA Carrier Type</a>
 */
public enum CarrierType {
    VOLUME,
    AUDIO_DISC,
    VIDEODISC,
    ONLINE_RESOURCE,
    MICROFICHE,
    SHEET
}
