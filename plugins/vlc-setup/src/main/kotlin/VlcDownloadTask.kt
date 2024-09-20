import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputFile
import java.io.File
import javax.inject.Inject

abstract class VlcDownloadTask @Inject constructor(
    @get:Input
    val vlcVersion: Provider<String>
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
    val vlcZipFile = tempDownloadDirectory.map { it.resolve("vlc-${vlcVersion.get()}.zip") }
    // Could also have used task-specific temp directory in project/build/temp:
    // temporaryDir.resolve("vlc-$vlcVersion.zip")

    init {
        val baseUrl = "https://get.videolan.org"
        // Make sure to download the 64-bit version of VLC
        src("$baseUrl/vlc/${vlcVersion.get()}/win64/vlc-${vlcVersion.get()}-win64.zip")
        dest(vlcZipFile)
        overwrite(false) // Prevents re-download every time
    }
}
