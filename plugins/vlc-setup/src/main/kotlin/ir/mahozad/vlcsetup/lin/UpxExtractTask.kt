package ir.mahozad.vlcsetup.lin

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import javax.inject.Inject

abstract class UpxExtractTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:InputFile
    abstract val upxArchiveFile: Property<File>

    @get:OutputDirectory
    val extractDirectory = upxArchiveFile.map { it.resolveSibling("upx") }

    @TaskAction
    fun execute() {
        // Could instead use project.copy { it.from(project.tarTree...
        // with a workaround: https://github.com/gradle/gradle/issues/15065
        execOperations.exec { exec ->
            exec
                .commandLine(
                    "tar", // TODO: Ensure tar exists on all/most macOS versions
                    "xf", upxArchiveFile.get(),
                    "--directory", extractDirectory.get(),
                    "--strip-components=1" // Removes the parent directory from the extraction path
                )
                .workingDir(upxArchiveFile.get().parent)
        }
    }
}
