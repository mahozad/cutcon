import de.undercouch.gradle.tasks.download.Download
import org.gradle.accessors.dm.LibrariesForLibs
import java.nio.file.Path
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.div

// See https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html#sec:applying_external_plugins

plugins {
    id("de.undercouch.download")
}

// See https://github.com/gradle/gradle/issues/15383
val libs = the<LibrariesForLibs>()

interface VlcBuilderExtension {
    val versionToUse: Property<String>
    val tempDownloadPath: Property<Path>
    val windowsPluginsPath: Property<Path>
    val shouldCompressPlugins: Property<Boolean>
    val shouldIncludeAllPlugins: Property<Boolean>
}

val vlcBuilder = project
    .extensions
    .create<VlcBuilderExtension>("vlcBuilder")
    .apply {
        versionToUse.convention("3.0.21")
        shouldCompressPlugins.convention(true)
        shouldIncludeAllPlugins.convention(false)
    }

val downloadVlc by tasks.register(
    name = "downloadVlc",
    type = Download::class
) {
    val baseUrl = "https://get.videolan.org"
    val version = vlcBuilder.versionToUse.get()
    // Make sure to download the 64-bit version of VLC
    src("$baseUrl/vlc/$version/win64/vlc-$version-win64.zip")
    dest((vlcBuilder.tempDownloadPath.get() / "vlc-$version.zip").toFile())
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
    into(vlcBuilder.tempDownloadPath.get() / "uncompressed")
}

val downloadUpx by tasks.register(
    name = "downloadUpx",
    type = Download::class
) {
    val version = libs.versions.upx.get()
    src("https://github.com/upx/upx/releases/download/v$version/upx-$version-win64.zip")
    dest((vlcBuilder.tempDownloadPath.get() / "upx-$version.zip").toFile())
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
    into(vlcBuilder.tempDownloadPath.get().toFile())
}

// See <PROJECT_ROOT>/README.md for more info.
tasks.register(
    name = "prepareVlcPlugins",
    type = Copy::class
) {
    dependsOn(unzipVlc)
    dependsOn(unzipUpx)
    from(unzipVlc.outputs)
    // If shouldIncludeAllPlugins == true and then in a later execution shouldIncludeAllPlugins == false
    // (i.e. the includes become more restrictive)
    // or if we directly remove include(s) or modify their patterns in a way that removes some files, then
    // the task will not remove those now-not-needed files. For these scenarios, clean the project first
    if (vlcBuilder.shouldIncludeAllPlugins.get()) {
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
    if (vlcBuilder.shouldCompressPlugins.get()) {
        eachFile {
            ProcessBuilder()
                .command("${vlcBuilder.tempDownloadPath.get() / "upx.exe"}", "uncompressed/$path")
                .directory(vlcBuilder.tempDownloadPath.get().toFile())
                .start()
                .inputReader()
                .forEachLine(::println)
        }
    }
    into(vlcBuilder.windowsPluginsPath.get())
}

/**
 * See <PROJECT_ROOT>/README.md -> Embedding VLC DLL files section for more info
 * and also https://docs.gradle.org/current/userguide/working_with_files.html
 */
afterEvaluate {
    tasks.named("prepareAppResources") {
        dependsOn("prepareVlcPlugins")
    }
}

tasks.withType<Test> {
    // See <PROJECT_ROOT>/assets/README.md for more info.
    // and https://github.com/JetBrains/compose-multiplatform/issues/3244
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
        vlcBuilder.tempDownloadPath.get() / "uncompressed",
        vlcBuilder.windowsPluginsPath.get()
    )
}
