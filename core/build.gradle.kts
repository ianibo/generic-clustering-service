plugins {
	id("io.micronaut.library")
}

repositories {
    mavenCentral()
}

dependencies {

	annotationProcessor("io.micronaut:micronaut-inject-java")
	annotationProcessor("io.micronaut.serde:micronaut-serde-processor")
	annotationProcessor("org.projectlombok:lombok")

	implementation("io.micronaut.serde:micronaut-serde-api")

	// JSON runtime (pick one; Jackson is common)
	implementation("io.micronaut.serde:micronaut-serde-jackson")
	implementation("com.fasterxml.jackson.core:jackson-databind")

    implementation("dev.langchain4j:langchain4j:0.32.0")
    implementation("dev.langchain4j:langchain4j-open-ai:0.32.0")

	compileOnly("org.projectlombok:lombok")

	testAnnotationProcessor("io.micronaut:micronaut-inject-java")
	testAnnotationProcessor("io.micronaut.serde:micronaut-serde-processor")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
	testImplementation("org.mockito:mockito-core:5.10.0")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
    testImplementation("io.micronaut.test:micronaut-test-junit5:4.3.0")

}
