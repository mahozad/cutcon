import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class UpxExtractTask : DefaultTask() {

    @get:InputFile
    abstract val upxArchiveFile: Property<File>

    @get:OutputDirectory
    abstract val extractDirectory: Property<File>

    @TaskAction
    fun execute() {
        // Could instead use project.copy { it.from(project.tarTree...
        // with a workaround: https://github.com/gradle/gradle/issues/15065
        project.exec { exec ->
            exec
                .commandLine(
                    "tar",
                    "xf", upxArchiveFile.get(),
                    "--directory", extractDirectory.get(),
                    "--strip-components=1" // Removes the parent directory from the extraction path
                )
                .workingDir(upxArchiveFile.get().parent)
        }
    }
}
