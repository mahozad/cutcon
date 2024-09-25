import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import java.io.File

abstract class VlcDownloadTask : Download() {

    @get:Input
    abstract val vlcVersion: Property<String>

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
    val vlcZipFile = tempDownloadDirectory.zip(vlcVersion) { directory, version ->
        directory.resolve("vlc-$version.zip")
    }

    init {
        val baseUrl = "https://get.videolan.org"
        // Make sure to download the 64-bit version of VLC
        src(vlcVersion.map { "$baseUrl/vlc/${it}/win64/vlc-${it}-win64.zip" })
        dest(vlcZipFile)
        overwrite(false) // Prevents re-download every time
    }
}
