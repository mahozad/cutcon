package ir.mahozad.vlcsetup.mac

import ir.mahozad.vlcsetup.VlcSetupExtension
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class UpxExtractTask : DefaultTask() {

    @get:OutputDirectory
    val extractDirectory = project.objects.property(File::class.java).value(
        project
            .gradle
            .gradleUserHomeDir
            .resolve(VlcSetupExtension.PLUGIN_NAME)
            .resolve("upx")
            .also(File::mkdirs)
    )

    @TaskAction
    fun execute() {
        // TODO: UPX does not yet provide binary for macOS.
        //  Use and download the release on GitHub like what
        //  has been done for Linux/Windows if/when it becomes available.
        //  See https://github.com/upx/upx/issues/555
        //  and https://github.com/upx/upx/issues/115
        // The following UPX file was acquired by installing upx with
        // `brew install upx` on a macOS and then finding the installed
        // UPX program with `which upx` and then copying it
        javaClass
            .getResource("/mac/upx-x64-4.2.4")!!
            .openStream()
            .use { inputStream ->
                val vlcFile = extractDirectory.get().resolve("upx")
                vlcFile.outputStream().use { outputStream -> inputStream.copyTo(outputStream) }
                vlcFile.setExecutable(true) // Required
            }
    }
}
