package gcs.core;

/**
 * A simple implementation of Platt scaling for calibrating similarity scores.
 */
public class PlattCalibration implements Calibration {

    private double a = -1.0;
    private double b = 0.0;

    @Override
    public double scoreToProb(double s) {
        return 1.0 / (1.0 + Math.exp(a * s + b));
    }

    @Override
    public void update(double s, boolean isDup) {
        // A simple gradient descent update
        double p = scoreToProb(s);
        double target = isDup ? 1.0 : 0.0;
        double error = target - p;
        a += 0.01 * error * s;
        b += 0.01 * error;
    }

    @Override
    public Thresholds thresholds() {
        // These thresholds are just examples and should be determined empirically
        return new Thresholds(0.9, 0.1);
    }
}
