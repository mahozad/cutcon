import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import java.io.File
import javax.inject.Inject
import kotlin.sequences.forEach

// See <PROJECT_ROOT>/README.md for more info.
abstract class VlcSetupTask @Inject constructor(
    @get:InputDirectory
    val vlcDirectory: Provider<File>,

    @get:InputDirectory
    val upxDirectory: Provider<File>,

    @get:Input
    val shouldCompressPlugins: Provider<Boolean>,

    @get:Input
    val shouldIncludeAllPlugins: Provider<Boolean>,

    @get:OutputDirectory
    val windowsCopyPath: Provider<File>
) : Copy() {

    init {
        check(windowsCopyPath.isPresent) {
            "${::windowsCopyPath.name} is not specified. Set it in ${VlcSetupExtension.PLUGIN_NAME}{} block."
        }
        // If shouldIncludeAllPlugins == true and then in a later execution shouldIncludeAllPlugins == false
        // (i.e. the includes become more restrictive)
        // or if we directly remove include(s) or modify their patterns in a way that removes some files, then
        // the task will not remove those now-not-needed files. This is for these scenarios.
        // Do NOT use vlcSetup.windowsTargetPath.get().deleteRecursively() as it is so dangerous
        windowsCopyPath
            .get()
            .walk()
            .filter { it.extension == "dll" }
            .forEach { it.delete() }
        from(vlcDirectory)
        into(windowsCopyPath)
        if (shouldIncludeAllPlugins.get()) {
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
        if (shouldCompressPlugins.get()) {
            eachFile { file ->
                ProcessBuilder()
                    .command("${upxDirectory.get().resolve("upx.exe")}", file.path)
                    .directory(vlcDirectory.get())
                    .start()
                    .inputReader()
                    .forEachLine(logger::info)
            }
        }
    }
}
