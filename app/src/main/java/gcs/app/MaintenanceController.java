package gcs.app;

import gcs.core.consolidation.ConsolidationService;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;

@Controller("/maintenance")
public class MaintenanceController {

	private final ConsolidationService consolidationService;

	public MaintenanceController(ConsolidationService consolidationService) {
		this.consolidationService = consolidationService;
	}

  @Secured("GCS-ADMIN")
	@Post("/consolidate")
	public void consolidate() {
		consolidationService.consolidate();
	}
}
