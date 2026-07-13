plugins {
    kotlin("jvm") version "1.9.25"
    // Deliberately NOT applying the kotlin("plugin.spring") or Spring Boot plugins.
    // The compiler will reject any code in this module that imports Spring.
}

group = "com.hafiz5007"
version = "0.1.0-SNAPSHOT"

java {
    toolchain { languageVersion.set(JavaLanguageVersion.of(21)) }
}

repositories {
    mavenCentral()
}

dependencies {
    // Kotlin standard library is added implicitly by the Kotlin plugin.

    // The one framework dep the domain needs: an established, well-tested
    // string-similarity library. Kept small (no reflection, no Spring, no HTTP).
    implementation("info.debatty:java-string-similarity:2.0.0")

    // SLF4J API only — no binding. The consuming app provides the binding
    // (logback via Spring Boot). Lets the domain log without importing Spring.
    implementation("org.slf4j:slf4j-api:2.0.16")

    // Test
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.3")
    testImplementation("io.mockk:mockk:1.13.13")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
