package win

import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.*

// Note that if running the tests for the first time, it may take a very long time to execute because
// it has to download the Gradle, plugins, and dependencies to the testkit dir (by default, build/tmp/)
class FunctionalTest {

    @TempDir(cleanup = CleanupMode.NEVER)
    lateinit var testUserProjectDirectory: Path

    val pluginDirectory by lazy {
        // return@lazy javaClass.getResource("/placeholder.txt")?.toURI()?.toPath()?.parent
        var pluginDirectory = javaClass.getProtectionDomain().codeSource.location.toURI().toPath()
        while (pluginDirectory.listDirectoryEntries().singleOrNull { it.name == "build.gradle.kts" } == null) {
            pluginDirectory = pluginDirectory.parent
        }
        return@lazy pluginDirectory.absolute()
    }

    val runnerCacheDir = System
        .getProperty("user.home")
        .let(::Path)
        .resolve(".gradle")
        .resolve("temp-for-plugin-test")
        .createDirectories()

    /*language=properties*/
    val propertiesContent = """
        systemProp.socksProxyHost=127.0.0.1
        systemProp.socksProxyPort=14395
    """

    /*language=kotlin*/
    val settingsContent = """
        rootProject.name = "PluginTester"
        pluginManagement {
            includeBuild("${pluginDirectory.invariantSeparatorsPathString}")
            repositories {
                gradlePluginPortal()
                mavenCentral()
            }
        }
    """

    /*language=kotlin*/
    fun createBuildContent(extraContent: () -> String) = """
        import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*
        plugins {
            id("org.jetbrains.kotlin.jvm") version "2.2.0-RC"
            id("org.jetbrains.kotlin.plugin.compose") version "2.2.0-RC"
            id("org.jetbrains.compose") version "1.8.0"
            id("vlc-setup")
        }
        compose.desktop {
            application {
                mainClass = "com.example.test"
            }
        }
        ${extraContent()}
    """

    fun runGradle(vararg arguments: String): BuildResult {
        return GradleRunner
            .create()
            // Can use a custom directory instead of the default build/tmp/ so that
            // downloaded gradle files are not removed when cleaning the project
            .withTestKitDir(runnerCacheDir.toFile())
            .withProjectDir(testUserProjectDirectory.toFile())
            .withArguments(*arguments)
            .build()
    }

