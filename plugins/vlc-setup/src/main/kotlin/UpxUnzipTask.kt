import org.gradle.api.file.RelativePath
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Copy
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import java.io.File

abstract class UpxUnzipTask : Copy() {

    @get:InputFile
    abstract val zipFile: Property<File>

    @get:OutputDirectory
    abstract val unzipDirectory: Property<File>

    init {
        from(project.zipTree(zipFile))
        into(unzipDirectory)
        include("**/upx.exe")
        includeEmptyDirs = false // Deletes empty remainder directories
        // All the below is to strip the parent directory from the target copy directory
        // (i.e. only copy the content of the directory and not the directory itself)
        // See https://docs.gradle.org/current/userguide/working_with_files.html#sec:unpacking_archives_example
        eachFile { it.relativePath = RelativePath(true, *it.relativePath.segments.drop(1).toTypedArray()) }
    }
}
