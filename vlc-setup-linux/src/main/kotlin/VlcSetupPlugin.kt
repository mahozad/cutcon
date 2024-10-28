import org.gradle.api.Plugin
import org.gradle.api.Project

@Suppress("unused")
abstract class VlcSetupPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        // val vlcSetupExtension = project.extensions.create(
        //     VlcSetupExtension.PLUGIN_NAME,
        //     VlcSetupExtension::class.java
        // )
        val vlcDownload = project.tasks.register("vlcDownloadLinux", VlcDownloadLinuxTask::class.java)
        val upxPrepare = project.tasks.register("upxPrepareLinuxTask", UpxDownloadLinuxTask::class.java) {
            it.upxVersion.set("4.2.4")
        }
        val vlcExtract = project.tasks.register("vlcExtractLinuxTask", VlcExtractLinuxTask::class.java) {
            it.dependsOn(vlcDownload)
            it.dependsOn(upxPrepare.get())
            it.snapFile.set(vlcDownload.get().vlcSnapFile)
            it.extractDirectory.set(vlcDownload.get().vlcSnapFile.map { it.resolveSibling("vlc") })
        }
        val vlcPreparePlugins = project.tasks.register("vlcPreparePluginsLinuxTask", VlcPreparePluginsLinuxTask::class.java) {
            it.dependsOn(vlcExtract)
            it.sourceDirectory.set(vlcExtract.get().extractDirectory)
            it.targetDirectory.set(vlcExtract.get().extractDirectory.map { it.resolveSibling("vlc-modified") })
        }
        val vlcCompressPlugins = project.tasks.register("vlcCompressPlugins", VlcCompressPluginsLinuxTask::class.java) {
            it.dependsOn(vlcPreparePlugins)
            it.vlcDirectory.set(vlcPreparePlugins.get().targetDirectory)
            it.upxDirectory.set(upxPrepare.get().upxDirectory)
        }

        /**
         * See <PROJECT_ROOT>/README.md -> Embedding VLC DLL files section for more info
         * and also https://docs.gradle.org/current/userguide/working_with_files.html
         */
        project
            .tasks
            // .withType(Sync::class.java)
            .matching { it.name == "run" }
            .all { it.dependsOn(vlcCompressPlugins) }
    }
}
