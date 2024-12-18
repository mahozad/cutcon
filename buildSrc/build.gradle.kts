plugins {
    `kotlin-dsl`
    // OR kotlin("jvm") version libs.versions.kotlin.jvm
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

tasks.withType<Test> {
    useJUnitPlatform()
}

dependencies {
    testImplementation(libs.junit5)
    testImplementation(libs.assertj)
    testImplementation(libs.systemStubs.core)
    testImplementation(libs.systemStubs.jupiter)
}
