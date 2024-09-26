import org.assertj.core.api.Assertions.assertThat
import org.gradle.testfixtures.ProjectBuilder
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

// See https://stackoverflow.com/questions/62282412/how-can-i-run-my-custom-gradle-task-in-the-unit-test
class UnitTest {

    @Test
    fun `When user project has no task named clean, applying the plugin should not throw error`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("vlc-setup")
    }

    @Test
    fun `When user project applies the plugin, the vlcSetup task should be added to the project`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("vlc-setup")
        assertThat(project.tasks.getByName("vlcSetup")).isInstanceOf(VlcSetupTask::class.java)
    }

    @Test
    fun `When user project applies the plugin, the vlcSetup extension should be available in the build script`() {
        val project = ProjectBuilder.builder().build()
        project.pluginManager.apply("vlc-setup")
        val vlcSetupExtension = project.extensions.getByName(VlcSetupExtension.PLUGIN_NAME)
        assertThat(vlcSetupExtension).isNotNull()
    }

    @Disabled(
        """
            Add the following dependencies to run this test:
            testImplementation("org.jetbrains.compose:compose-gradle-plugin:1.6.0")
            testImplementation("org.jetbrains.kotlin:kotlin-gradle-plugin:1.9.22")
        """
    )
    @Test
    fun `When user project applies the compose multiplatform plugin, -------------`() {
        val project = ProjectBuilder.builder().build()
        // project.pluginManager.apply("org.jetbrains.kotlin.jvm")
        project.pluginManager.apply("org.jetbrains.compose")
        project.pluginManager.apply("vlc-setup")
    }
}
