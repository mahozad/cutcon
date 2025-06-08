package ir.mahozad.vlcsetup.mac

import ir.mahozad.vlcsetup.OS
import ir.mahozad.vlcsetup.TasksConfigure
import ir.mahozad.vlcsetup.VlcSetupExtension
import ir.mahozad.vlcsetup.getCurrentOs
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider

class MacTasksConfigure(
    private val project: Project,
    private val vlcSetupExtension: VlcSetupExtension
) : TasksConfigure() {

    override fun isApplicable() = getCurrentOs() == OS.MAC

    override fun apply(): TaskProvider<*> {
        val vlcDownload = project.tasks.register("vlcDownload", VlcDownloadTask::class.java) {
            it.vlcVersion.set(vlcSetupExtension.vlcVersion)
        }
        val upxExtract = project.tasks.register("upxExtract", UpxExtractTask::class.java)
        val vlcExtract = project.tasks.register("vlcExtract", VlcExtractTask::class.java) {
            it.dependsOn(vlcDownload)
            it.vlcArchiveFile.set(vlcDownload.get().vlcArchiveFile)
        }
        val vlcFilterPlugins = project.tasks.register("vlcFilterPlugins", VlcFilterPluginsTask::class.java) {
            it.dependsOn(vlcExtract)
            it.sourceDirectory.set(vlcExtract.get().extractDirectory)
            it.shouldIncludeAllPlugins.set(vlcSetupExtension.shouldIncludeAllVlcFiles)
        }
        val vlcCompressPlugins =
            project.tasks.register("vlcCompressPlugins", VlcCompressPluginsTask::class.java) {
                it.dependsOn(upxExtract)
                it.dependsOn(vlcFilterPlugins)
                it.vlcDirectory.set(vlcFilterPlugins.get().targetDirectory)
                it.upxDirectory.set(upxExtract.get().extractDirectory)
                it.shouldCompressPlugins.set(vlcSetupExtension.shouldCompressVlcFiles)
            }
        val vlcSetupTask = project.tasks.register("vlcSetup", VlcSetupTask::class.java) {
            it.dependsOn(vlcCompressPlugins)
            it.sourceDirectory.set(vlcCompressPlugins.get().targetDirectory)
            it.targetDirectory.set(vlcSetupExtension.pathToCopyVlcMacosFilesTo)
        }
        project
            .tasks
            .withType(Delete::class.java)
            .matching { it.name == "clean" }
            .all {
                it.doLast {
                    // Do NOT use targetDirectory.get().deleteRecursively() as it is so dangerous
                    // because the user may have other files in this directory besides VLC.
                    // This is still not the best way as user may have their custom .dylib files in this directory
                    vlcSetupTask.get()
                        .targetDirectory.get()
                        .walk()
                        .filter { it.extension == "dylib" }
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
