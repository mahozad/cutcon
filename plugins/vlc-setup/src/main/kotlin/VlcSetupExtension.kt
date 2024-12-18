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
abstract class VlcSetupExtension @Inject constructor(
    project: Project
) {

    companion object {
        const val PLUGIN_NAME = "vlcSetup"
        const val DEFAULT_VLC_VERSION = "3.0.21"
        const val DEFAULT_UPX_VERSION = "4.2.4"
    }

    // TODO: Make this enum, as our Linux variant does not support all VLC versions
    /**
     * The version of VLC to download and use.
     */
    val vlcVersion = project.objects.property(String::class.java).value(DEFAULT_VLC_VERSION)

    /**
     * The version of UPX tool to download which is used to compress VLC files.
     */
    val upxVersion = project.objects.property(String::class.java).value(DEFAULT_UPX_VERSION)

    /**
     * Whether to compress VLC files to preserve space without any change in functionality.
     */
    val shouldCompressVlcFiles = project.objects.property(Boolean::class.java).value(true)

    /**
     * Whether to include all VLC files to be able to play most formats/codecs.
     *
     * It may make the app significantly larger.
     */
    val shouldIncludeAllVlcFiles = project.objects.property(Boolean::class.java).value(false)

    /**
     * The directory to copy the VLC Linux files to.
     */
    val pathToCopyVlcLinuxFilesTo = project.objects.property(File::class.java)

    /**
     * The directory to copy the VLC Windows files to.
     */
    val pathToCopyVlcWindowsFilesTo = project.objects.property(File::class.java)
}
