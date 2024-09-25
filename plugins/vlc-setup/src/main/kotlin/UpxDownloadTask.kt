import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import java.io.File

abstract class UpxDownloadTask : Download() {

    @get:Input
    abstract val upxVersion: Property<String>

    // Could also have used task-specific `temporaryDir` property (project/build/temp)
    @get:OutputDirectory
    val tempDownloadDirectory: Provider<File> = project.objects.property(File::class.java).value(
        project
            .gradle
            .gradleUserHomeDir
            .resolve(VlcSetupExtension.PLUGIN_NAME)
            .also(File::mkdirs)
    )

    @get:OutputFile
    val upxZipFile = tempDownloadDirectory.zip(upxVersion) { directory, version ->
        directory.resolve("upx-$version.zip")
    }

    init {
        val baseUrl = "https://github.com/upx/upx/releases/download"
        src(upxVersion.map { "$baseUrl/v$it/upx-$it-win64.zip" })
        dest(upxZipFile)
        overwrite(false) // Prevents re-download every time
    }
}
