import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VlcUnzipLinuxTask : DefaultTask() {

    @get:InputFile
    abstract val zipFile: Property<File>

    @get:OutputDirectory
    abstract val unzipDirectory: Property<File>

    @TaskAction
    fun execute() {
        project.exec {
            it.commandLine(
                "tar",
                "xJf",
                zipFile.get(),
                "--directory", unzipDirectory.get(),
                "--strip-components=1" // Omits the parent directory in archive from the extracted files
            )
        }
    }
}

////////////////////////////////////////////////////
////////////////////////////////////////////////////
////////////////////////////////////////////////////

// See https://github.com/gradle/gradle/issues/15065
// Requires org.tukaani:xz:1.10 dependency

//fun File.seekable() = SeekableFileInputStream(this)
//
//fun SeekableFileInputStream.lzma2() = SeekableXZInputStream(this)
//
//class XZArchiver(private val resource: ReadableResourceInternal) : ReadableResource {
//    private val uri = URIBuilder(resource.uri).schemePrefix("xz:").build()
//    override fun read() = resource.backingFile.seekable().lzma2()
//    override fun getURI() = uri
//    override fun toString() = displayName
//    override fun getBaseName() = displayName
//    override fun getDisplayName() = resource.displayName
//}
//
//fun ResourceHandler.xz(path: Property<File>): XZArchiver {
//    val resourceResolverField = DefaultResourceHandler::class.java.getDeclaredField("resourceResolver").apply {
//        isAccessible = true
//    }
//    val resourceResolver = resourceResolverField.get(this) as ResourceResolver
//    val resource = resourceResolver.resolveResource(path)
//    resourceResolverField.isAccessible = false
//    return XZArchiver(resource)
//}
