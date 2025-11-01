package gcs.core.classification;

/**
 * Represents the RDA Media Type (MARC 337), which reflects the general type of intermediation
 * device required to view, play, run, etc., the content of a resource.
 *
 * @see <a href="https://www.loc.gov/standards/sourcelist/marc-source-list.html#media-type">RDA Media Type</a>
 */
public enum MediaType {
    UNMEDIATED,
    AUDIO,
    COMPUTER,
    VIDEO,
    MICROFORM,
    TACTILE
}
