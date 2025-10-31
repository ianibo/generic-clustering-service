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
	implementation("com.sangupta:murmur:1.0.0")

	// JSON runtime (pick one; Jackson is common)
	implementation("io.micronaut.serde:micronaut-serde-jackson")

	compileOnly("org.projectlombok:lombok")

	testAnnotationProcessor("io.micronaut:micronaut-inject-java")
	testAnnotationProcessor("io.micronaut.serde:micronaut-serde-processor")
	testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
	testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")

}
