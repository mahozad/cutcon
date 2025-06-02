package ir.mahozad.vlcsetup.mac

import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.*
import java.io.File

abstract class VlcCompressPluginsTask : DefaultTask() {

    @get:Input
    abstract val shouldCompressPlugins: Property<Boolean>

    @get:InputDirectory
    abstract val vlcDirectory: Property<File>

    @get:InputDirectory
    abstract val upxDirectory: Property<File>

    @get:OutputDirectory
    val targetDirectory = vlcDirectory.map { it.resolveSibling("vlc-compressed") }

    @TaskAction
    fun execute() {
        targetDirectory.get().deleteRecursively() // For when the filtered plugins become more restrictive
        targetDirectory.get().let(vlcDirectory.get()::copyRecursively)
        // FIXME: Implement this when UPX adds support for compressing macOS .dylib files
        throw StopExecutionException("UPX does not (yet) seem to support compressing .dylib files")
    }
}
