import org.gradle.api.Plugin
import org.gradle.api.Project

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
        val upxExtract = project.tasks.register("upxExtract", UpxExtractTask::class.java) {
            it.dependsOn(upxDownload)
            it.mustRunAfter(vlcDownload)
            it.upxArchiveFile.set(upxDownload.get().upxArchiveFile)
            it.extractDirectory.set(upxDownload.get().tempDownloadDirectory.map { it.resolve("upx") })
        }
        val vlcExtract = project.tasks.register("vlcExtract", VlcExtractTask::class.java) {
            it.dependsOn(vlcDownload)
            it.mustRunAfter(upxDownload)
            it.vlcArchiveFile.set(vlcDownload.get().vlcArchiveFile)
            it.extractDirectory.set(vlcDownload.get().vlcArchiveFile.map { it.resolveSibling("vlc") })
        }
        val vlcPreparePlugins = project.tasks.register("vlcPreparePlugins", VlcPreparePluginsTask::class.java) {
            it.dependsOn(vlcExtract)
            it.sourceDirectory.set(vlcExtract.get().extractDirectory)
            it.targetDirectory.set(vlcSetupExtension.linuxCopyPath)
        }
        val vlcCompressPlugins = project.tasks.register("vlcCompressPlugins", VlcCompressPluginsTask::class.java) {
            it.dependsOn(vlcPreparePlugins)
            it.vlcDirectory.set(vlcPreparePlugins.get().targetDirectory)
            it.upxDirectory.set(upxExtract.get().extractDirectory)
        }

        project
            .tasks
            .matching { it.name == "prepareAppResources" }
            .all { it.dependsOn(vlcCompressPlugins) }
    }
}
