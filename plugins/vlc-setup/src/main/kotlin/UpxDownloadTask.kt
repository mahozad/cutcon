import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import java.io.File
import javax.inject.Inject

abstract class UpxDownloadTask @Inject constructor(
    @get:Input
    val upxVersion: Provider<String>
) : Download() {

    @get:Input
    var tempDownloadDirectory: Provider<File> = project.objects.property(File::class.java).value(
        project
            .gradle
            .gradleUserHomeDir
            .resolve(VlcSetupExtension.PLUGIN_NAME)
            .also(File::mkdirs)
    )

    @get:OutputFile
    val upxZipFile = tempDownloadDirectory.map { it.resolve("upx-${upxVersion.get()}.zip") }
    // Could also have used task-specific temp directory in project/build/temp:
    // temporaryDir.resolve("vlc-$vlcVersion.zip")

    init {
        src(upxVersion.map { "https://github.com/upx/upx/releases/download/v$it/upx-$it-win64.zip" })
        dest(upxZipFile)
        overwrite(false) // Prevents re-download every time
    }
}
