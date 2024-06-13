import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.gradle.api.tasks.wrapper.Wrapper.DistributionType
import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*
import org.jetbrains.kotlin.cli.common.toBooleanLenient
import java.nio.file.Path
import java.time.LocalDate
import kotlin.io.path.*

plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.buildDownload)
    // Alternative: https://github.com/yshrsmz/BuildKonfig
    alias(libs.plugins.buildConfig)
}

val appRawFilesPath = rootDir.toPath() / "raw"
val appResourcesPath = rootDir.toPath() / "asset"
val vlcDirectoryName = "vlc"
val isVlcFull = System.getenv("fullVlc").toBooleanLenient() ?: false
val shouldMinifyVlc = System.getenv("minifyVlc").toBooleanLenient() ?: true
val releaseDate: LocalDate = LocalDate.of(2024, 2, 29)

group = "ir.mahozad"
version = "1"

sourceSets {
    create("uiTest") {
        // Adds files from the main source set to the compile classpath and runtime classpath of this new source set.
        // sourceSets.main.output is a collection of all the directories containing compiled main classes and resources
        compileClasspath += sourceSets.main.get().output
        runtimeClasspath += sourceSets.main.get().output
    }
}
// Makes the uiTestImplementation configuration extend from testImplementation,
// which means that all the declared dependencies of the test code (and transitively the main as well)
// also become dependencies of this new configuration
val uiTestImplementation by configurations.getting {
    extendsFrom(configurations.testImplementation.get())
}
val uiTest = task<Test>("uiTest") {
    description = "Runs UI tests."
    group = "verification"

    testClassesDirs = sourceSets["uiTest"].output.classesDirs
    classpath = sourceSets["uiTest"].runtimeClasspath
    shouldRunAfter("test")

    testLogging {
        events(TestLogEvent.PASSED)
    }
}
tasks.check { dependsOn(uiTest) }

tasks.processResources {
    from(rootDir.toPath() / "CHANGELOG.md")
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
 * See https://docs.gradle.org/current/userguide/working_with_files.html
 */
afterEvaluate {
    tasks.named("prepareAppResources") {
        dependsOn("prepareVlcPlugins")
    }
}

val downloadVlc by tasks.register(
    name = "downloadVlc",
    type = Download::class
) {
    val baseUrl = "https://get.videolan.org"
    val version = libs.versions.vlc.get()
    // Make sure to download the 64-bit version of VLC
    src("$baseUrl/vlc/$version/win64/vlc-$version-win64.zip")
    dest((appRawFilesPath / "vlc-$version.zip").toFile())
    overwrite(false) // Prevents re-download every time
}

val unzipVlc by tasks.register(
    name = "unzipVlc",
    type = Copy::class
) {
    dependsOn(downloadVlc)
    dependsOn(unzipUpx)
    from(zipTree(downloadVlc.dest)) {
        // All the below is to copy only the contents of the root directory
        // in the archive and not the root directory itself
        // See https://docs.gradle.org/current/userguide/working_with_files.html#ex-unpacking-a-subset-of-a-zip-file
        eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray()) }
        includeEmptyDirs = false // Deletes empty remainder directories
    }
    into(appRawFilesPath / "vlc")
}

val downloadUpx by tasks.register(
    name = "downloadUpx",
    type = Download::class
) {
    val version = libs.versions.upx.get()
    src("https://github.com/upx/upx/releases/download/v$version/upx-$version-win64.zip")
    dest((appRawFilesPath / "upx-$version.zip").toFile())
    overwrite(false) // Prevents re-download every time
}

val unzipUpx by tasks.register(
    name = "unzipUpx",
    type = Copy::class
) {
    dependsOn(downloadUpx)
    from(zipTree(downloadUpx.dest)) {
        // All the below is to copy only the contents of the root directory
        // in the archive and not the root directory itself
        // See https://docs.gradle.org/current/userguide/working_with_files.html#ex-unpacking-a-subset-of-a-zip-file
        eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray()) }
        includeEmptyDirs = false // Deletes empty remainder directories
    }
    include("**/upx.exe")
    into(appRawFilesPath.toFile())
}

tasks.register(
    name = "prepareVlcPlugins",
    type = Copy::class
) {
    dependsOn(unzipVlc)
    dependsOn(unzipUpx)
    from(unzipVlc.outputs)
    // If isVlcFull == true and then in a later execution isVlcFull == false (i.e. includes become more restrictive)
    // or if we directly remove include(s) or modify their patterns in a way that removes some files, then
    // the task will not remove those now-not-needed files. For these scenarios, clean the project first
    if (isVlcFull) {
        include("*.dll")
        include("plugins/**/*.dll")
    } else {
        include(
            "libvlc.dll",
            "libvlccore.dll",
            "plugins/access/libfilesystem_plugin.dll",
            // Along with audio_output/libmmdevice_plugin.dll normalizes audio loudness
            "plugins/audio_filter/libnormvol_plugin.dll",
            "plugins/audio_filter/libscaletempo_pitch_plugin.dll",
            "plugins/audio_filter/libscaletempo_plugin.dll",
            "plugins/audio_output/libdirectsound_plugin.dll",
            // Multimedia device output (along with audio_filter/libnormvol_plugin.dll normalizes audio loudness)
            "plugins/audio_output/libmmdevice_plugin.dll",
            // Various audio and video decoders/encoders delivered by the FFmpeg library.
            // When almost all other DLLs were available (not deleted) I deleted this file and the player still worked but the video flickered (when pausing and sometimes during playback)
            "plugins/codec/libavcodec_plugin.dll",
            "plugins/demux/libts_plugin.dll",
            "plugins/packetizer/libpacketizer_mpeg4audio_plugin.dll",
            "plugins/packetizer/libpacketizer_mpeg4video_plugin.dll",
            // For speedup of live-stream video to work correctly and smoothly; speedup of finished videos works without this
            "plugins/stream_filter/libcache_read_plugin.dll",
            // Can include this to show the text overlay (save path) when taking a screenshot
            // "plugins/text_renderer/libfreetype_plugin.dll",
            "plugins/video_chroma/libswscale_plugin.dll",
            // To de-interlace the video playback; otherwise, not needed
            "plugins/video_filter/libdeinterlace_plugin.dll",
            // Recommended video output for Windows Vista and later
            "plugins/video_output/libdirect3d9_plugin.dll",
            // Recommended video output for Windows 8 and later
            "plugins/video_output/libdirect3d11_plugin.dll",
            // Recommended video output for Windows XP
            "plugins/video_output/libdirectdraw_plugin.dll",
            "plugins/video_output/libdrawable_plugin.dll",
            // This is needed when drawing to a skia bitmap surface in the app code
            "plugins/video_output/libvmem_plugin.dll"
        )
    }
    if (shouldMinifyVlc) {
        eachFile {
            ProcessBuilder()
                .command("${appRawFilesPath / "upx.exe"}", "vlc/$path")
                .directory(appRawFilesPath.toFile())
                .start()
                .inputReader()
                .forEachLine(::println)
        }
    }
    into(appResourcesPath / "windows" / vlcDirectoryName)
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

dependencies {
    implementation(compose.desktop.currentOs)
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
    implementation(variantOf(libs.ffmpeg) { classifier("windows-x86_64-gpl") })
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

tasks.wrapper {
    gradleVersion = libs.versions.gradle.get()
    networkTimeout = 60_000 // milliseconds
    distributionType = DistributionType.ALL
    validateDistributionUrl = false
}
