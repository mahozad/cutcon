import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction
import java.io.File

abstract class VlcInstallToolsLinuxTask : DefaultTask() {

    private val script: File by lazy {
        val destination = temporaryDir.resolve("script.sh")
        javaClass.getResourceAsStream("/script.sh")?.use { input ->
            destination.outputStream().use(input::copyTo)
        }
        destination
    }

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
                    "lua5.2",
                    //
                    //
                    //
                    "subversion",
                    "yasm",
                    "cvs",
                    "cmake",
                    "ragel"
                )
                .setStandardInput(System.`in`)
        }

        project.exec {
            it
                .commandLine("sh", "$script")
                .setStandardInput(System.`in`)
        }

//        project.exec {
//            it.commandLine(
//                "sudo", "-S", "apt", "build-dep", "vlc"
//            ).setStandardInput(System.`in`)
//        }
//
//        project.exec {
//            it.commandLine(
//                "sudo", "-S", "apt", "remove", "meson"
//            ).setStandardInput(System.`in`)
//        }
//
//        project.exec {
//            it.commandLine(
//                "sudo", "-S", "apt", "install", "python3-pip"
//            ).setStandardInput(System.`in`)
//        }
//
//        project.exec {
//            it.commandLine(
//                "sudo", "-S", "pip3", "install", "meson"
//            ).setStandardInput(System.`in`)
//        }
//
//        // https://github.com/xiph/rav1e/issues/2289
//        project.exec {
//            it.commandLine("sudo add-apt-repository ppa:team-xbmc/ppa")
//        }
//        project.exec {
//            it.commandLine(
//                "sudo", "-S", "apt", "install", "nasm=2.14.02"
//            ).setStandardInput(System.`in`)
//        }
    }
}
