package gcs.core.classification;

/**
 * Represents the RDA Content Type (MARC 336), which describes the form of communication through
 * which a work is expressed.
 *
 * @see <a href="https://www.loc.gov/standards/sourcelist/marc-source-list.html#content-type">RDA Content Type</a>
 */
public enum ContentType {
	TEXT,
	TWO_DIMENSIONAL_MOVING_IMAGE,
	TACTILE_TEXT,
	STILL_IMAGE,
	AUDIO,
	COMPUTER_PROGRAM,
	CARTOGRAPHIC_DATA,
	NOTATED_MUSIC,
	UNKNOWN
}
