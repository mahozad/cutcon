import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.Delete
import org.gradle.api.tasks.Sync
import org.gradle.api.tasks.testing.Test

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
         * See <PROJECT_ROOT>/README.md -> Embedding VLC DLL files section for more info
         * and also https://docs.gradle.org/current/userguide/working_with_files.html
         */
        project.tasks
            .withType(Sync::class.java)
            .matching { it.name == "prepareAppResources" }
            .all { it.dependsOn(vlcSetup) }

        project.tasks.withType(Test::class.java) { test ->
            // See <PROJECT_ROOT>/assets/README.md for more info.
            // and https://github.com/JetBrains/compose-multiplatform/issues/3244
            test.dependsOn("prepareAppResources")
            project.afterEvaluate {
                test.systemProperty(
                    "compose.application.resources.dir",
                    project.tasks.named("prepareAppResources", Sync::class.java).get().destinationDir
                )
            }
        }

        project.tasks.named("clean", Delete::class.java) {
            it.delete += setOf(
                upxUnzip.get().unzipDirectory, // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
                vlcUnzip.get().unzipDirectory, // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
                vlcSetup.get().windowsCopyPath // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
            )
        }
    }
}
