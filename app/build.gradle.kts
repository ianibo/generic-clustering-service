plugins {
  id("com.gradleup.shadow")
  id("io.micronaut.application")
  id("io.micronaut.aot")
  id("io.micronaut.test-resources") 
  id("org.graalvm.buildtools.native") version "0.10.3"
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
  runtimeOnly("org.flywaydb:flyway-database-postgresql")
  implementation("io.micronaut.sql:micronaut-jdbc-hikari")
  implementation("org.flywaydb:flyway-core")
  implementation("dev.langchain4j:langchain4j:0.32.0")
  implementation("dev.langchain4j:langchain4j-open-ai:0.32.0")


  compileOnly("org.projectlombok:lombok")
  compileOnly("jakarta.persistence:jakarta.persistence-api")
  runtimeOnly("org.yaml:snakeyaml")
  // runtimeOnly("com.h2database:h2")
  runtimeOnly("org.postgresql:postgresql")
  implementation("com.pgvector:pgvector:0.1.4")
  implementation("io.micronaut.elasticsearch:micronaut-elasticsearch")
  implementation("co.elastic.clients:elasticsearch-java:9.2.0")

  implementation("io.micronaut.security:micronaut-security-jwt")
  implementation("io.micronaut.security:micronaut-security-oauth2")
  implementation("io.micronaut.reactor:micronaut-reactor")

  testImplementation("io.micronaut.test:micronaut-test-junit5")
  testImplementation("org.mockito:mockito-core:5.10.0")

  testImplementation("org.junit.jupiter:junit-jupiter-api:5.10.0")
  testImplementation("org.junit.jupiter:junit-jupiter-params:5.10.0")
  testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.10.0")
  testImplementation("io.micronaut.test:micronaut-test-junit5:3.9.0")
  testImplementation("io.micronaut:micronaut-http-client")
  testImplementation("org.testcontainers:junit-jupiter")
  testImplementation("org.testcontainers:postgresql")
  testImplementation("org.testcontainers:elasticsearch")
  testImplementation("org.testcontainers:testcontainers")
  testCompileOnly("org.projectlombok:lombok")


  testAnnotationProcessor("io.micronaut:micronaut-inject-java")
  testAnnotationProcessor("io.micronaut.serde:micronaut-serde-processor")
  testAnnotationProcessor("org.projectlombok:lombok")
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

graalvmNative {
	binaries {
		named("main") {
			buildArgs.add("--verbose")
			buildArgs.add("-march=x86-64-v2")
			buildArgs.add("-H:+AddAllCharsets")
			buildArgs.add("--initialize-at-build-time=ch.qos.logback.contrib.jackson.JacksonJsonFormatter,org.slf4j.LoggerFactory,ch.qos.logback,kotlin.coroutines.intrinsics.CoroutineSingletons")
			buildArgs.add("--add-opens=java.base/java.nio=ALL-UNNAMED")
			buildArgs.add("--add-opens=java.base/jdk.internal.misc=ALL-UNNAMED")
			buildArgs.add("--add-opens=java.base/jdk.internal.ref=ALL-UNNAMED")
			buildArgs.add("--trace-class-initialization=ch.qos.logback.classic.Logger")
			buildArgs.add("--initialize-at-run-time=io.netty")
			buildArgs.add("-H:ReflectionConfigurationFiles=/home/app/resources/META-INF/native-image/reflect-config.json")
		}
	}
}

tasks {
	dockerfile {
		val javaOpts = listOf(
			"-XX:+UseContainerSupport",
			"-XX:MinRAMPercentage=50.0",
			"-XX:MaxRAMPercentage=80.0",
			"-XX:InitialRAMPercentage=50.0",
			"-XX:+PrintFlagsFinal",
			"-Dcom.sun.net.ssl.checkRevocation=false",
			"--add-exports=java.base/jdk.internal.ref=ALL-UNNAMED",
			"--add-opens=java.base/java.lang=ALL-UNNAMED",
			"--add-opens=java.base/sun.nio.ch=ALL-UNNAMED",
			"--add-opens=java.management/sun.management=ALL-UNNAMED",
			"--add-opens=jdk.management/com.sun.management.internal=ALL-UNNAMED"
		).joinToString(" ")

		environmentVariable("JAVA_OPTIONS", javaOpts)
	}

	dockerfileNative {
		jdkVersion = "21"
	}

	dockerBuild {
		images.set(listOf("docker.hosting.semweb.co/gcs-svc:next"))
	}

	dockerBuildNative {
		images.set(listOf("docker.hosting.semweb.co/gcs-svc:native-next"))
	}
}