    @Test
    fun `The plugins should be copied to the specified pathToCopyVlcWindowsFilesTo directory`() {
        println("testUserProjectDirectory = $testUserProjectDirectory")
        (testUserProjectDirectory / "settings.gradle.kts").writeText(settingsContent)
        (testUserProjectDirectory / "gradle.properties").writeText(propertiesContent)
        (testUserProjectDirectory / "build.gradle.kts").writeText(createBuildContent {
            /*language=kotlin*/
            """
                vlcSetup {
                    pathToCopyVlcWindowsFilesTo = file("temp/") // Relative to testUserProjectDirectory
                }
            """
        })

        val gradleExecutionResult = runGradle("vlcSetup", "--stacktrace")

        println("============== Test build output ==============")
        println(gradleExecutionResult.output)
        println("===============================================")
        assertThat(gradleExecutionResult.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `When the shouldCompressVlcFiles is set to true, should compress the plugins`() {
        println("testUserProjectDirectory = $testUserProjectDirectory")
        val windowsPath = testUserProjectDirectory.resolve("windows-plugins").toFile()
        (testUserProjectDirectory / "settings.gradle.kts").writeText(settingsContent)
        (testUserProjectDirectory / "gradle.properties").writeText(propertiesContent)
        (testUserProjectDirectory / "build.gradle.kts").writeText(createBuildContent {
            /*language=kotlin*/
            """
                vlcSetup {
                    shouldCompressVlcFiles = true
                    pathToCopyVlcWindowsFilesTo = File("${windowsPath.absoluteFile.invariantSeparatorsPath}")
                }
            """
        })

        val gradleExecutionResult = runGradle("vlcSetup", "--stacktrace")

        assertThat(windowsPath.resolve("libvlccore.dll").length()).isBetween(1_300_000, 1_600_000)
        assertThat(gradleExecutionResult.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `When the shouldCompressVlcFiles is set to false, should not compress the plugins`() {
        println("testUserProjectDirectory = $testUserProjectDirectory")
        val windowsPath = testUserProjectDirectory.resolve("windows-plugins").toFile()
        (testUserProjectDirectory / "settings.gradle.kts").writeText(settingsContent)
        (testUserProjectDirectory / "gradle.properties").writeText(propertiesContent)
        (testUserProjectDirectory / "build.gradle.kts").writeText(createBuildContent {
            /*language=kotlin*/
            """
                vlcSetup {
                    shouldCompressVlcFiles = false
                    pathToCopyVlcWindowsFilesTo = File("${windowsPath.absoluteFile.invariantSeparatorsPath}")
                }
            """
        })

        val gradleExecutionResult = runGradle("vlcSetup", "--stacktrace")

        assertThat(windowsPath.resolve("libvlccore.dll").length()).isBetween(2_700_000, 3_000_000)
        assertThat(gradleExecutionResult.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `When the shouldCompressVlcFiles was true and is set to false and then running the task again, plugins in pathToCopyVlcWindowsFilesTo should update`() {
        println("testUserProjectDirectory = $testUserProjectDirectory")
        val windowsPath = testUserProjectDirectory.resolve("windows-plugins").toFile()
        (testUserProjectDirectory / "settings.gradle.kts").writeText(settingsContent)
        (testUserProjectDirectory / "gradle.properties").writeText(propertiesContent)
        (testUserProjectDirectory / "build.gradle.kts").writeText(createBuildContent {
            /*language=kotlin*/
            """
                vlcSetup {
                    shouldCompressVlcFiles = true
                    pathToCopyVlcWindowsFilesTo = File("${windowsPath.absoluteFile.invariantSeparatorsPath}")
                }
            """
        })

        val gradleExecutionResult1 = runGradle("vlcSetup", "--stacktrace")
        (testUserProjectDirectory / "build.gradle.kts").writeText(createBuildContent {
            /*language=kotlin*/
            """
                vlcSetup {
                    shouldCompressVlcFiles = false
                    pathToCopyVlcWindowsFilesTo = File("${windowsPath.absoluteFile.invariantSeparatorsPath}")
                }
            """
        })
        val gradleExecutionResult2 = runGradle("vlcSetup", "--stacktrace")

        assertThat(windowsPath.resolve("libvlccore.dll").length()).isBetween(2_700_000, 3_000_000)
        assertThat(gradleExecutionResult1.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(gradleExecutionResult2.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `When the shouldCompressVlcFiles was false and is set to true and then running the task again, plugins in pathToCopyVlcWindowsFilesTo should update`() {
        println("testUserProjectDirectory = $testUserProjectDirectory")
        val windowsPath = testUserProjectDirectory.resolve("windows-plugins").toFile()
        (testUserProjectDirectory / "settings.gradle.kts").writeText(settingsContent)
        (testUserProjectDirectory / "gradle.properties").writeText(propertiesContent)
        (testUserProjectDirectory / "build.gradle.kts").writeText(createBuildContent {
            /*language=kotlin*/
            """
                vlcSetup {
                    shouldCompressVlcFiles = false
                    pathToCopyVlcWindowsFilesTo = File("${windowsPath.absoluteFile.invariantSeparatorsPath}")
                }
            """
        })

        val gradleExecutionResult1 = runGradle("vlcSetup", "--stacktrace")
        (testUserProjectDirectory / "build.gradle.kts").writeText(createBuildContent {
            /*language=kotlin*/
            """
                vlcSetup {
                    shouldCompressVlcFiles = true
                    pathToCopyVlcWindowsFilesTo = File("${windowsPath.absoluteFile.invariantSeparatorsPath}")
                }
            """
        })
        val gradleExecutionResult2 = runGradle("vlcSetup", "--stacktrace")

        assertThat(windowsPath.resolve("libvlccore.dll").length()).isBetween(1_300_000, 1_600_000)
        assertThat(gradleExecutionResult1.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(gradleExecutionResult2.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `When a plugin is deleted manually from the pathToCopyVlcWindowsFilesTo and then running the task again, should copy the missing file to pathToCopyVlcWindowsFilesTo`() {
        println("testUserProjectDirectory = $testUserProjectDirectory")
        val windowsPath = testUserProjectDirectory.resolve("windows-plugins").toFile()
        (testUserProjectDirectory / "settings.gradle.kts").writeText(settingsContent)
        (testUserProjectDirectory / "gradle.properties").writeText(propertiesContent)
        (testUserProjectDirectory / "build.gradle.kts").writeText(createBuildContent {
            /*language=kotlin*/
            """
                vlcSetup {
                    shouldCompressVlcFiles = false
                    pathToCopyVlcWindowsFilesTo = File("${windowsPath.absoluteFile.invariantSeparatorsPath}")
                }
            """
        })

        val gradleExecutionResult1 = runGradle("vlcSetup", "--stacktrace")
        windowsPath.resolve("libvlccore.dll").delete()
        val gradleExecutionResult2 = runGradle("vlcSetup", "--stacktrace")

        assertThat(windowsPath.resolve("libvlccore.dll")).exists()
        assertThat(gradleExecutionResult1.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(gradleExecutionResult2.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }

    @Test
    fun `When setting shouldIncludeAllVlcFiles from true to false (less plugins being included) and then running the task again, should remove the additional plugins in pathToCopyVlcWindowsFilesTo`() {
        println("testUserProjectDirectory = $testUserProjectDirectory")
        val windowsPath = testUserProjectDirectory.resolve("windows-plugins").toFile()
        (testUserProjectDirectory / "settings.gradle.kts").writeText(settingsContent)
        (testUserProjectDirectory / "gradle.properties").writeText(propertiesContent)
        (testUserProjectDirectory / "build.gradle.kts").writeText(createBuildContent {
            /*language=kotlin*/
            """
                vlcSetup {
                    shouldCompressVlcFiles = false
                    shouldIncludeAllVlcFiles = true
                    pathToCopyVlcWindowsFilesTo = File("${windowsPath.absoluteFile.invariantSeparatorsPath}")
                }
            """
        })

        val gradleExecutionResult1 = runGradle("vlcSetup", "--stacktrace")
        val samplePlugin = windowsPath.resolve("plugins/gui/libqt_plugin.dll")
        assertThat(samplePlugin).exists()
        (testUserProjectDirectory / "build.gradle.kts").writeText(createBuildContent {
            /*language=kotlin*/
            """
                vlcSetup {
                    shouldCompressVlcFiles = false
                    shouldIncludeAllVlcFiles = false
                    pathToCopyVlcWindowsFilesTo = File("${windowsPath.absoluteFile.invariantSeparatorsPath}")
                }
            """
        })
        val gradleExecutionResult2 = runGradle("vlcSetup", "--stacktrace")

        assertThat(samplePlugin).doesNotExist()
        assertThat(gradleExecutionResult1.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
        assertThat(gradleExecutionResult2.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
