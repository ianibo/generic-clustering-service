package gcs.app;

import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;

import java.util.Collections;
import java.util.Map;

@Controller("/health")
public class HealthController {

    @Get
    public Map<String, Boolean> health() {
        return Collections.singletonMap("ok", true);
    }
}
