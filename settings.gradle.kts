rootProject.name="GenericClusterService"

include("core", "app", "examples")

pluginManagement {
  plugins {
    id("io.micronaut.application") version "4.5.4"
    id("com.gradleup.shadow") version "8.3.7"
    id("io.micronaut.library") version "4.5.4"
    id("io.micronaut.aot") version "4.5.4"
    id("org.asciidoctor.jvm.convert") version "3.3.2"
  }
}
