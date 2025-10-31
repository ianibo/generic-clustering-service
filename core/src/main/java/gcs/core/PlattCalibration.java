package gcs.core;

/**
 * A simple implementation of Platt scaling for calibrating similarity scores.
 */
public class PlattCalibration implements Calibration {

    private static final double PACCEPT = 0.98;
    private static final double PREJECT = 0.02;

    private double a = -1.0;
    private double b = 0.0;

    @Override
    public double scoreToProb(double s) {
        return 1.0 / (1.0 + Math.exp(a * s + b));
    }

    private double probToScore(double p) {
        return (Math.log(1.0 / p - 1.0) - b) / a;
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
        return new Thresholds(probToScore(PACCEPT), probToScore(PREJECT));
    }
}
