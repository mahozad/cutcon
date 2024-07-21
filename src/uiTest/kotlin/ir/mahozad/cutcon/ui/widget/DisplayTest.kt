package ir.mahozad.cutcon.ui.widget

import androidx.compose.foundation.background
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.runDesktopComposeUiTest
import androidx.compose.ui.unit.Density
import ir.mahozad.cutcon.defaultAudioImage
import ir.mahozad.cutcon.model.AspectRatio
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.io.path.*

@OptIn(ExperimentalTestApi::class)
class DisplayTest {

    @Test
    fun `When screen scaling (density) is 1 and the aspect ratio is source, image should have its intrinsic aspect ratio`() =
        runDesktopComposeUiTest(width = 300, height = 200) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    Display(
                        image = defaultAudioImage,
                        aspectRatio = AspectRatio.SOURCE,
                        modifier = Modifier.background(Color.Cyan)
                    )
                }
            }

            val referencePath = javaClass.getResource("/reference/1.png")?.toURI()?.toPath()
            val screenshot = Image.makeFromBitmap(captureToImage().asSkiaBitmap())
            val actualPath = createTempDirectory() / "screenshot.png"
            val actualData = screenshot.encodeToData(EncodedImageFormat.PNG) ?: error("Could not encode image as png")
            actualPath.writeBytes(actualData.bytes)

            assert(actualPath.readBytes().contentEquals(referencePath?.readBytes())) {
                "The screenshot '$actualPath' does not match the reference '$referencePath'"
            }
        }

    @Test
    fun `When screen scaling (density) is 1 and the aspect ratio is W16H9, image should have 16 to 9 aspect ratio`() =
        runDesktopComposeUiTest(width = 300, height = 200) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1f)) {
                    Display(
                        image = defaultAudioImage,
                        aspectRatio = AspectRatio.W16H9,
                        modifier = Modifier.background(Color.Cyan)
                    )
                }
            }

            val referencePath = javaClass.getResource("/reference/2.png")?.toURI()?.toPath()
            val screenshot = Image.makeFromBitmap(captureToImage().asSkiaBitmap())
            val actualPath = createTempDirectory() / "screenshot.png"
            val actualData = screenshot.encodeToData(EncodedImageFormat.PNG) ?: error("Could not encode image as png")
            actualPath.writeBytes(actualData.bytes)

            assert(actualPath.readBytes().contentEquals(referencePath?.readBytes())) {
                "The screenshot '$actualPath' does not match the reference '$referencePath'"
            }
        }

    @Disabled("FIXME: If the scaling of the OS screen is a custom value, the display image distorts")
    @Test
    fun `When screen scaling (density) is not 1 and the aspect ratio is source, image should have its intrinsic aspect ratio`() =
        runDesktopComposeUiTest(width = 300, height = 200) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1.25f)) {
                    Display(
                        image = defaultAudioImage,
                        aspectRatio = AspectRatio.SOURCE,
                        modifier = Modifier.background(Color.Cyan)
                    )
                }
            }

            val referencePath = javaClass.getResource("/reference/3.png")?.toURI()?.toPath()
            val screenshot = Image.makeFromBitmap(captureToImage().asSkiaBitmap())
            val actualPath = createTempDirectory() / "screenshot.png"
            val actualData = screenshot.encodeToData(EncodedImageFormat.PNG) ?: error("Could not encode image as png")
            actualPath.writeBytes(actualData.bytes)

            assert(actualPath.readBytes().contentEquals(referencePath?.readBytes())) {
                "The screenshot '$actualPath' does not match the reference '$referencePath'"
            }
        }

    @Disabled("FIXME: If the scaling of the OS screen is a custom value, the display image distorts")
    @Test
    fun `When screen scaling (density) is not 1 and the aspect ratio is W16H9, image should have 16 to 9 aspect ratio`() =
        runDesktopComposeUiTest(width = 300, height = 200) {
            setContent {
                CompositionLocalProvider(LocalDensity provides Density(1.25f)) {
                    Display(
                        image = defaultAudioImage,
                        aspectRatio = AspectRatio.W16H9,
                        modifier = Modifier.background(Color.Cyan)
                    )
                }
            }

            val referencePath = javaClass.getResource("/reference/4.png")?.toURI()?.toPath()
            val screenshot = Image.makeFromBitmap(captureToImage().asSkiaBitmap())
            val actualPath = createTempDirectory() / "screenshot.png"
            val actualData = screenshot.encodeToData(EncodedImageFormat.PNG) ?: error("Could not encode image as png")
            actualPath.writeBytes(actualData.bytes)

            assert(actualPath.readBytes().contentEquals(referencePath?.readBytes())) {
                "The screenshot '$actualPath' does not match the reference '$referencePath'"
            }
        }
}
