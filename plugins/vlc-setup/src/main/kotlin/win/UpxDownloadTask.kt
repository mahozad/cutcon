package win

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
    private val downloadDirectory: Provider<File> =
        project.objects.property(File::class.java).value(
            project
                .gradle
                .gradleUserHomeDir
                .resolve(VlcSetupExtension.PLUGIN_NAME)
                .also(File::mkdirs)
        )

    @get:OutputFile
    val upxArchiveFile = downloadDirectory.zip(upxVersion) { directory, version ->
        directory.resolve("upx-$version.zip")
    }

    init {
        val baseUrl = "https://github.com/upx/upx/releases/download"
        src(upxVersion.map { "$baseUrl/v$it/upx-$it-win64.zip" })
        dest(upxArchiveFile)
        overwrite(false) // Prevents re-download every time
    }
}
