package gcs.app;

import gcs.core.consolidation.ConsolidationService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;

@Controller("/maintenance")
public class MaintenanceController {

    private final ConsolidationService consolidationService;

    public MaintenanceController(ConsolidationService consolidationService) {
        this.consolidationService = consolidationService;
    }

    @Post("/consolidate")
    public void consolidate() {
        consolidationService.consolidate();
    }
}
