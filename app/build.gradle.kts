plugins {
    id("com.github.johnrengelman.shadow")
    id("io.micronaut.application")
}

version = "0.1"
group = "gcs"

repositories {
    mavenCentral()
}

configurations {
    create("integrationTestImplementation") {
        extendsFrom(configurations.testImplementation.get())
    }
    create("integrationTestRuntimeOnly") {
        extendsFrom(configurations.testRuntimeOnly.get())
    }
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
    testImplementation("io.micronaut.test:micronaut-test-junit5:3.9.0")
    testImplementation("io.micronaut:micronaut-http-client")
    "integrationTestImplementation"("org.testcontainers:junit-jupiter:1.17.6")
}

application {
    mainClass.set("gcs.app.Application")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

micronaut {
    runtime("netty")
    testRuntime("junit5")
    processing {
        incremental(true)
        annotations("gcs.app.*")
    }
}

sourceSets {
    create("integrationTest") {
        compileClasspath += sourceSets.main.get().output + sourceSets.test.get().output
        runtimeClasspath += sourceSets.main.get().output + sourceSets.test.get().output
    }
}

tasks.register<Test>("integrationTest") {
    useJUnitPlatform()
    testClassesDirs = sourceSets["integrationTest"].output.classesDirs
    classpath = sourceSets["integrationTest"].runtimeClasspath
}
