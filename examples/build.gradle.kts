plugins {
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(project(":core"))
    implementation("com.fasterxml.jackson.core:jackson-databind:2.15.2")
}

application {
    mainClass.set("gcs.examples.Cli")
}
