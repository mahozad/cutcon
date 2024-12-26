package mac

import VlcSetupExtension
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
        directory.resolve("vlc-$version.dmg")
    }

    init {
        val baseUrl = "https://get.videolan.org"
        // NOTE: The universal version of VLC for macOS includes .dylib files that
        // should work on both intel64 and arm64 architectures.
        // Could instead just use the -intel64.dmg to reduce the total size by about 50%.
        src(vlcVersion.map { version -> "$baseUrl/vlc/$version/macosx/vlc-$version-universal.dmg" })
        dest(vlcArchiveFile)
        overwrite(false) // Prevents re-download every time
        readTimeout(60_000) // 1 minute
    }
}
