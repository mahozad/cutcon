package ir.mahozad.vlcsetup.mac

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

            copy.include("lib/libvlc.dylib")
            copy.include("lib/libvlccore.dylib")
            if (shouldIncludeAllPlugins.get()) {
                copy.include("plugins/**")
            } else {
                // For testing the effect of including/excluding plugins, use vlc option "--no-plugins-cache"
                // to avoid vlc plugin caches. For example, as an argument to vlcj MediaPlayerFactory.
                copy.include(
                    // Needed to play audio of a TS video
                    "plugins/libauhal_plugin.dylib",
                    "plugins/libfilesystem_plugin.dylib",
                    "plugins/libnormvol_plugin.dylib",
                    "plugins/libtrivial_channel_mixer_plugin.dylib",
                    "plugins/libaudio_format_plugin.dylib",
                    "plugins/libscaletempo_pitch_plugin.dylib",
                    "plugins/libscaletempo_plugin.dylib",
                    "plugins/libavcodec_plugin.dylib",
                    "plugins/libopus_plugin.dylib",
                    "plugins/libts_plugin.dylib",
                    "plugins/libpacketizer_mpeg4audio_plugin.dylib",
                    "plugins/libpacketizer_mpeg4video_plugin.dylib",
                    "plugins/libcache_read_plugin.dylib",
                    "plugins/libswscale_plugin.dylib",
                    "plugins/libdeinterlace_plugin.dylib",
                    "plugins/libvmem_plugin.dylib"
                )
            }
        }
    }
}
