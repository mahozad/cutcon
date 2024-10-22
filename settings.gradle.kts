pluginManagement {
    includeBuild("plugins/vlc-setup")
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        // Or, simply, jetbrainsCompose() if compose plugin had been applied here
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        // Or, simply, jetbrainsCompose() if compose plugin had been applied here
        maven(url = "https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

rootProject.name = "Cutcon"
