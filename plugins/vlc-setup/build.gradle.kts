import org.gradle.kotlin.dsl.gradlePlugin

plugins {
    id("org.gradle.java-gradle-plugin")
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
}

dependencies {
    // Can find the id of a gradle plugin as described in https://stackoverflow.com/q/74221701
    implementation("de.undercouch.download:de.undercouch.download.gradle.plugin:5.6.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.11.1")
    testImplementation("org.assertj:assertj-core:3.26.3")
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
