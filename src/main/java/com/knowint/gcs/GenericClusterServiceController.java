package com.knowint.gcs;

import io.micronaut.http.annotation.*;

@Controller("/genericClusterService")
public class GenericClusterServiceController {

    @Get(uri="/", produces="text/plain")
    public String index() {
        return "Example Response";
    }
}