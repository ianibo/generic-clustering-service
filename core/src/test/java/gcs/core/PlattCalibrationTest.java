package gcs.core;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlattCalibrationTest {

    @Test
    void testThresholds() {
        var calibration = new PlattCalibration();
        var thresholds1 = calibration.thresholds();

        // Update with some data
        for (int i = 0; i < 100; i++) {
            calibration.update(0.95, true);
            calibration.update(0.05, false);
        }

        var thresholds2 = calibration.thresholds();

        // The high threshold should be higher than the low threshold
        assertTrue(thresholds1.high() > thresholds1.low());
        assertTrue(thresholds2.high() > thresholds2.low());
    }
}
