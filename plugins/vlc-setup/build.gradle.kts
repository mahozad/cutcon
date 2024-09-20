import org.gradle.kotlin.dsl.gradlePlugin

plugins {
    id("org.gradle.java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
}

dependencies {
    implementation("de.undercouch.download:de.undercouch.download.gradle.plugin:5.6.0")
}

gradlePlugin {
    plugins {
        create("vlc-setup") {
            id = "vlc-setup"
            implementationClass = "VlcSetupPlugin"
        }
    }
}
