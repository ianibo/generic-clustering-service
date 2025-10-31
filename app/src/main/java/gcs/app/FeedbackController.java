package gcs.app;

import gcs.core.Calibration;
import gcs.core.PlattCalibration;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import jakarta.inject.Singleton;


@Controller("/feedback")
public class FeedbackController {

    private final Calibration calibration;

    public FeedbackController(Calibration calibration) {
        this.calibration = calibration;
    }

    @Post
    public void feedback(@Body FeedbackRequest request) {
        calibration.update(request.score(), request.isDup());
    }

    @Singleton
    public Calibration calibration() {
        return new PlattCalibration();
    }

    public record FeedbackRequest(double score, boolean isDup) {}
}
