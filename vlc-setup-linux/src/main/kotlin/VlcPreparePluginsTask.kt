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

    /**
     * Acquired the patchelf file from https://github.com/NixOS/patchelf/releases
     * Could instead have used the chrpath tool (can acquire it by `sudo apt install chrpath`
     * and then finding where its binary is with `whereis chrpath` and copying the file) but chrpath
     * does not work if the file does not already have rpath in it (as in libvlccore.so file).
     */
    private val patchelf: File by lazy {
        val destination = temporaryDir.resolve("patchelf")
        javaClass
            .getResourceAsStream("/patchelf-0.18.0")
            ?.use { input -> destination.outputStream().use(input::copyTo) }
        // See https://stackoverflow.com/a/32331442
        return@lazy destination.apply { setExecutable(true) }
    }

    /**
     * List of libraries:
     *   - libidn.so: needed in Fedora 41
     *     Grab the file from vlc snap itself (extract it and find it) to prevent compatibility errors
     *     with other libraries (for example, extracting the file from the rpm downloaded from
     *     https://rpmfind.net/linux/rpm2html/search.php?query=libidn.so.11()(64bit)
     *     worked on Fedora 41 but did not work on elementary OS 7.1 although the OS worked without this file)
     *
     * NOTE: To extract an rpm file: rpm2cpio file.rpm | cpio -idmv
     */
    private val extraLibraries by lazy {
        // See https://stackoverflow.com/q/11012819
        val uri = javaClass.classLoader.getResource("extra-libs")?.toURI()
            ?: error("Could not initiate loading extra libraries")
        val jar = FileSystems
            .newFileSystem(uri, mapOf<String, String>())
            .getPath("extra-libs")
        val libsPath = temporaryDir.resolve("extra-libs").toPath()
        @OptIn(ExperimentalPathApi::class)
        jar.copyToRecursively(
            target = libsPath,
            overwrite = true,
            followLinks = false
        )
        return@lazy libsPath
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

                "usr/lib/vlc/plugins/access/libfilesystem_plugin.so",
                // Required for audio mute and level to work correctly (at least in Ubuntu 18.04)
                "usr/lib/vlc/plugins/audio_mixer/libfloat_mixer_plugin.so",
                // Required on openSUSE Tumbleweed 2024-11 with Generic desktop (KDE desktop did not  need this)
                "usr/lib/vlc/plugins/audio_filter/libsamplerate_plugin.so",
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
            copy.includeEmptyDirs = false
            copy.eachFile {
                // All the below is to strip usr/lib/ from the target copy directory
                // See https://docs.gradle.org/current/userguide/working_with_files.html#sec:unpacking_archives_example
                it.relativePath = RelativePath(true, *it.relativePath.segments.drop(2).toTypedArray())
            }
        }

        project.copy { copy ->
            copy.from(extraLibraries)
            copy.into(targetDirectory)
        }

        /*
        Could also have instead used the below method by installing the chrpath on system
        and loading the script.sh like how the chrpath is loaded above.
        But, installing chrpath using apt requires sudo and it in turn
        requires user password to be entered on the console/terminal.
            project.exec {
                it
                    .commandLine("sh", "$script")
                    .setStandardInput(System.`in`)
                    .workingDir(targetDirectory)
            }
        script.sh content:
            # Good explanation of rpath/runpath and $ORIGIN:
            # https://unix.stackexchange.com/a/22999

            # To install libraries/programs using apt or apt-get, we need to use sudo
            # and, it in turn, needs the user password which is configured to be read
            # from standard input using the -S option and .setStandardInput(System.in)
            # See https://stackoverflow.com/q/21659637
            sudo -S apt install chrpath

            chrpath -r '$ORIGIN' ./libvlc.so

            # Optional step
            # (removing this step does not seem to affect anything but the
            # rpath of the files in plugins/ will be an absolute non-existent path)
            find ./vlc/plugins/ -type f -name "*.so*" | xargs -n1 chrpath -r '$ORIGIN/../../..'
         */
        targetDirectory
            .get()
            .walk()
            .filter { ".so" in it.name }
            .forEach { file ->
                project.exec {
                    it.setIgnoreExitValue(true) // For files that did not contain rpath
                    it.commandLine(
                        patchelf.absolutePath,
                        "--set-rpath",
                        if ("libvlc" in file.name) {
                            "\$ORIGIN"
                        } else {
                            "\$ORIGIN/../../.."
                        },
                        file.absolutePath
                    )
                }
            }
    }
}
