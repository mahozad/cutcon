import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class UpxPrepareTask : DefaultTask() {

    // Could also have used task-specific `temporaryDir` property (project/build/temp)
    private val tempDownloadDirectory: Provider<File> = project.objects.property(File::class.java).value(
        project
            .gradle
            .gradleUserHomeDir
            .resolve(VlcSetupExtension.PLUGIN_NAME)
            .also(File::mkdirs)
    )

    private val extractDirectory = "upx"

    @get:Input
    abstract val upxVersion: Property<String>

    @get:OutputDirectory
    val upxDirectory = tempDownloadDirectory.map {
        it.resolve(extractDirectory)
    }

    @TaskAction
    fun execute() {
        val version = upxVersion.get()
        val baseUrl = "https://github.com/upx/upx/releases/download"
        val downloadUrl = "$baseUrl/v$version/upx-$version-amd64_linux.tar.xz"
        val saveFileName = "upx-$version.tar.xz"
        project.exec {
            it
                .commandLine("wget", "-N", "-O", saveFileName, downloadUrl)
                .workingDir(tempDownloadDirectory)
        }
        project.exec {
            it
                .commandLine("tar", "xf", saveFileName, "--directory", extractDirectory, "--strip-components=1")
                .workingDir(tempDownloadDirectory)
        }
    }
}
