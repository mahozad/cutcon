import org.gradle.api.DefaultTask
import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import java.nio.file.FileSystems
import kotlin.io.path.ExperimentalPathApi
import kotlin.io.path.copyToRecursively

abstract class VlcPreparePluginsTask : DefaultTask() {

    @get:InputDirectory
    abstract val sourceDirectory: Property<File>

    @get:OutputDirectory
    abstract val targetDirectory: Property<File>

    @TaskAction
    fun execute() {
        // FIXME: This is so dangerous
        targetDirectory.get().deleteRecursively()
        project.copy { copy ->
            copy.include(
                "files/lib*",
                "files/vlc/plugins/access/libfilesystem_plugin.so",

                // Required for audio mute and level to work correctly (at least in Ubuntu 18.04)
                "files/vlc/plugins/audio_mixer/libfloat_mixer_plugin.so",

                // Required on openSUSE Tumbleweed 2024-11 with Generic desktop (KDE desktop did not need this)
                "files/vlc/plugins/audio_filter/libsamplerate_plugin.so",

                "files/vlc/plugins/audio_filter/libaudio_format_plugin.so",
                "files/vlc/plugins/audio_filter/libscaletempo_pitch_plugin.so",
                "files/vlc/plugins/audio_filter/libscaletempo_plugin.so",
                "files/vlc/plugins/audio_filter/libtrivial_channel_mixer_plugin.so",
                "files/vlc/plugins/audio_filter/libnormvol_plugin.so",
                "files/vlc/plugins/audio_output/libadummy_plugin.so",
                "files/vlc/plugins/audio_output/libafile_plugin.so",
                "files/vlc/plugins/audio_output/libalsa_plugin.so",
                "files/vlc/plugins/audio_output/libamem_plugin.so",
                "files/vlc/plugins/audio_output/libjack_plugin.so",
                "files/vlc/plugins/audio_output/libpulse_plugin.so",
                "files/vlc/plugins/codec/libavcodec_plugin.so",
                "files/vlc/plugins/codec/libopus_plugin.so",
                "files/vlc/plugins/demux/libts_plugin.so",
                "files/vlc/plugins/packetizer/libpacketizer_mpeg4audio_plugin.so",
                "files/vlc/plugins/packetizer/libpacketizer_mpeg4video_plugin.so",
                "files/vlc/plugins/stream_filter/libcache_read_plugin.so",
                "files/vlc/plugins/video_chroma/libswscale_plugin.so",
                "files/vlc/plugins/video_filter/libdeinterlace_plugin.so",
                "files/vlc/plugins/video_output/**"
            )
            copy.from(sourceDirectory)
            copy.into(targetDirectory)
            copy.includeEmptyDirs = false
            copy.eachFile {
                // All the below is to strip files/ from the target copy directory
                // See https://docs.gradle.org/current/userguide/working_with_files.html#sec:unpacking_archives_example
                it.relativePath = RelativePath(true, *it.relativePath.segments.drop(1).toTypedArray())
            }
        }
    }
}
