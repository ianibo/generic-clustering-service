plugins {
	id("java")
	id("io.micronaut.test-resources")
  id("io.micronaut.library")
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

	implementation(project(":app"))
	implementation(project(":core"))

  implementation("io.micronaut.serde:micronaut-serde-api")
	implementation("io.micronaut:micronaut-http-server")
	implementation("io.micronaut:micronaut-jackson-databind")
	implementation("io.micronaut.serde:micronaut-serde-jackson")
	implementation("ch.qos.logback:logback-classic")


  compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.yaml:snakeyaml")

	testImplementation("io.micronaut.test:micronaut-test-junit5")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
	testImplementation("io.micronaut.test:micronaut-test-junit5:3.9.0")
	testImplementation("io.micronaut:micronaut-http-client")

  testAnnotationProcessor("io.micronaut:micronaut-inject-java")
  testAnnotationProcessor("io.micronaut.serde:micronaut-serde-processor")
}

java {
	sourceCompatibility = JavaVersion.toVersion("21")
	targetCompatibility = JavaVersion.toVersion("21")
}

tasks.withType<Test> {
	useJUnitPlatform()
	onlyIf { project.hasProperty("runIT") }
}

// Also keep the conventional 'test' task disabled in this subproject (optional)
tasks.named<Test>("test") {
	enabled = false
}
