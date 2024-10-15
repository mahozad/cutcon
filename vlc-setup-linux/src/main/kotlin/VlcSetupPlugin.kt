import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
abstract class VlcSetupPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // val vlcSetupExtension = project.extensions.create(
        //     VlcSetupExtension.PLUGIN_NAME,
        //     VlcSetupExtension::class.java
        // )
        val vlcInstallTools = project.tasks.register("vlcInstallTools", VlcInstallToolsLinuxTask::class.java)
        val vlcDownload = project.tasks.register("vlcDownloadLinux", VlcDownloadLinuxTask::class.java) {
            // it.vlcVersion.set(vlcSetupExtension.vlcVersion)
            it.dependsOn(vlcInstallTools)
            it.vlcVersion.set("3.0.21")
        }
        val vlcUntar = project.tasks.register("vlcUntarLinux", VlcUntarLinuxTask::class.java) {
            it.dependsOn(vlcDownload)
            // it.mustRunAfter(upxDownload)
            it.tarFile.set(vlcDownload.get().vlcTarFile)
            it.untarDirectory.set(vlcDownload.get().tempDownloadDirectory.map { it.resolve("vlc") })
        }
        val prepareLibraries = project.tasks.register("vlcPrepareLibraries", VlcPrepareLibrariesLinuxTask::class.java) {
            it.dependsOn(vlcUntar)
            it.vlcDirectory.set(vlcUntar.get().untarDirectory)
        }

        /**
         * See <PROJECT_ROOT>/README.md -> Embedding VLC DLL files section for more info
         * and also https://docs.gradle.org/current/userguide/working_with_files.html
         */
        project
            .tasks
            // .withType(Sync::class.java)
            .matching { it.name == "assemble" }
            .all { it.dependsOn(prepareLibraries) }
    }
}
