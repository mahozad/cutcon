import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.io.path.createDirectories

abstract class VlcPrepareLibrariesLinuxTask : DefaultTask() {

    @get:InputDirectory
    abstract val vlcDirectory: Property<File>

    private val script: File by lazy {
        val destination = temporaryDir.resolve("script.sh")
        javaClass.getResourceAsStream("/script.sh")?.use { input ->
            destination.outputStream().use(input::copyTo)
        }
        destination
    }

    @TaskAction
    fun execute() {
        project.exec {
            it
                .commandLine("sh", "$script")
                .setStandardInput(System.`in`)
                .workingDir(vlcDirectory.get())
        }
    }
}
