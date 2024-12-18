package lin

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import org.gradle.internal.logging.progress.ProgressLoggerFactory
import org.gradle.process.ExecOperations
import java.io.File
import java.io.OutputStream
import javax.inject.Inject
import kotlin.math.roundToInt

abstract class VlcCompressPluginsTask @Inject constructor(
    private val execOperations: ExecOperations,
    private val progressLoggerFactory: ProgressLoggerFactory
) : DefaultTask() {

    @get:Input
    abstract val shouldCompressPlugins: Property<Boolean>

    @get:InputDirectory
    abstract val vlcDirectory: Property<File>

    @get:InputDirectory
    abstract val upxDirectory: Property<File>

    @get:OutputDirectory
    val targetDirectory = vlcDirectory.map { it.resolveSibling("vlc-compressed") }

    @TaskAction
    fun execute() {
        targetDirectory.get().deleteRecursively() // For when the filtered plugins become more restrictive
        targetDirectory.get().let(vlcDirectory.get()::copyRecursively)
        if (!shouldCompressPlugins.get()) return // OR throw StopExecutionException()
        // See https://github.com/gradle/gradle/issues/3654
        val progressLogger = progressLoggerFactory.newOperation(VlcCompressPluginsTask::class.java)
        progressLogger.start("Compressing VLC files", name)
        progressLogger.progress("0%")
        val upxPath = upxDirectory.get().resolve("upx").absolutePath
        val vlcFiles = targetDirectory.get().walk().toList()
        for ((index, file) in vlcFiles.withIndex()) {
            // Excludes libvlc and libvlccore because compressing them seems to break the app
            if ("libvlc" in file.name) continue
            file.setExecutable(true) // Required by UPX
            execOperations.exec { exec ->
                // For when upx throws NotCompressibleException
                // See https://stackoverflow.com/a/13278843
                exec.isIgnoreExitValue = true
                // Ignores/throws away the process output to avoid it being shown in Gradle output
                exec.standardOutput = OutputStream.nullOutputStream()
                exec.errorOutput = OutputStream.nullOutputStream()
                exec.commandLine(upxPath, "--best", file.absolutePath)
            }
            val progress = (index.toFloat() / vlcFiles.lastIndex * 100).roundToInt()
            progressLogger.progress("$progress%")
        }
        progressLogger.completed()
    }
}
