package ir.mahozad.vlcsetup.lin

import de.undercouch.gradle.tasks.download.Download
import ir.mahozad.vlcsetup.VlcSetupExtension
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
    private val downloadDirectory: Provider<File> =
        project.objects.property(File::class.java).value(
            project
                .gradle
                .gradleUserHomeDir
                .resolve(VlcSetupExtension.PLUGIN_NAME)
                .also(File::mkdirs)
        )

    @get:OutputFile
    val vlcArchiveFile = downloadDirectory.zip(vlcVersion) { directory, version ->
        directory.resolve("vlc-$version.jar")
    }

    init {
        val baseUrl = "https://repo1.maven.org/maven2/ir/mahozad/vlc-plugins-linux"
        src(vlcVersion.map { version -> "$baseUrl/$version/vlc-plugins-linux-$version.jar" })
        dest(vlcArchiveFile)
        overwrite(false) // Prevents re-download every time
        readTimeout(60_000) // 1 minute
    }
}
