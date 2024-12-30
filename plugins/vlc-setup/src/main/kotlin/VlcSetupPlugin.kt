import lin.LinuxTasksConfigure
import mac.MacTasksConfigure
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.TaskProvider
import win.WindowsTasksConfigure

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
        val tasksConfigureList = listOf(
            WindowsTasksConfigure(project, vlcSetupExtension),
            LinuxTasksConfigure(project, vlcSetupExtension),
            MacTasksConfigure(project, vlcSetupExtension)
        )
        val vlcSetupTasks = mutableListOf<TaskProvider<*>>()
        for (tasksConfigure in tasksConfigureList) {
            val vlcSetupTask = tasksConfigure.configure()
            vlcSetupTask?.let(vlcSetupTasks::add)
        }

        /**
         * See https://docs.gradle.org/current/userguide/working_with_files.html
         */
        project
            .tasks
            .matching { it.name == "processResources" }
            .all { it.dependsOn(vlcSetupTasks) }
        // If the windowsCopyPath is inside the path defined as Compose Multiplatform resources directory then
        // it makes the task prepareAppResources implicitly depend on vlcSetup task because its input directory
        // contains output files/directories (windowsCopyPath) of vlcSetup task.
        // So, here, the prepareAppResources task (if/when it exists) is configured to run after vlcSetup task.
        project
            .tasks
            .matching { it.name == "prepareAppResources" }
            .all { it.mustRunAfter(vlcSetupTasks) }
    }
}
