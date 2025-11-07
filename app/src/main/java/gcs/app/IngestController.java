package gcs.app;

import gcs.core.IngestService;
import gcs.core.InputRecord;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.security.annotation.Secured;

@Controller("/ingest")
public class IngestController {

	private final IngestService ingestService;

	public IngestController(IngestService ingestService) {
		this.ingestService = ingestService;
	}

  @Secured("GCS-ADMIN")
	@Post
	public InputRecord ingest(@Body InputRecord record) {
		return ingestService.ingest(record);
	}
}
