import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import java.io.File

abstract class VlcUnzipTask : Copy() {

    @get:InputFile
    abstract val zipFile: Property<File>

    @get:OutputDirectory
    abstract val unzipDirectory: Property<File>

    init {
        from(project.zipTree(zipFile)) {
            // All the below is to copy only the contents of the root directory
            // in the archive and not the root directory itself
            // See https://docs.gradle.org/current/userguide/working_with_files.html#sec:unpacking_archives_example
            eachFile { it.relativePath = RelativePath(true, *it.relativePath.segments.drop(1).toTypedArray()) }
            includeEmptyDirs = false // Deletes empty remainder directories
        }
        into(unzipDirectory)
    }
}
