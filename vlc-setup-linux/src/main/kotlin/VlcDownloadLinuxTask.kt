import de.undercouch.gradle.tasks.download.Download
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.OutputFile
import java.io.File

abstract class VlcDownloadLinuxTask : Download() {

    @get:Input
    abstract val vlcVersion: Property<String>

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
    val vlcTarFile = tempDownloadDirectory.zip(vlcVersion) { directory, version ->
        directory.resolve("vlc-$version.tar.xz")
    }

    init {
        val baseUrl = "https://get.videolan.org"
        src(vlcVersion.map { "$baseUrl/vlc/${it}/vlc-${it}.tar.xz" })
        dest(vlcTarFile)
        overwrite(false) // Prevents re-download every time
    }
}
