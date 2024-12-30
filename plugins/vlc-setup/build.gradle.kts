plugins {
    alias(libs.plugins.gradle.pluginDevelopment)
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    implementation(libs.download.gradlePlugin)
    testImplementation(libs.kotlin.gradlePlugin)
    testImplementation(libs.compose.gradlePlugin)
    testImplementation(libs.junit5)
    testImplementation(libs.assertj)
    testImplementation(libs.systemStubs.core)
    testImplementation(libs.systemStubs.jupiter)
}

tasks.withType<Test> {
    useJUnitPlatform()
}

gradlePlugin {
    plugins {
        create("vlc-setup") {
            id = "vlc-setup"
            implementationClass = "VlcSetupPlugin"
        }
    }
}
