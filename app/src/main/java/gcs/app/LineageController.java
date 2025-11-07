package gcs.app;

import gcs.app.adapters.PgLineageResolver;
import gcs.core.lineage.Lineage;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.PathVariable;
import io.micronaut.security.annotation.Secured;

@Controller("/resolve")
public class LineageController {

	private final PgLineageResolver lineageResolver;

	public LineageController(PgLineageResolver lineageResolver) {
		this.lineageResolver = lineageResolver;
	}

  @Secured("GCS-ADMIN")
	@Get("/{cid}")
	public Lineage resolve(@PathVariable String cid) {
		return lineageResolver.resolve(cid);
	}
}
