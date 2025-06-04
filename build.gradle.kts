import ir.mahozad.cutcon.OS
import ir.mahozad.cutcon.getCurrentOs
import ir.mahozad.cutcon.toBooleanOrNull
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*
import java.time.LocalDate
import kotlin.io.path.div

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.buildConfig)
    alias(libs.plugins.vlcSetup)
    id("ui-test-setup")
}

val appResourcesPath = rootDir.toPath() / "src" / "asset"
val vlcDirectoryName = "vlc"
val releaseDate: LocalDate = LocalDate.of(2024, 7, 21)

group = "ir.mahozad"
version = "3"

vlcSetup {
    vlcVersion = libs.versions.vlc.get()
    shouldCompressVlcFiles = System.getenv("vlcCompression").toBooleanOrNull() ?: true
    shouldIncludeAllVlcFiles = System.getenv("vlcAllPlugins").toBooleanOrNull() ?: false
    pathToCopyVlcLinuxFilesTo = (appResourcesPath / "linux" / vlcDirectoryName).toFile()
    pathToCopyVlcMacosFilesTo = (appResourcesPath / "macos" / vlcDirectoryName).toFile()
    pathToCopyVlcWindowsFilesTo = (appResourcesPath / "windows" / vlcDirectoryName).toFile()
}

/**
 * See the buildconfig plugin above
 * and https://github.com/gmazzo/gradle-buildconfig-plugin
 * Could also just create a file and read it in the class.
 */
buildConfig {
    packageName("${project.group}.${project.name.lowercase()}")
    buildConfigField(name = "APP_NAME", value = project.name)
    buildConfigField(name = "APP_VERSION", value = "${project.version}")
    buildConfigField(name = "VLC_DIRECTORY_NAME", value = vlcDirectoryName)
    buildConfigField(
        name = "APP_RELEASE_DATE",
        type = LocalDate::class.qualifiedName!!,
        expression = """LocalDate.parse("$releaseDate")"""
    )
}

kotlin {
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

/**
 * Modifies the process[Main]Resources task to copy the changelog file.
 */
tasks.processResources {
    from(rootDir.toPath() / "CHANGELOG.md")
}

tasks.withType<Test> {
    useJUnitPlatform()
    // See https://github.com/JetBrains/compose-multiplatform/issues/3244
    dependsOn("prepareAppResources")
    project.afterEvaluate {
        systemProperty(
            "compose.application.resources.dir",
            project.tasks.named("prepareAppResources", Sync::class.java).get().destinationDir
        )
    }
    // See the jvmArgs in compose.desktop.application block below for why
    jvmArgs("--add-opens=java.base/java.nio=ALL-UNNAMED")
}

tasks.wrapper {
    gradleVersion = libs.versions.gradle.get()
    networkTimeout = 60_000 // milliseconds
    distributionType = DistributionType.ALL
    validateDistributionUrl = false
}

dependencies {
    implementation(compose.desktop.currentOs)
    implementation(compose.components.resources)
    // Alternative icon packs:
    // https://github.com/DevSrSouza/compose-icons: FontAwesome and so on
    // https://github.com/microsoft/fluentui-system-icons: Has Android drawable files
    // which now can be used in Compose Multiplatform with the new resources library
    implementation(compose.materialIconsExtended)
    implementation(libs.kotlin.coroutines.core)
    implementation(libs.kotlin.coroutines.swing)
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
    implementation(libs.ffmpeg) // The main artifact
    // Cannot use version catalog containing artifact classifier
    // See https://github.com/gradle/gradle/issues/17169
    // and https://stackoverflow.com/q/71485996
    implementation(variantOf(libs.ffmpeg) {
        if (getCurrentOs() == OS.WINDOWS) {
            classifier("windows-x86_64-gpl")
        } else if (getCurrentOs() == OS.MAC) {
            // FIXME: FFmpeg 4.4-1.5.6 worked but FFmpeg 6.1.1-1.5.10 and 5.0-1.5.7 did not work
            //  in macOS 10.13.6 (High Sierra) (which is no longer supported by Apple) (installed on VirtualBox)
            //  and threw the same exception as in https://github.com/bytedeco/javacv/issues/2018
            //  The probable solution is to install libxcb with `brew install libxcb`
            //  see https://github.com/bytedeco/javacv/issues/1073
            classifier("macosx-x86_64-gpl")
        } else if (getCurrentOs() == OS.LINUX) {
            // FIXME: FFmpeg 6.1.1-1.5.10 did not work in Kubuntu 18.04 (installed on VirtualBox)
            //  and throws the following exception because the distro GLIBC version is 2.27 (can get using `ldd --version` command):
            //  UnsatisfiedLinkError: no jniavutil in java.library.path
            //  ... libc.so.6: version `GLIBC_2.28' not found required by .../libavutil.so.58
            //  See https://blog.jetbrains.com/idea/2025/01/updated-system-requirements-for-linux-gnu-c-library-glibc/
            //  and https://github.com/bytedeco/javacpp-presets/issues/1379
            //  and https://github.com/bytedeco/javacpp-presets/issues/22
            //  and https://github.com/bytedeco/javacv/issues/2203#issuecomment-2456429199
            classifier("linux-x86_64-gpl")
        }
    })
    implementation(libs.vlcj)
    implementation(libs.apache.tika)
    // Alternative: implementation("com.mpatric:mp3agic:0.9.1") but it does not support extracting FLAC cover art etc.
    implementation(libs.jAudioTagger)
    implementation(libs.jna.jpms)
    implementation(libs.jna.platform.jpms)

    testImplementation(compose.desktop.uiTestJUnit4)
    testImplementation(libs.kotlin.coroutines.test)
    testImplementation(libs.junit5)
    // Includes the Vintage engine to be able to run JUnit 4 tests as well
    testImplementation(libs.junit5.vintageEngine)
    testImplementation(libs.assertj)
    testImplementation(libs.mockk)
    testImplementation(libs.imageComparison)
    testImplementation(libs.systemStubs.core)
    testImplementation(libs.systemStubs.jupiter)
}

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
        jvmArgs += "-splash:${'$'}APPDIR/resources/splash-screen.gif"
        /**
         * Needed in the media player to get a VarHandle of a private/package-private field.
         */
        jvmArgs += "--add-opens=java.base/java.nio=ALL-UNNAMED"
        nativeDistributions {
            modules(
                // For preventing app crash when running the app exe
                // (introduced in ch.qos.logback:logback-classic version 1.4.5)
                "java.naming",
                // For showing the display image when running the app exe
                // (introduced in app version 1.5.0 by reimplementing the media player)
                "jdk.unsupported",
                // For playing local files when running the app exe (introduced in CMP version 1.6.0)
                "jdk.accessibility",
                // For launching the app using "Open with" on a file (required by Apache Tika)
                "java.sql"
            )
            targetFormats(Dmg, Msi, Exe, Deb)
            packageVersion = "${project.version}.0.0"
            packageName = project.name
            vendor = "Mahdi Hosseinzadeh"
            windows {
                iconFile = (rootDir.toPath() / "raw" / "logo.ico").toFile()
                menuGroup = project.name // Start menu shortcut
            }
            linux {
                iconFile = (rootDir.toPath() / "raw" / "logo.png").toFile()
            }
            macOS {
                iconFile = (rootDir.toPath() / "raw" / "logo.icns").toFile()
            }
            appResourcesRootDir = appResourcesPath.toFile()
            buildTypes.release.proguard {
                version = libs.versions.proguard.get()
                configurationFiles.from("src/proguard-rules.pro")
            }
        }
    }
}
