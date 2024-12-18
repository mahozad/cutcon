package lin

import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import java.io.File

abstract class VlcExtractTask : Copy() {

    @get:InputFile
    abstract val vlcArchiveFile: Property<File>

    @get:OutputDirectory
    val extractDirectory = vlcArchiveFile.map { it.resolveSibling("vlc") }

    init {
        from(project.zipTree(vlcArchiveFile))
        into(extractDirectory)
        includeEmptyDirs = false // Deletes empty remainder directories
        // All the below is to strip the parent directory from the target copy directory
        // (i.e. only copy the content of the directory and not the directory itself)
        // See https://docs.gradle.org/current/userguide/working_with_files.html#sec:unpacking_archives_example
        eachFile { it.relativePath = RelativePath(true, *it.relativePath.segments.drop(1).toTypedArray()) }
    }
}
