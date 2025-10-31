import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.configure

plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1" apply false
    id("io.micronaut.application") version "4.1.0" apply false
}

subprojects {
    apply(plugin = "java-library")

    repositories {
        mavenCentral()
    }

    extensions.configure<JavaPluginExtension> {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
}
