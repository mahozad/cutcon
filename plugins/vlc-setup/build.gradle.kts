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
            implementationClass = "ir.mahozad.vlcsetup.VlcSetupPlugin"
            description = """
                Prepares and builds VLC for Compose Multiplatform desktop applications
                (.dll/.so/.dylib plugin files for Windows, Linux, macOS respectively) 
                to be able to implement a self-contained media player with vlcj library
                without requiring VLC to have been installed on the system. 
            """.trimIndent()
            displayName = "VLC Setup"
            website = "https://github.com/mahozad/vlc-setup"
            vcsUrl = "https://github.com/mahozad/vlc-setup"
            tags = listOf(
                "vlc",
                "vlcj",
                "kotlin-multiplatform",
                "compose-multiplatform",
                "media-player",
                "video-player",
                "audio-player"
            )
        }
    }
}
