import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VlcCompressPluginsTask : DefaultTask() {

    @get:InputDirectory
    abstract val vlcDirectory: Property<File>

    @get:InputDirectory
    abstract val upxDirectory: Property<File>

    @TaskAction
    fun execute() {
        val upxPath = upxDirectory.get().resolve("upx").absolutePath
        vlcDirectory
            .get()
            .walk()
            // Exclude libvlc and libvlccore because compressing them seems to break app
            .filter { "libvlc" !in it.name }
            .filter { "so" in it.extension }
            .forEach { file ->
                project.exec {
                    // For when upx throws NotCompressibleException
                    // See https://stackoverflow.com/a/13278843
                    it.setIgnoreExitValue(true)
                    it.commandLine(upxPath, "--best", file.absolutePath)
                }
            }
    }
}
