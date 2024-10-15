import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

abstract class VlcInstallToolsLinuxTask : DefaultTask() {

    @TaskAction
    fun execute() {
        project.exec {
            it
                // To install libraries/programs using apt or apt-get, we need to use sudo
                // and, it in turn, needs the user password which is configured to be read
                // from standard input using the -S option and .setStandardInput(System.in)
                // See https://stackoverflow.com/q/21659637
                //
                // If the downloads fail, retrying (re-executing the task) or setting proxy in gradle.properties may help
                .commandLine(
                    "sudo", "-S",
                    "apt", "install",
                    "g++",
                    "make",
                    "libtool",
                    "automake",
                    "autopoint",
                    "pkg-config",
                    "flex",
                    "bison",
                    "lua5.2"
                )
                .setStandardInput(System.`in`)
        }
    }
}
