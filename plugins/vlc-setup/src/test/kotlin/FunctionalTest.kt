import org.assertj.core.api.Assertions.assertThat
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.CleanupMode
import org.junit.jupiter.api.io.TempDir
import java.nio.file.Path
import kotlin.io.path.Path
import kotlin.io.path.absolute
import kotlin.io.path.createDirectories
import kotlin.io.path.div
import kotlin.io.path.invariantSeparatorsPathString
import kotlin.io.path.listDirectoryEntries
import kotlin.io.path.name
import kotlin.io.path.toPath
import kotlin.io.path.writeText

class FunctionalTest {

    private fun findPluginDirectory(): Path? {
        // return javaClass.getResource("/placeholder.txt")?.toURI()?.toPath()?.parent
        var pluginDirectory = javaClass.getProtectionDomain().codeSource.location.toURI().toPath()
        while (pluginDirectory.listDirectoryEntries().singleOrNull { it.name == "build.gradle.kts" } == null) {
            pluginDirectory = pluginDirectory.parent
        }
        return pluginDirectory.absolute()
    }

    // Note that if running the test for the first time, it may take a very long time to execute
    // because it has to download the Gradle, plugins, and dependencies to the testkit dir (by default, build/tmp/)
    @Test
    fun `Example functional test`(
        @TempDir(cleanup = CleanupMode.NEVER) testUserProjectDirectory: Path
    ) {
        println("testUserProjectDirectory = $testUserProjectDirectory")
        val pluginDirectory = findPluginDirectory() ?: error("Plugin directory not found")
        (testUserProjectDirectory / "settings.gradle.kts").writeText(
            /*language=kotlin*/
            """
                rootProject.name = "PluginTester"
                pluginManagement {
                    includeBuild("${pluginDirectory.invariantSeparatorsPathString}")
                    repositories {
                        gradlePluginPortal()
                        mavenCentral()
                    }
                }
            """
        )
        (testUserProjectDirectory / "build.gradle.kts").writeText(
            /*language=kotlin*/
            """
                import org.jetbrains.compose.desktop.application.dsl.TargetFormat.*
                plugins {
                    id("org.jetbrains.kotlin.jvm") version "1.9.24"
                    id("org.jetbrains.compose") version "1.6.11"
                    id("vlc-setup")
                }
                vlcSetup {
                    windowsCopyPath = file("temp/") // Relative to testUserProjectDirectory
                }
                compose.desktop {
                    application {
                        mainClass = "com.example.test"
                    }
                }
            """
        )
        (testUserProjectDirectory / "gradle.properties").writeText(
            /*language=properties*/
            """
                systemProp.socksProxyHost=127.0.0.1
                systemProp.socksProxyPort=14395
            """
        )

        val runnerCacheDir = System
            .getProperty("user.home")
            .let(::Path)
            .resolve(".gradle")
            .resolve("temp-for-plugin-test")
            .createDirectories()

        val gradleRunnerExecution = GradleRunner
            .create()
            // Can use a custom directory instead of the default build/tmp/ so that
            // downloaded gradle files are not removed when cleaning the project
            .withTestKitDir(runnerCacheDir.toFile())
            .withProjectDir(testUserProjectDirectory.toFile())
            .withArguments("vlcSetup", "--stacktrace")
            .build()

        println("============== Test build output ==============")
        println(gradleRunnerExecution.output)
        println("===============================================")
        assertThat(gradleRunnerExecution.task(":vlcSetup")?.outcome).isEqualTo(TaskOutcome.SUCCESS)
    }
}
