import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete

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
        val vlcDownload = project.tasks.register("vlcDownload", VlcDownloadTask::class.java) {
            it.vlcVersion.set(vlcSetupExtension.vlcVersion)
        }
        val upxDownload = project.tasks.register("upxDownload", UpxDownloadTask::class.java) {
            it.upxVersion.set(vlcSetupExtension.upxVersion)
        }
        val vlcUnzip = project.tasks.register("vlcUnzip", VlcUnzipTask::class.java) {
            it.dependsOn(vlcDownload)
            it.mustRunAfter(upxDownload)
            it.zipFile.set(vlcDownload.get().vlcZipFile)
            it.unzipDirectory.set(vlcDownload.get().tempDownloadDirectory.map { it.resolve("vlc") })
        }
        val upxUnzip = project.tasks.register("upxUnzip", UpxUnzipTask::class.java) {
            it.dependsOn(upxDownload)
            it.mustRunAfter(vlcDownload)
            it.zipFile.set(upxDownload.get().upxZipFile)
            it.unzipDirectory.set(upxDownload.get().tempDownloadDirectory.map { it.resolve("upx") })
        }
        val vlcSetup = project.tasks.register("vlcSetup", VlcSetupTask::class.java) {
            it.vlcDirectory.set(vlcUnzip.get().unzipDirectory)
            it.upxDirectory.set(upxUnzip.get().unzipDirectory)
            it.windowsCopyPath.set(vlcSetupExtension.windowsCopyPath)
            it.shouldCompressPlugins.set(vlcSetupExtension.shouldCompressPlugins)
            it.shouldIncludeAllPlugins.set(vlcSetupExtension.shouldIncludeAllPlugins)
        }

        /**
         * See https://docs.gradle.org/current/userguide/working_with_files.html
         */
        project
            .tasks
            .matching { it.name == "processResources" }
            .all { it.dependsOn(vlcSetup) }

        // If the windowsCopyPath is inside the path defined as Compose Multiplatform resources directory then
        // it makes the task prepareAppResources implicitly depend on vlcSetup task because its input directory
        // contains output files/directories (windowsCopyPath) of vlcSetup task.
        // So, here, the prepareAppResources task (if/when it exists) is configured to run after vlcSetup task.
        project
            .tasks
            .matching { it.name == "prepareAppResources" }
            .all { it.mustRunAfter(vlcSetup) }

        project
            .tasks
            .withType(Delete::class.java)
            .matching { it.name == "clean" }
            .all {
                it.delete += setOf(
                    upxUnzip.get().unzipDirectory, // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
                    vlcUnzip.get().unzipDirectory, // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
                    vlcSetup.get().windowsCopyPath // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
                )
            }
    }
}
