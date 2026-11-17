plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.plugin.spring)
    alias(libs.plugins.kotlin.plugin.jpa)
    alias(libs.plugins.spring.boot)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(platform(libs.spring.boot.dependencies))
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation(libs.spring.boot.flyway)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)
    runtimeOnly(libs.postgresql)

    testImplementation("org.springframework.boot:spring-boot-starter-test")
    testImplementation(libs.mockk)
    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation(libs.testcontainers.postgresql)
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
}
