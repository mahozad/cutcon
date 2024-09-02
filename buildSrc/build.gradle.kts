plugins {
    `kotlin-dsl`
    // OR kotlin("jvm") version libs.versions.kotlin.jvm
}

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    // See https://github.com/gradle/gradle/issues/15383
    // and https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html#sec:applying_external_plugins
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
    implementation("de.undercouch:gradle-download-task:${libs.versions.buildDownload.get()}")
}
