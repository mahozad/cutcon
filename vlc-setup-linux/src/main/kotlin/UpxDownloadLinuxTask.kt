import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class UpxDownloadLinuxTask : DefaultTask() {

    @get:Input
    abstract val upxVersion: Property<String>

    // Could also have used task-specific `temporaryDir` property (project/build/temp)
    private val tempDownloadDirectory: Provider<File> = project.objects.property(File::class.java).value(
        project
            .gradle
            .gradleUserHomeDir
            // .resolve(VlcSetupExtension.PLUGIN_NAME)
            .resolve("vlc-setup-linux")
            .also(File::mkdirs)
    )

    private val extractDirectory = "upx"

    @get:OutputDirectory
    val upxDirectory = tempDownloadDirectory.map {
        it.resolve(extractDirectory)
    }

    // @get:OutputFile
    // val upxTarFile = tempDownloadDirectory.zip(upxVersion) { directory, version ->
    //     directory.resolve("upx-$version.tar.xz")
    // }

    init {
        // src(upxVersion.map { "$baseUrl/v$it/upx-$it-amd64_linux.tar.xz" })
        // dest(upxTarFile)
        // overwrite(false) // Prevents re-download every time
    }

    @TaskAction
    fun execute() {
        val version = upxVersion.get()
        val baseUrl = "https://github.com/upx/upx/releases/download"
        val archive = "upx-$version-amd64_linux.tar.xz"
        project.exec {
            it
                .commandLine("wget", "-N", "$baseUrl/v$version/$archive")
                .workingDir(tempDownloadDirectory)
        }
        project.exec {
            it
                .commandLine("tar", "xf", archive, "--directory", extractDirectory, "--strip-components=1")
                .workingDir(tempDownloadDirectory)
        }
    }
}
