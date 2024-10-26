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
            it.dependsOn(vlcInstallTools)
        }
        val vlcExtract = project.tasks.register("vlcExtractLinuxTask", VlcExtractLinuxTask::class.java) {
            it.dependsOn(vlcDownload)
            it.snapFile.set(vlcDownload.get().vlcSnapFile)
            it.extractDirectory.set(vlcDownload.get().vlcSnapFile.map { it.resolveSibling("vlc") })
        }
        val prepareLibraries = project.tasks.register("vlcPrepareLibraries", VlcPrepareLibrariesLinuxTask::class.java) {
            it.dependsOn(vlcExtract)
            it.vlcDirectory.set(vlcExtract.get().snapFile.map { it.parentFile })
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
