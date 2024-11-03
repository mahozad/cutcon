import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VlcExtractTask : DefaultTask() {

    @get:InputFile
    abstract val snapFile: Property<File>

    @get:OutputDirectory
    abstract val extractDirectory: Property<File>

    /**
     * Needed at least on Debian 12.7 because it had not this tool pre-installed.
     * Acquired the file from https://launchpad.net/ubuntu/+source/squashfs-tools
     * clicking on one of the builds, and downloading the amd64 deb file and then extracting
     * the deb file using dpkg-deb -xv file.deb extraction-dir and grabbed the unsquasfs file.
     * Also, see https://github.com/plougher/squashfs-tools
     * and https://snapshot.debian.org/package/squashfs-tools/1:4.6.1-1/
     */
    private val unsquashfs: File by lazy {
        val destination = temporaryDir.resolve("unsquashfs")
        javaClass
            .getResourceAsStream("/unsquashfs-4.6.1")
            ?.use { input -> destination.outputStream().use(input::copyTo) }
        // See https://stackoverflow.com/a/32331442
        return@lazy destination.apply { setExecutable(true) }
    }

    @TaskAction
    fun execute() {
        project.exec {
            // See https://askubuntu.com/a/1531222
            it.commandLine(
                unsquashfs.absolutePath,
                "-d", extractDirectory.get().absolutePath,
                "-f", // Prevents failure if the -d already exists as Gradle automatically creates it
                snapFile.get().absolutePath
            )
            // OR
            // it.commandLine(
            //     "file-roller",
            //     "--force",
            //     "--extract-to=${extractDirectory.get().absolutePath}",
            //     snapFile.get().absolutePath
            // )
        }
    }
}
