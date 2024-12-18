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
    testImplementation("uk.org.webcompere:system-stubs-core:2.1.7")
    testImplementation("uk.org.webcompere:system-stubs-jupiter:2.1.7")
}
