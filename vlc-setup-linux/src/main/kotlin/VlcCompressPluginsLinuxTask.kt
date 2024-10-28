import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VlcCompressPluginsLinuxTask : DefaultTask() {

    @get:InputDirectory
    abstract val vlcDirectory: Property<File>

    @get:InputDirectory
    abstract val upxDirectory: Property<File>

    @TaskAction
    fun execute() {
        // Exclude libvlc and libvlccore because compressing them seems to break something
        val upxPath = upxDirectory.get().resolve("upx").absolutePath
        vlcDirectory
            .get()
            .resolve("usr/lib/vlc/")
            .walk()
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
