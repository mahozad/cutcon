import org.gradle.api.Project
import java.io.File
import javax.inject.Inject

/**
 * Could have instead made this an interface and set the
 *  default values after crating an instance like this:
 *  vlcSetup.shouldCompressPlugins.convention(true)
 *
 * See https://github.com/JetBrains/compose-multiplatform/blob/master/gradle-plugins/compose/src/main/kotlin/org/jetbrains/compose/desktop/application/dsl/ProguardSettings.kt
 * and https://github.com/gradle/gradle/issues/8423
 */
abstract class VlcSetupExtension @Inject constructor(project: Project) {
    companion object {
        const val PLUGIN_NAME = "vlcSetup"
        const val DEFAULT_VLC_VERSION = "3.0.21"
        const val DEFAULT_UPX_VERSION = "4.2.4"
    }

    val vlcVersion = project.objects.property(String::class.java).value(DEFAULT_VLC_VERSION)
    val upxVersion = project.objects.property(String::class.java).value(DEFAULT_UPX_VERSION)
    val windowsCopyPath = project.objects.property(File::class.java)
    val shouldCompressPlugins = project.objects.property(Boolean::class.java).value(true)
    val shouldIncludeAllPlugins = project.objects.property(Boolean::class.java).value(false)
}
