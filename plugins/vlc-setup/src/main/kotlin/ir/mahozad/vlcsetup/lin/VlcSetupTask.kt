package ir.mahozad.vlcsetup.lin

import ir.mahozad.vlcsetup.VlcSetupExtension
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VlcSetupTask : DefaultTask() {

    init {
        group = "compose desktop"
    }

    @get:InputDirectory
    abstract val sourceDirectory: Property<File>

    @get:OutputDirectory
    abstract val targetDirectory: Property<File>

    @TaskAction
    fun execute() {
        check(targetDirectory.isPresent) {
            """
                ${VlcSetupExtension::pathToCopyVlcLinuxFilesTo.name} is not specified.
                Set it in ${VlcSetupExtension.PLUGIN_NAME}{} block.
            """
        }
        // Do NOT use targetDirectory.get().deleteRecursively() as it is so dangerous
        // because the user may have other files in this directory besides VLC.
        // This is still not the best way as user may have their custom .so files in this directory
        targetDirectory
            .get()
            .walk()
            .filter { it.extension == "so" }
            .forEach { it.delete() }
        project.copy { copy ->
            copy.from(sourceDirectory)
            copy.into(targetDirectory)
        }
    }
}
