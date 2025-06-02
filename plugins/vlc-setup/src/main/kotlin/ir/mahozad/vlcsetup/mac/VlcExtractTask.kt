package ir.mahozad.vlcsetup.mac

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.process.ExecOperations
import java.io.File
import java.io.OutputStream
import javax.inject.Inject

abstract class VlcExtractTask @Inject constructor(
    private val execOperations: ExecOperations
) : DefaultTask() {

    @get:InputFile
    abstract val vlcArchiveFile: Property<File>

    @get:OutputDirectory
    val extractDirectory = vlcArchiveFile.map { it.resolveSibling("vlc") }

    private val tempMountDirectory = "vlc-temp"

    @TaskAction
    fun execute() {
        execOperations.exec { exec ->
            // Ignores/throws away the process output to avoid it being shown in Gradle output
            exec.standardOutput = OutputStream.nullOutputStream()
            exec.errorOutput = OutputStream.nullOutputStream()
            exec.commandLine(
                "hdiutil", // TODO: Ensure hdiutil is available on all/most macOS versions
                "attach",
                "-mountpoint", tempMountDirectory,
                vlcArchiveFile.get()
            )
        }
        project.copy { copy ->
            copy.from("$tempMountDirectory/VLC.app/Contents/MacOS/")
            copy.into(extractDirectory)
        }
        execOperations.exec { exec ->
            exec.standardOutput = OutputStream.nullOutputStream()
            exec.errorOutput = OutputStream.nullOutputStream()
            exec.commandLine("hdiutil", "detach", tempMountDirectory)
        }
    }
}
