plugins {
    id("com.gradleup.shadow")
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

  annotationProcessor("io.micronaut:micronaut-inject-java")
  annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
  annotationProcessor("org.projectlombok:lombok")

	implementation(project(":core"))

  implementation("io.micronaut.serde:micronaut-serde-api")
	implementation("io.micronaut:micronaut-http-server")
	implementation("io.micronaut:micronaut-jackson-databind")
	implementation("io.micronaut.serde:micronaut-serde-jackson")
	implementation("ch.qos.logback:logback-classic")


  compileOnly("org.projectlombok:lombok")
	runtimeOnly("org.yaml:snakeyaml")

	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
	testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
	testImplementation("io.micronaut.test:micronaut-test-junit5:3.9.0")
	testImplementation("io.micronaut:micronaut-http-client")
	"integrationTestImplementation"("org.testcontainers:junit-jupiter:1.17.6")

  testAnnotationProcessor("io.micronaut:micronaut-inject-java")
  testAnnotationProcessor("io.micronaut.serde:micronaut-serde-processor")
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

val integrationTest by sourceSets.creating {
	java.srcDir("src/integrationTest/java")
	resources.srcDir("src/integrationTest/resources")
	compileClasspath += sourceSets["main"].output + configurations.testRuntimeClasspath.get()
	runtimeClasspath += output + compileClasspath
}

configurations[integrationTest.implementationConfigurationName].extendsFrom(configurations.testImplementation.get())
configurations[integrationTest.runtimeOnlyConfigurationName].extendsFrom(configurations.testRuntimeOnly.get())

val integrationTestTask = tasks.register<Test>("integrationTest") {
	testClassesDirs = integrationTest.output.classesDirs
	classpath = integrationTest.runtimeClasspath
	shouldRunAfter(tasks.test)
	useJUnitPlatform() // if you use JUnit 5
}
