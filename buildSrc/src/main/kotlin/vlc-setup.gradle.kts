import de.undercouch.gradle.tasks.download.Download
import org.gradle.accessors.dm.LibrariesForLibs
import org.gradle.kotlin.dsl.property
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.deleteIfExists
import kotlin.io.path.deleteRecursively
import kotlin.io.path.div
import kotlin.io.path.extension
import kotlin.io.path.walk

// See https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html#sec:applying_external_plugins
// An easy and informative way to debug the tasks and why they are up to date or not is to add
// --info to the gradle command so that it shows the info logs (such as why a task was skipped)

plugins {
    id("de.undercouch.download")
}

// See https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

/**
 * Could have instead made this an interface and set the
 *  default values after crating an instace like this:
 *  vlcSetup.shouldCompressPlugins.convention(true)
 *
 * See https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/desktop/application/dsl/ProguardSettings.kt
 * and https://github.com/gradle/gradle/issues/8423
 */
abstract class VlcSetupExtension @Inject constructor(project: Project) {
    companion object { val pluginName = "vlcSetup" }
    val defaultTempDownloadPath = project.gradle.gradleUserHomeDir.toPath() / pluginName
    val versionToUse = project.objects.property<String>().value("3.0.21")
    val tempDownloadPath = project.objects.property<Path?>().value(defaultTempDownloadPath)
    val windowsTargetPath = project.objects.property<Path?>().value(null)
    val shouldCompressPlugins = project.objects.property<Boolean>().value(true)
    val shouldIncludeAllPlugins = project.objects.property<Boolean>().value(false)
}

val vlcSetup = project.extensions.create(
    name = VlcSetupExtension.pluginName,
    type = VlcSetupExtension::class
)

val downloadVlc by tasks.register(
    name = "downloadVlc",
    type = Download::class
) {
    val baseUrl = "https://get.videolan.org"
    val version = vlcSetup.versionToUse.get()
    // Make sure to download the 64-bit version of VLC
    src("$baseUrl/vlc/$version/win64/vlc-$version-win64.zip")
    dest((vlcSetup.tempDownloadPath.get() / "vlc-$version.zip").toFile())
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
        // See https://docs.gradle.org/current/userguide/working_with_files.html#sec:unpacking_archives_example
        eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray()) }
        includeEmptyDirs = false // Deletes empty remainder directories
    }
    into(vlcSetup.tempDownloadPath.get() / "unzipped")
}

val downloadUpx by tasks.register(
    name = "downloadUpx",
    type = Download::class
) {
    val version = libs.versions.upx.get()
    src("https://github.com/upx/upx/releases/download/v$version/upx-$version-win64.zip")
    dest((vlcSetup.tempDownloadPath.get() / "upx-$version.zip").toFile())
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
        // See https://docs.gradle.org/current/userguide/working_with_files.html#sec:unpacking_archives_example
        eachFile { relativePath = RelativePath(true, *relativePath.segments.drop(1).toTypedArray()) }
        includeEmptyDirs = false // Deletes empty remainder directories
    }
    include("**/upx.exe")
    into(vlcSetup.tempDownloadPath.get().toFile())
}

@OptIn(ExperimentalPathApi::class)
val prepareVlcPlugins by tasks.register(
    name = "prepareVlcPlugins",
    type = Copy::class
) {
    check(vlcSetup.windowsTargetPath.isPresent) {
        "Specify ${vlcSetup::windowsTargetPath.name} in ${VlcSetupExtension.pluginName}{}"
    }
    inputs.property(vlcSetup::shouldCompressPlugins.name, vlcSetup.shouldCompressPlugins)
    dependsOn(unzipVlc)
    dependsOn(unzipUpx)
    from(unzipVlc.outputs)
    doFirst {
        // If shouldIncludeAllPlugins == true and then in a later execution shouldIncludeAllPlugins == false
        // (i.e. the includes become more restrictive)
        // or if we directly remove include(s) or modify their patterns in a way that removes some files, then
        // the task will not remove those now-not-needed files. This is for these scenarios.
        // Do NOT use vlcSetup.windowsTargetPath.get().deleteRecursively() as it is so dangerous
        vlcSetup
            .windowsTargetPath
            .get()
            .walk()
            .filter { it.extension == "dll" }
            .forEach { it.deleteIfExists() }
    }
    if (vlcSetup.shouldIncludeAllPlugins.get()) {
        include("*.dll")
        include("plugins/**/*.dll")
    } else {
        include(
            "libvlc.dll",
            "libvlccore.dll",
            "plugins/access/libfilesystem_plugin.dll",
            // Along with audio_output/libmmdevice_plugin.dll normalizes audio loudness
            "plugins/audio_filter/libnormvol_plugin.dll",
            // Needed for MP3 (or audio) files with single (mono) channel
            "plugins/audio_filter/libtrivial_channel_mixer_plugin.dll",
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
    if (vlcSetup.shouldCompressPlugins.get()) {
        eachFile {
            ProcessBuilder()
                .command("${vlcSetup.tempDownloadPath.get() / "upx.exe"}", "unzipped/$path")
                .directory(vlcSetup.tempDownloadPath.get().toFile())
                .start()
                .inputReader()
                .forEachLine(logger::info)
        }
    }
    into(vlcSetup.windowsTargetPath.get())
}

/**
 * See https://docs.gradle.org/current/userguide/working_with_files.html
 */
tasks
    .withType(Sync::class)
    .matching { it.name == "prepareAppResources" }
    .all { dependsOn(prepareVlcPlugins) }

tasks.withType<Test> {
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
tasks.named("clean", type = Delete::class) {
    delete += setOf(
        vlcSetup.tempDownloadPath.get() / "unzipped",
        vlcSetup.windowsTargetPath.get()
    )
}
