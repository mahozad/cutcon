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

    @TaskAction
    fun execute() {
        project.exec {
            // See https://askubuntu.com/a/1531222
            it.commandLine(
                "unsquashfs",
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
