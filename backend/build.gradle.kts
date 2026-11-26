import org.gradle.testing.jacoco.tasks.JacocoReportBase

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.kotlin.plugin.jpa)
    alias(libs.plugins.spring.boot)
    jacoco
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation(libs.spring.boot.micrometer.tracing.opentelemetry)
    implementation(libs.micrometer.tracing.bridge.otel)
    runtimeOnly(libs.micrometer.registry.prometheus)
    implementation(libs.resilience4j.circuitbreaker)
    implementation(libs.resilience4j.retry)
    implementation(libs.resilience4j.micrometer)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("org.springframework.boot:spring-boot-kafka")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.spring.security.crypto)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)
    implementation(libs.spring.boot.flyway)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    implementation(libs.springdoc.openapi.starter.webmvc.ui)
    runtimeOnly(libs.postgresql)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(libs.mockk)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.kafka)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

springBoot {
    mainClass.set("com.fincaria.FincariaApplicationKt")
}

tasks.named<Test>("test") {
    useJUnitPlatform()
    finalizedBy(tasks.jacocoTestReport)
}

val jacocoCoverageExclusions = listOf("**/FincariaApplication*")

tasks.withType<JacocoReportBase>().configureEach {
    classDirectories.setFrom(
        classDirectories.files.map { fileTree(it) { exclude(jacocoCoverageExclusions) } }
    )
}

tasks.jacocoTestCoverageVerification {
    violationRules {
        rule {
            limit {
                minimum = "0.85".toBigDecimal()
            }
        }
    }
}

tasks.check {
    dependsOn(tasks.jacocoTestCoverageVerification)
}
