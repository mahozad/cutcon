package ir.mahozad.vlcsetup.win

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VlcFilterPluginsTask : DefaultTask() {

    @get:Input
    abstract val shouldIncludeAllPlugins: Property<Boolean>

    @get:InputDirectory
    abstract val sourceDirectory: Property<File>

    @get:OutputDirectory
    val targetDirectory = sourceDirectory.map { it.resolveSibling("vlc-filtered") }

    @TaskAction
    fun execute() {
        targetDirectory.get().deleteRecursively() // For when shouldIncludeAllPlugins changes from true to false
        project.copy { copy ->
            copy.from(sourceDirectory)
            copy.into(targetDirectory)
            if (shouldIncludeAllPlugins.get()) {
                copy.include("*.dll")
                copy.include("plugins/**/*.dll")
            } else {
                // For testing the effect of including/excluding plugins, use vlc option "--no-plugins-cache"
                // to avoid vlc plugin caches. For example, as an argument to vlcj MediaPlayerFactory.
                copy.include(
                    "libvlc.dll",
                    "libvlccore.dll",
                    "plugins/access/libfilesystem_plugin.dll",
                    // Along with audio_output/libmmdevice_plugin.dll normalizes audio loudness
                    "plugins/audio_filter/libnormvol_plugin.dll",
                    // Needed for MP3 (or audio) files with single (mono) channel
                    "plugins/audio_filter/libtrivial_channel_mixer_plugin.dll",
                    // Needed for FLAC audio files
                    "plugins/audio_filter/libaudio_format_plugin.dll",
                    "plugins/audio_filter/libscaletempo_pitch_plugin.dll",
                    "plugins/audio_filter/libscaletempo_plugin.dll",
                    "plugins/audio_output/libdirectsound_plugin.dll",
                    // Multimedia device output (along with audio_filter/libnormvol_plugin.dll normalizes audio loudness)
                    "plugins/audio_output/libmmdevice_plugin.dll",
                    // Various audio and video decoders/encoders delivered by the FFmpeg library.
                    // When almost all other DLLs were available (not deleted), I deleted this file and
                    // the player still worked but the video flickered (when pausing and sometimes during playback)
                    "plugins/codec/libavcodec_plugin.dll",
                    // For Opus audio format (for example, in 4K MKV videos downloaded from YouTube)
                    "plugins/codec/libopus_plugin.dll",
                    "plugins/demux/libts_plugin.dll",
                    "plugins/packetizer/libpacketizer_mpeg4audio_plugin.dll",
                    "plugins/packetizer/libpacketizer_mpeg4video_plugin.dll",
                    // For speedup of live-stream video to work correctly and smoothly; speedup of video files works without this
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
        }
    }
}
