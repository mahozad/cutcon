import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.TaskProvider

// See https://docs.gradle.org/current/userguide/implementing_gradle_plugins_precompiled.html#sec:applying_external_plugins
// An easy and informative way to debug the tasks and why they are up to date or not is to add
// --info to the gradle command so that it shows the info logs (such as why a task was skipped)
@Suppress("unused")
abstract class VlcSetupPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        val vlcSetupExtension = project.extensions.create(
            VlcSetupExtension.PLUGIN_NAME,
            VlcSetupExtension::class.java
        )
        val currentOs = getCurrentOs()
        val vlcSetupTask: TaskProvider<*>
        if (currentOs == OS.WINDOWS) {
            val vlcDownload = project.tasks.register("vlcDownload", win.VlcDownloadTask::class.java) {
                it.vlcVersion.set(vlcSetupExtension.vlcVersion)
            }
            val upxDownload = project.tasks.register("upxDownload", win.UpxDownloadTask::class.java) {
                it.upxVersion.set(vlcSetupExtension.upxVersion)
            }
            val upxExtract = project.tasks.register("upxExtract", win.UpxExtractTask::class.java) {
                it.dependsOn(upxDownload)
                it.mustRunAfter(vlcDownload)
                it.upxArchiveFile.set(upxDownload.get().upxArchiveFile)
            }
            val vlcExtract = project.tasks.register("vlcExtract", win.VlcExtractTask::class.java) {
                it.dependsOn(vlcDownload)
                it.mustRunAfter(upxDownload)
                it.vlcArchiveFile.set(vlcDownload.get().vlcArchiveFile)
            }
            val vlcFilterPlugins = project.tasks.register("vlcFilterPlugins", win.VlcFilterPluginsTask::class.java) {
                it.dependsOn(vlcExtract)
                it.sourceDirectory.set(vlcExtract.get().extractDirectory)
                it.shouldIncludeAllPlugins.set(vlcSetupExtension.shouldIncludeAllVlcFiles)
            }
            val vlcCompressPlugins = project.tasks.register("vlcCompressPlugins", win.VlcCompressPluginsTask::class.java) {
                it.dependsOn(upxExtract)
                it.dependsOn(vlcFilterPlugins)
                it.vlcDirectory.set(vlcFilterPlugins.get().targetDirectory)
                it.upxDirectory.set(upxExtract.get().extractDirectory)
                it.shouldCompressPlugins.set(vlcSetupExtension.shouldCompressVlcFiles)
            }
            vlcSetupTask = project.tasks.register("vlcSetup", win.VlcSetupTask::class.java) {
                it.dependsOn(vlcCompressPlugins)
                it.sourceDirectory.set(vlcCompressPlugins.get().targetDirectory)
                it.targetDirectory.set(vlcSetupExtension.pathToCopyVlcWindowsFilesTo)
            }
            project
                .tasks
                .withType(Delete::class.java)
                .matching { it.name == "clean" }
                .all {
                    it.delete += setOf(
                        upxExtract.get().extractDirectory,
                        vlcExtract.get().extractDirectory,
                        vlcSetupTask.get().targetDirectory // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
                    )
                }
        } else if (currentOs == OS.LINUX) {
            val vlcDownload = project.tasks.register("vlcDownload", lin.VlcDownloadTask::class.java) {
                it.vlcVersion.set("3.0.20" /* FIXME: vlcSetupExtension.vlcVersion */)
            }
            val upxDownload = project.tasks.register("upxDownload", lin.UpxDownloadTask::class.java) {
                it.upxVersion.set(vlcSetupExtension.upxVersion)
            }
            val upxExtract = project.tasks.register("upxExtract", lin.UpxExtractTask::class.java) {
                it.dependsOn(upxDownload)
                it.mustRunAfter(vlcDownload)
                it.upxArchiveFile.set(upxDownload.get().upxArchiveFile)
            }
            val vlcExtract = project.tasks.register("vlcExtract", lin.VlcExtractTask::class.java) {
                it.dependsOn(vlcDownload)
                it.mustRunAfter(upxDownload)
                it.vlcArchiveFile.set(vlcDownload.get().vlcArchiveFile)
            }
            val vlcFilterPlugins = project.tasks.register("vlcFilterPlugins", lin.VlcFilterPluginsTask::class.java) {
                it.dependsOn(vlcExtract)
                it.sourceDirectory.set(vlcExtract.get().extractDirectory)
                it.shouldIncludeAllPlugins.set(vlcSetupExtension.shouldIncludeAllVlcFiles)
            }
            val vlcCompressPlugins = project.tasks.register("vlcCompressPlugins", lin.VlcCompressPluginsTask::class.java) {
                it.dependsOn(upxExtract)
                it.dependsOn(vlcFilterPlugins)
                it.vlcDirectory.set(vlcFilterPlugins.get().targetDirectory)
                it.upxDirectory.set(upxExtract.get().extractDirectory)
                it.shouldCompressPlugins.set(vlcSetupExtension.shouldCompressVlcFiles)
            }
            vlcSetupTask = project.tasks.register("vlcSetup", lin.VlcSetupTask::class.java) {
                it.dependsOn(vlcCompressPlugins)
                it.sourceDirectory.set(vlcCompressPlugins.get().targetDirectory)
                it.targetDirectory.set(vlcSetupExtension.pathToCopyVlcLinuxFilesTo)
            }
            project
                .tasks
                .withType(Delete::class.java)
                .matching { it.name == "clean" }
                .all {
                    it.delete += setOf(
                        upxExtract.get().extractDirectory,
                        vlcExtract.get().extractDirectory,
                        vlcSetupTask.get().targetDirectory // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
                    )
                }
        } else if (currentOs == OS.MAC) {
            val vlcDownload = project.tasks.register("vlcDownload", mac.VlcDownloadTask::class.java) {
                it.vlcVersion.set(vlcSetupExtension.vlcVersion)
            }
            val upxExtract = project.tasks.register("upxExtract", mac.UpxExtractTask::class.java)
            val vlcExtract = project.tasks.register("vlcExtract", mac.VlcExtractTask::class.java) {
                it.dependsOn(vlcDownload)
                it.vlcArchiveFile.set(vlcDownload.get().vlcArchiveFile)
            }
            val vlcFilterPlugins = project.tasks.register("vlcFilterPlugins", mac.VlcFilterPluginsTask::class.java) {
                it.dependsOn(vlcExtract)
                it.sourceDirectory.set(vlcExtract.get().extractDirectory)
                it.shouldIncludeAllPlugins.set(vlcSetupExtension.shouldIncludeAllVlcFiles)
            }
            val vlcCompressPlugins =
                project.tasks.register("vlcCompressPlugins", mac.VlcCompressPluginsTask::class.java) {
                    it.dependsOn(upxExtract)
                    it.dependsOn(vlcFilterPlugins)
                    it.vlcDirectory.set(vlcFilterPlugins.get().targetDirectory)
                    it.upxDirectory.set(upxExtract.get().extractDirectory)
                    it.shouldCompressPlugins.set(vlcSetupExtension.shouldCompressVlcFiles)
                }
            vlcSetupTask = project.tasks.register("vlcSetup", mac.VlcSetupTask::class.java) {
                it.dependsOn(vlcCompressPlugins)
                it.sourceDirectory.set(vlcCompressPlugins.get().targetDirectory)
                it.targetDirectory.set(vlcSetupExtension.pathToCopyVlcMacosFilesTo)
            }
            project
                .tasks
                .withType(Delete::class.java)
                .matching { it.name == "clean" }
                .all {
                    it.delete += setOf(
                        upxExtract.get().extractDirectory,
                        vlcExtract.get().extractDirectory,
                        vlcSetupTask.get().targetDirectory // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
                    )
                }
        } else {
            error("""The OS "$currentOs" is not supported""")
        }

        /**
         * See https://docs.gradle.org/current/userguide/working_with_files.html
         */
        project
            .tasks
            .matching { it.name == "processResources" }
            .all { it.dependsOn(vlcSetupTask) }
        // If the windowsCopyPath is inside the path defined as Compose Multiplatform resources directory then
        // it makes the task prepareAppResources implicitly depend on vlcSetup task because its input directory
        // contains output files/directories (windowsCopyPath) of vlcSetup task.
        // So, here, the prepareAppResources task (if/when it exists) is configured to run after vlcSetup task.
        project
            .tasks
            .matching { it.name == "prepareAppResources" }
            .all { it.mustRunAfter(vlcSetupTask) }
    }
}
