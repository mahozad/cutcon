import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import java.io.File

abstract class VlcDownloadLinuxTask : Download() {

    // Could also have used task-specific `temporaryDir` property (project/build/temp)
    @get:OutputDirectory
    val tempDownloadDirectory: Provider<File> = project.objects.property(File::class.java).value(
        project
            .gradle
            .gradleUserHomeDir
            // .resolve(VlcSetupExtension.PLUGIN_NAME)
            .resolve("vlc-setup-linux")
            .also(File::mkdirs)
    )

    @get:OutputFile
    val vlcSnapFile = tempDownloadDirectory.map {
        it.resolve("vlc.snap")
    }

    init {
        // Got from https://search.apps.ubuntu.com/api/v1/package/vlc
        // See the readme/howto file for more information
        val baseUrl = "https://api.snapcraft.io/api/v1/snaps/download"
        src("$baseUrl/RT9mcUhVsRYrDLG8qnvGiy26NKvv6Qkd_3777.snap")
        dest(vlcSnapFile)
        overwrite(false) // Prevents re-download every time
    }
}
