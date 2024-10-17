import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File
import kotlin.io.path.createDirectories

abstract class VlcPrepareLibrariesLinuxTask : DefaultTask() {

    @get:InputDirectory
    abstract val vlcDirectory: Property<File>

    @TaskAction
    fun execute() {
        val nativeDirectory = vlcDirectory
            .get()
            .resolve("contrib")
            .resolve("native")
            .also { it.toPath().createDirectories() }
        project.exec {
            it
                .commandLine("../bootstrap")
                .workingDir(nativeDirectory)
        }
        project.exec {
            it
                .commandLine("make")
                .workingDir(nativeDirectory)
        }
    }
}
