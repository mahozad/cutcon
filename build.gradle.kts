import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.*

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.buildDownload)
    alias(libs.plugins.buildConfig)
    id("vlc-setup-linux")
}

val appRawFilesPath = rootDir.toPath() / "raw"
val appResourcesPath = rootDir.toPath() / "asset"
val vlcDirectoryName = "vlc"
val releaseDate: LocalDate = LocalDate.of(2024, 2, 29)

group = "ir.mahozad"
version = "1"

vlcSetup {
    vlcVersion = libs.versions.vlc.get()
    linuxCopyPath = (appResourcesPath / "linux" / vlcDirectoryName).toFile()
    shouldCompressPlugins = System.getenv("vlcCompression")?.toBooleanStrictOrNull() ?: true
    shouldIncludeAllPlugins = System.getenv("vlcAllPlugins")?.toBooleanStrictOrNull() ?: false
}

tasks.withType<Test> {
    useJUnitPlatform()
    // See https://github.com/JetBrains/compose-multiplatform/issues/3244
    dependsOn("prepareAppResources")
    afterEvaluate {
        systemProperty(
            "compose.application.resources.dir",
            tasks.getByName<Sync>("prepareAppResources").destinationDir
        )
    }
}

@OptIn(ExperimentalPathApi::class)
tasks.clean {
    delete += appResourcesPath
        .walk(PathWalkOption.INCLUDE_DIRECTORIES)
        .filter(Path::isDirectory)
        .filter { it.name == vlcDirectoryName }
}

/**
 * See the buildconfig plugin above
 * and https://github.com/gmazzo/gradle-buildconfig-plugin
 * Could also just create a file and read it in the class.
 */
