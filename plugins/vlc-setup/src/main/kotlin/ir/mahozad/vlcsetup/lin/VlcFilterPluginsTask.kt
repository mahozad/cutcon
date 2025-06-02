package ir.mahozad.vlcsetup.lin

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
            copy.include("*.so*")
            if (shouldIncludeAllPlugins.get()) {
                copy.include("vlc/plugins/**/*.so")
            } else {
                // For testing the effect of including/excluding plugins, use vlc option "--no-plugins-cache"
                // to avoid vlc plugin caches. For example, as an argument to vlcj MediaPlayerFactory.
                copy.include(
                    "vlc/plugins/access/libfilesystem_plugin.so",

                    // Required for audio mute and level to work correctly (at least in Ubuntu 18.04)
                    "vlc/plugins/audio_mixer/libfloat_mixer_plugin.so",

                    // Required on openSUSE Tumbleweed 2024-11 with Generic desktop (KDE desktop did not need this)
                    "vlc/plugins/audio_filter/libsamplerate_plugin.so",

                    "vlc/plugins/audio_filter/libaudio_format_plugin.so",
                    "vlc/plugins/audio_filter/libscaletempo_pitch_plugin.so",
                    "vlc/plugins/audio_filter/libscaletempo_plugin.so",
                    "vlc/plugins/audio_filter/libtrivial_channel_mixer_plugin.so",
                    "vlc/plugins/audio_filter/libnormvol_plugin.so",
                    "vlc/plugins/audio_output/libadummy_plugin.so",
                    "vlc/plugins/audio_output/libafile_plugin.so",
                    "vlc/plugins/audio_output/libalsa_plugin.so",
                    "vlc/plugins/audio_output/libamem_plugin.so",
                    "vlc/plugins/audio_output/libjack_plugin.so",
                    "vlc/plugins/audio_output/libpulse_plugin.so",
                    "vlc/plugins/codec/libavcodec_plugin.so",
                    "vlc/plugins/codec/libopus_plugin.so",
                    "vlc/plugins/demux/libts_plugin.so",
                    "vlc/plugins/packetizer/libpacketizer_mpeg4audio_plugin.so",
                    "vlc/plugins/packetizer/libpacketizer_mpeg4video_plugin.so",
                    "vlc/plugins/stream_filter/libcache_read_plugin.so",
                    "vlc/plugins/video_chroma/libswscale_plugin.so",
                    "vlc/plugins/video_filter/libdeinterlace_plugin.so",
                    "vlc/plugins/video_output/**"
                )
            }
        }
    }
}
