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
        val vlcDownload = project.tasks.register(
            "vlcDownload",
            VlcDownloadTask::class.java,
            vlcSetupExtension.versionToUse
        )
        val upxDownload = project.tasks.register(
            "upxDownload",
            UpxDownloadTask::class.java,
            project.objects.property(String::class.java).value("4.2.4")
        )
        val vlcUnzip = project.tasks.register(
            "vlcUnzip",
            VlcUnzipTask::class.java,
            vlcDownload.get().vlcZipFile,
            vlcDownload.get().tempDownloadDirectory.map { it.resolve("vlc") }
        ).also { task -> task.configure { it.dependsOn(vlcDownload) } }
        val upxUnzip = project.tasks.register(
            "upxUnzip",
            UpxUnzipTask::class.java,
            upxDownload.get().upxZipFile,
            upxDownload.get().tempDownloadDirectory.map { it.resolve("upx") }
        ).also { task -> task.configure { it.dependsOn(upxDownload) } }
        val vlcSetup = project.tasks.register(
            "vlcSetup",
            VlcSetupTask::class.java,
            vlcUnzip.get().unzipDirectory,
            upxUnzip.get().unzipDirectory,
            vlcSetupExtension.shouldCompressPlugins,
            vlcSetupExtension.shouldIncludeAllPlugins,
            vlcSetupExtension.windowsCopyPath
        )
        vlcSetup.configure {
            it.dependsOn(upxUnzip)
            it.dependsOn(vlcUnzip)
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
                upxUnzip.get().unzipDirectory,   // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
                vlcUnzip.get().unzipDirectory,   // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
                vlcSetup.get().windowsCopyPath // TODO: DANGEROUS!!! (if accidentally in code set to a directory with usable files)
            )
        }
    }
}