buildConfig {
    packageName("${project.group}.${project.name.lowercase()}")
    buildConfigField("String", "APP_NAME", """"${project.name}"""")
    buildConfigField("String", "APP_VERSION", """"${project.version}"""")
    buildConfigField("String", "VLC_DIRECTORY_NAME", """"$vlcDirectoryName"""")
    buildConfigField(
        "java.time.LocalDate",
        "APP_RELEASE_DATE",
        """LocalDate.of(${releaseDate.year}, ${releaseDate.monthValue}, ${releaseDate.dayOfMonth})"""
    )
}

kotlin {
    jvm(name = "desktop")

    sourceSets {
        val desktopMain by getting
        val desktopTest by getting
        val uiTest by creating { dependsOn(desktopTest) }
        desktopMain.dependencies {
            // Alternative icon packs:
            // https://github.com/DevSrSouza/compose-icons: FontAwesome and so on
            // https://github.com/microsoft/fluentui-system-icons: Has Android drawable files
            // which now can be used in Compose Multiplatform with the new resources library
            implementation(compose.materialIconsExtended)
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.material3)
            implementation(compose.ui)
            @OptIn(org.jetbrains.compose.ExperimentalComposeLibrary::class)
            implementation(compose.components.resources)
            implementation(compose.preview)
            implementation(libs.kotlin.coroutines.core)
            implementation(libs.mahozad.wavySlider)
            // Version 1.4.5 crashed the app when running app exe.
            // Requires modules("java.naming") in compose { nativeDistribution block below.
            // See https://github.com/JetBrains/compose-multiplatform/issues/1358
            // and https://github.com/JetBrains/compose-multiplatform/issues/1977
            implementation(libs.logback.classic)
            implementation(libs.kotlinLogging)
            // Fixes libraries that use log4j for logging (for example, apache poi)
            // Maybe could use log4j-over-slf4j library instead
            // See https://www.slf4j.org/legacy.html#log4j-over-slf4j
            implementation(libs.log4jToSlf4j)
            implementation(libs.persianDateTime)
            implementation(libs.vlcj)
            implementation(libs.apache.tika)
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlin.coroutines.swing)
            implementation(libs.jAudioTagger)
            implementation(libs.jna.jpms)
            implementation(libs.jna.platform.jpms)
            implementation(libs.ffmpeg) // The main artifact
            // Cannot use version catalog containing artifact classifier
            // See https://github.com/gradle/gradle/issues/17169
            // and https://stackoverflow.com/q/71485996
            implementation(
                dependencies.variantOf(libs.ffmpeg) {
                    if (currentOS == OS.WINDOWS) {
                        classifier("windows-x86_64-gpl")
                    } else {
                        classifier("linux-x86_64-gpl")
                    }
                }
            )
        }
        desktopTest.dependencies {
            implementation(compose.desktop.uiTestJUnit4)
            implementation(libs.junit5)
            // Includes the Vintage engine to be able to run JUnit 4 tests as well
            implementation(libs.junit5.vintageEngine)
            implementation(libs.assertj)
            implementation(libs.mockk)
            implementation(libs.imageComparison)
            implementation(libs.kotlin.coroutines.test)
        }
    }

    // With toolchain, we say, no matter what JDK is used for running Gradle itself,
    // we want this specific JDK for building/compiling our code (aka tasks of our library/app).
    // So, if Gradle finds a local JDK matching the specified toolchain properties,
    // it will use it to compile our library/app code. Otherwise, it will download a proper JDK.
    // Additionally, if we haven't set source and target compatibility of our code explicitly,
    // Gradle configures source and target compatibility to be equal to the toolchain ones.
    // Note that setting a toolchain via the kotlin extension
    // updates the toolchain for Java compile tasks as well.
    // See https://kotlinlang.org/docs/gradle-configure-project.html
    // and https://blog.gradle.org/java-toolchains
    // and https://blog.jetbrains.com/kotlin/2021/11/gradle-jvm-toolchain-support-in-the-kotlin-plugin/
    // and https://kotlinlang.org/docs/gradle-compiler-options.html#all-compiler-options
    jvmToolchain(libs.versions.jvm.get().toInt())
}

// val uiTest = task<Test>("uiTest") {
//     description = "Runs UI tests."
//     group = "verification"
//
//     testClassesDirs = sourceSets["uiTest"].output.classesDirs
//     classpath = sourceSets["uiTest"].runtimeClasspath
//     shouldRunAfter("test")
//
//     testLogging {
//         events(TestLogEvent.PASSED)
//     }
// }
// tasks.check { dependsOn(uiTest) }

compose.desktop {
    application {
        mainClass = "ir.mahozad.cutcon.MainKt"
        /**
         * Java supports showing native OS splash screen with `-splash` JVM argument.
         * So, here an image is set to be shown as native OS splash screen.
         * It works when the app is launched with its installed exe or one of run*Distributable tasks.
         * To also set the splash for application uber jar (created with one of package*UberJar* tasks),
         * copy the splash image into classpath (like src/main/resources directory)
         * and update all the Jar tasks in the build script like below:
         *
         * ```kotlin
         * tasks.withType<Jar> {
         *     manifest {
         *         attributes["SplashScreen-Image"] = "image.png"
         *     }
         * }
         * ```
         *
         * See https://github.com/JetBrains/compose-multiplatform/issues/3233
         * and https://docs.oracle.com/javase/tutorial/uiswing/misc/splashscreen.html
         * and https://learn.microsoft.com/en-us/windows/uwp/launch-resume/add-a-splash-screen
         *
         * The splash image can have transparency (PNG and GIF are supported).
         * It seems that the Windows OS does not respect the GIF speed and no-loop settings and
         * plays the animation at a low frame rate. See https://stackoverflow.com/q/25382400
         */
        jvmArgs += "-splash:app/resources/splash-screen.gif"
        nativeDistributions {
            // java.naming is to prevent app crash when running the app exe
            // (introduced with ch.qos.logback:logback-classic version 1.4.5)
            // jdk.unsupported is for showing the display image when running the app exe
            modules("java.naming", "jdk.unsupported")
            targetFormats(Dmg, Msi, Exe, Deb)
            packageVersion = "${project.version}.0.0"
            packageName = project.name
            vendor = "Mahdi Hosseinzadeh"
            windows {
                iconFile = (appRawFilesPath / "logo.ico").toFile()
                menuGroup = project.name // Start menu shortcut
            }
            appResourcesRootDir = appResourcesPath.toFile()
            buildTypes.release.proguard {
                version = libs.versions.proguard.get()
                configurationFiles.from("rules.pro")
            }
        }
    }
}

// Adopted from https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/internal/utils/osUtils.kt
enum class OS { WINDOWS, LINUX }
val currentOS: OS by lazy {
    val os = System.getProperty("os.name")
    when {
        os.startsWith("Win", ignoreCase = true) -> OS.WINDOWS
        os.startsWith("Linux", ignoreCase = true) -> OS.LINUX
        else -> error("Unsupported OS")
    }
}

tasks.wrapper {
    gradleVersion = libs.versions.gradle.get()
    networkTimeout = 60_000 // milliseconds
    distributionType = DistributionType.ALL
    validateDistributionUrl = false
}
