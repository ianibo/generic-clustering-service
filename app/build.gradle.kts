plugins {
	id("com.gradleup.shadow")
	id("io.micronaut.application")
	id("io.micronaut.aot")
}

version = "0.1"
group = "gcs"

repositories {
    mavenCentral()
}

dependencies {

  annotationProcessor("io.micronaut:micronaut-inject-java")
  annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
  annotationProcessor("org.projectlombok:lombok")
  annotationProcessor("io.micronaut.data:micronaut-data-processor")

	implementation(project(":core"))

  implementation("io.micronaut.serde:micronaut-serde-api")
	implementation("io.micronaut:micronaut-http-server")
	implementation("io.micronaut:micronaut-jackson-databind")
	implementation("io.micronaut.serde:micronaut-serde-jackson")
	implementation("ch.qos.logback:logback-classic")
  implementation("io.micronaut.data:micronaut-data-jdbc")
  implementation("io.micronaut.flyway:micronaut-flyway")
  implementation("io.micronaut.sql:micronaut-jdbc-hikari")
  implementation("org.flywaydb:flyway-core")


  compileOnly("org.projectlombok:lombok")
  compileOnly("jakarta.persistence:jakarta.persistence-api")
	runtimeOnly("org.yaml:snakeyaml")
  runtimeOnly("com.h2database:h2")

	testImplementation("io.micronaut.test:micronaut-test-junit5")
  testImplementation("org.mockito:mockito-core:5.10.0")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
	testImplementation("io.micronaut.test:micronaut-test-junit5:3.9.0")
	testImplementation("io.micronaut:micronaut-http-client")

  testAnnotationProcessor("io.micronaut:micronaut-inject-java")
  testAnnotationProcessor("io.micronaut.serde:micronaut-serde-processor")
}

application {
    mainClass.set("gcs.app.Application")
}


java {
	sourceCompatibility = JavaVersion.toVersion("21")
	targetCompatibility = JavaVersion.toVersion("21")
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
    aot {
        // Please review carefully the optimizations enabled below
        // Check https://micronaut-projects.github.io/micronaut-aot/latest/guide/ for more details
        optimizeServiceLoading = false
        convertYamlToJava = false
        precomputeOperations = true
        cacheEnvironment = true
        optimizeClassLoading = true
        deduceEnvironment = true
        optimizeNetty = true
        replaceLogbackXml = true
    }
}
