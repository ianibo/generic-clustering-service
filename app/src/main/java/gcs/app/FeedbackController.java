package gcs.app;

import gcs.core.Calibration;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;


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

    public record FeedbackRequest(double score, boolean isDup) {}
}
