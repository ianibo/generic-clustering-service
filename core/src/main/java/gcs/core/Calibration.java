package gcs.core;

/**
 * A service for calibrating similarity scores to probabilities.
 */
public interface Calibration {

    /**
     * The thresholds for the calibration.
     * @param high The high threshold.
     * @param low The low threshold.
     */
    record Thresholds(double high, double low) {}

    /**
     * Converts a similarity score to a probability.
     * @param s The similarity score.
     * @return The probability.
     */
    double scoreToProb(double s);

    /**
     * Updates the calibration with a new data point.
     * @param s The similarity score.
     * @param isDup True if the pair is a duplicate, false otherwise.
     */
    void update(double s, boolean isDup);

    /**
     * Returns the current thresholds.
     * @return The current thresholds.
     */
    Thresholds thresholds();
}
