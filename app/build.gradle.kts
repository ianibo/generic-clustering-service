plugins {
    id("com.github.johnrengelman.shadow") version "8.1.1"
    id("io.micronaut.application") version "4.1.0"
}

version = "0.1"
group = "gcs"

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("io.micronaut:micronaut-http-server")
    implementation("io.micronaut:micronaut-jackson-databind")
    implementation("ch.qos.logback:logback-classic")
    runtimeOnly("org.yaml:snakeyaml")
    annotationProcessor("io.micronaut:micronaut-inject-java")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
}

application {
    mainClass.set("gcs.app.Application")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
