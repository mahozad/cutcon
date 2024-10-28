import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VlcPreparePluginsLinuxTask : DefaultTask() {

    private val script: File by lazy {
        val destination = temporaryDir.resolve("script.sh")
        javaClass.getResourceAsStream("/script.sh")?.use { input ->
            destination.outputStream().use(input::copyTo)
        }
        destination
    }

    @get:InputDirectory
    abstract val sourceDirectory: Property<File>

    @get:OutputDirectory
    abstract val targetDirectory: Property<File>

    @TaskAction
    fun execute() {
        // TODO: This is so dangerous
        targetDirectory.get().deleteRecursively()
        project.copy { copy ->
            copy.include(

                "usr/lib/libvlc.so",
                "usr/lib/libvlccore.so.9",

                "usr/lib/vlc/plugins/plugins.dat",
                "usr/lib/vlc/plugins/access/libfilesystem_plugin.so",
                "usr/lib/vlc/plugins/audio_filter/libaudio_format_plugin.so",
                "usr/lib/vlc/plugins/audio_filter/libscaletempo_pitch_plugin.so",
                "usr/lib/vlc/plugins/audio_filter/libscaletempo_plugin.so",
                "usr/lib/vlc/plugins/audio_filter/libtrivial_channel_mixer_plugin.so",
                "usr/lib/vlc/plugins/audio_filter/libnormvol_plugin.so",
                "usr/lib/vlc/plugins/audio_output/libadummy_plugin.so",
                "usr/lib/vlc/plugins/audio_output/libafile_plugin.so",
                "usr/lib/vlc/plugins/audio_output/libalsa_plugin.so",
                "usr/lib/vlc/plugins/audio_output/libamem_plugin.so",
                "usr/lib/vlc/plugins/audio_output/libjack_plugin.so",
                "usr/lib/vlc/plugins/audio_output/libpulse_plugin.so",
                "usr/lib/vlc/plugins/codec/libavcodec_plugin.so",
                "usr/lib/vlc/plugins/codec/libopus_plugin.so",
                "usr/lib/vlc/plugins/demux/libts_plugin.so",
                "usr/lib/vlc/plugins/packetizer/libpacketizer_mpeg4audio_plugin.so",
                "usr/lib/vlc/plugins/packetizer/libpacketizer_mpeg4video_plugin.so",
                "usr/lib/vlc/plugins/stream_filter/libcache_read_plugin.so",
                "usr/lib/vlc/plugins/video_chroma/libswscale_plugin.so",
                "usr/lib/vlc/plugins/video_filter/libdeinterlace_plugin.so",
                "usr/lib/vlc/plugins/video_output/**"
            )
            copy.from(sourceDirectory)
            copy.into(targetDirectory)
        }
        project.exec {
            it
                .commandLine("sh", "$script")
                .setStandardInput(System.`in`)
                .workingDir(targetDirectory)
        }
    }
}
