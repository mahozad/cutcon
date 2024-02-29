package ir.mahozad.cutcon.ui

import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.asSkiaBitmap
import androidx.compose.ui.graphics.toAwtImage
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.*
import ir.mahozad.cutcon.defaultLanguage
import ir.mahozad.cutcon.ui.panel.SidePanel
import kotlinx.coroutines.test.runTest
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import javax.imageio.ImageIO
import kotlin.io.path.Path
import kotlin.io.path.outputStream
import kotlin.io.path.readBytes
import kotlin.io.path.writeBytes

/**
 * See https://github.com/JetBrains/compose-multiplatform/issues/2520
 */
class UiTest {
    @get:Rule
    val composeTestRule = createComposeRule()

    @Ignore
    @Test
    fun exampleTest1() {
        composeTestRule.setContent {
            Text(text = "test")
        }
        composeTestRule
            .onNodeWithText(text = "test")
            .performClick()
            .assertIsDisplayed()
    }

    @Ignore
    @Test
    fun exampleTest2() = runTest {
        // To test Composable components that need Window or [Frame]WindowScope,
        // we may think we can do this:
        // composeTestRule.setContent {
        //     Window({}) {
        //         TheComposable()
        //     }
        // }
        // But it does not work because Window cannot be seen through by the test rule.
        // print the hierarchy with println(composeTestRule.onRoot().printToString()) to see.
        // See https://github.com/JetBrains/compose-multiplatform/issues/2107
        val fakeWindowScope = object : FrameWindowScope {
            override val window get() = ComposeWindow()
        }
        with(fakeWindowScope) {
            composeTestRule.setContent {
                SidePanel()
            }
        }

        composeTestRule.awaitIdle()

        println(composeTestRule.onRoot().printToString())

        composeTestRule
            .onNodeWithText(defaultLanguage.messages.btnLblStartConversion)
            .assertIsDisplayed()
    }

    /**
     * See https://stackoverflow.com/a/76817459
     */
    @Ignore
    @Test
    @OptIn(ExperimentalTestApi::class)
    fun takeExampleScreenshot() = runDesktopComposeUiTest(width = 400, height = 50) {
        setContent {
            Surface {
                Text(text = "test")
            }
        }

        val referencePath = Path("reference.png")
        val screenshot = Image.makeFromBitmap(captureToImage().asSkiaBitmap())
        val actualPath = Path("screenshot.png")
        val actualData = screenshot.encodeToData(EncodedImageFormat.PNG) ?: error("Could not encode image as png")
        actualPath.writeBytes(actualData.bytes)

        assert(actualPath.readBytes().contentEquals(referencePath.readBytes())) {
            "The screenshot '$actualPath' does not match the reference '$referencePath'"
        }
    }

    @Ignore
    @Test
    fun takeAnotherExampleScreenshot() {
        composeTestRule.setContent {
            Surface {
                Text(text = "test")
            }
        }
        val image = composeTestRule.onRoot().captureToImage()
        ImageIO.write(image.toAwtImage(), "PNG", Path("output.png").outputStream())
    }

    /**
     * FFmpeg 5.1 gpl
     * ffmpeg.exe -f gdigrab -framerate 33.3333333 -i title="SplashScreenCreator" -plays 0 -y out.apng
     * ffmpeg.exe -ss 6s -to 9s -i out.apng -plays 0 out-trimmed.apng
     * apngtogif converter 1.8 with threshold 64 to create the gif
     * optimize the GIF with https://ezgif.com/optimize
     */
    @Test
    fun createSplashScreen() {
        application(exitProcessOnExit = false) {
            Window(
                title = "SplashScreenCreator",
                undecorated = true,
                transparent = true,
                resizable = false,
                state = rememberWindowState(
                    size = DpSize.Unspecified,
                    position = WindowPosition(Alignment.Center)
                ),
                onCloseRequest = ::exitApplication
            ) {
                val rotation by rememberInfiniteTransition()
                    .animateFloat(
                        initialValue = 0f,
                        targetValue = 30f,
                        animationSpec = infiniteRepeatable(
                            tween(
                                durationMillis = 3000,
                                easing = LinearEasing
                            )
                        )
                    )
                Box(modifier = Modifier.size(180.dp)) {
                    Image(
                        painter = painterResource("logo-background.svg"),
                        contentDescription = null,
                        modifier = Modifier.rotate(rotation)
                    )
                    Image(
                        painter = painterResource("logo.svg"),
                        contentDescription = null
                    )
                }
            }
        }
    }
}
