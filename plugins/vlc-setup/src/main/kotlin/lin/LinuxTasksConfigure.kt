package lin

import OS
import TasksConfigure
import VlcSetupExtension
import getCurrentOs
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider

class LinuxTasksConfigure(
    private val project: Project,
    private val vlcSetupExtension: VlcSetupExtension
) : TasksConfigure() {

    override fun isApplicable() = getCurrentOs() == OS.LINUX

    override fun apply(): TaskProvider<*> {
        val vlcDownload = project.tasks.register("vlcDownload", VlcDownloadTask::class.java) {
            it.vlcVersion.set("3.0.20" /* FIXME: vlcSetupExtension.vlcVersion */)
        }
        val upxDownload = project.tasks.register("upxDownload", UpxDownloadTask::class.java) {
            it.upxVersion.set(vlcSetupExtension.upxVersion)
        }
        val upxExtract = project.tasks.register("upxExtract", UpxExtractTask::class.java) {
            it.dependsOn(upxDownload)
            it.upxArchiveFile.set(upxDownload.get().upxArchiveFile)
        }
        val vlcExtract = project.tasks.register("vlcExtract", VlcExtractTask::class.java) {
            it.dependsOn(vlcDownload)
            it.vlcArchiveFile.set(vlcDownload.get().vlcArchiveFile)
        }
        val vlcFilterPlugins = project.tasks.register("vlcFilterPlugins", VlcFilterPluginsTask::class.java) {
            it.dependsOn(vlcExtract)
            it.sourceDirectory.set(vlcExtract.get().extractDirectory)
            it.shouldIncludeAllPlugins.set(vlcSetupExtension.shouldIncludeAllVlcFiles)
        }
        val vlcCompressPlugins = project.tasks.register("vlcCompressPlugins", VlcCompressPluginsTask::class.java) {
            it.dependsOn(upxExtract)
            it.dependsOn(vlcFilterPlugins)
            it.vlcDirectory.set(vlcFilterPlugins.get().targetDirectory)
            it.upxDirectory.set(upxExtract.get().extractDirectory)
            it.shouldCompressPlugins.set(vlcSetupExtension.shouldCompressVlcFiles)
        }
        val vlcSetupTask = project.tasks.register("vlcSetup", VlcSetupTask::class.java) {
            it.dependsOn(vlcCompressPlugins)
            it.sourceDirectory.set(vlcCompressPlugins.get().targetDirectory)
            it.targetDirectory.set(vlcSetupExtension.pathToCopyVlcLinuxFilesTo)
        }
        project
            .tasks
            .withType(Delete::class.java)
            .matching { it.name == "clean" }
            .all {
                it.doLast {
                    // Do NOT use targetDirectory.get().deleteRecursively() as it is so dangerous
                    // because the user may have other files in this directory besides VLC.
                    // This is still not the best way as user may have their custom .so files in this directory
                    vlcSetupTask.get()
                        .targetDirectory.get()
                        .walk()
                        .filter { it.extension == "so" }
                        .forEach { it.delete() }
                }
                it.delete += setOf(
                    upxExtract.get().extractDirectory,
                    vlcExtract.get().extractDirectory,
                    vlcFilterPlugins.get().targetDirectory,
                    vlcCompressPlugins.get().targetDirectory
                )
            }
        return vlcSetupTask
    }
}
