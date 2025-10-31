plugins {
    id("java-library")
}

repositories {
    mavenCentral()
}

dependencies {
    annotationProcessor("io.micronaut.serde:micronaut-serde-processor:2.2.0")
    implementation("io.micronaut.serde:micronaut-serde-api:2.2.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}
