package gcs.app;

import io.micronaut.runtime.Micronaut;

/**
 * The main entry point for the Micronaut application.
 */
public class Application {

    public static void main(String[] args) {
        Micronaut.run(Application.class, args);
    }
}
