import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VlcModifyLinuxTask : DefaultTask() {

    private val script: File by lazy {
        val destination = temporaryDir.resolve("script.sh")
        javaClass.getResourceAsStream("/script.sh")?.use { input ->
            destination.outputStream().use(input::copyTo)
        }
        destination
    }

    @get:InputDirectory
    abstract val sourceDirectory: Property<File>

    @get:OutputDirectory
    abstract val targetDirectory: Property<File>

    @TaskAction
    fun execute() {
        // TODO: This is so dangerous
        targetDirectory.get().deleteRecursively()
        project.copy { copy ->
            copy.include("usr/lib/**")
            copy.exclude(
                "usr/lib/ssl/**",
                "usr/lib/jvm/**",
                "usr/lib/debug/**",
                "usr/lib/x86_64-linux-gnu/**"
            )
            copy.from(sourceDirectory)
            copy.into(targetDirectory)
        }
        println("*******************************************")
        // sourceDirectory.get().copyRecursively(
        //     target = targetDirectory.get(),
        //     overwrite = true,
        //     onError = { file, exception ->
        //         if ("usr/bin/X11" in file.absolutePath) {
        //             OnErrorAction.SKIP
        //         } else {
        //             throw exception
        //         }
        //     }
        // )
        // project.exec {
        //     it
        //         .commandLine("sh", "$script")
        //         .setStandardInput(System.`in`)
        //         .workingDir(targetDirectory)
        // }
    }
}
