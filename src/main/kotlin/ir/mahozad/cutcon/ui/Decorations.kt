package ir.mahozad.cutcon.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.window.WindowDraggableArea
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.ComposeWindow
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.WindowScope
import com.sun.jna.Native
import com.sun.jna.NativeLibrary
import com.sun.jna.Pointer
import com.sun.jna.Structure
import com.sun.jna.platform.win32.BaseTSD.LONG_PTR
import com.sun.jna.platform.win32.User32
import com.sun.jna.platform.win32.WinDef.*
import com.sun.jna.platform.win32.WinUser.*
import com.sun.jna.win32.W32APIOptions
import io.github.oshai.kotlinlogging.KotlinLogging.logger
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.ui.icon.Close
import ir.mahozad.cutcon.ui.icon.Icons
import ir.mahozad.cutcon.ui.icon.Minimize

/**
 * It is called client-side decorations.
 * See https://en.wikipedia.org/wiki/Client-side_decoration
 */
@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun WindowScope.WindowDecoration(
    isDecorationVisible: Boolean,
    icon: Painter,
    title: @Composable (defaultFontSize: TextUnit) -> Unit,
    isMinimizable: Boolean,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    val windowHandle = remember(window) {
        val windowPointer = (window as? ComposeWindow)
            ?.windowHandle
            ?.let(::Pointer)
            ?: Native.getWindowPointer(window)
        HWND(windowPointer)
    }
    remember(windowHandle) { CustomWindowProcedure(windowHandle) }
    // For rounded corners, the window transparent should be set to true which requires
    // window undecorated to be set to true as well which causes the workaround for
    // window shadow and minimize/restore/close animations to not work anymore.
    Surface(modifier = Modifier.fillMaxHeight() /* Ensures the content fills the whole window height */) {
        Column {
            if (isDecorationVisible) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    WindowDraggableArea(modifier = Modifier.fillMaxWidth().height(30.dp)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Spacer(Modifier.width(8.dp))
                            Image(
                                painter = icon,
                                contentDescription = Messages.ICO_DSC_TITLE_BAR_ICON,
                                modifier = Modifier.height(20.dp)
                            )
                            Spacer(Modifier.width(2.dp))
                            title(defaultFontSize)
                            Spacer(modifier = Modifier.weight(1f))
                            if (isMinimizable && window is ComposeWindow) {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .width(48.dp)
                                        .fillMaxHeight()
                                        .clickable {
                                            // See the code below for why
                                            User32.INSTANCE.CloseWindow(windowHandle)
                                        }
                                ) {
                                    Icon(
                                        imageVector = Icons.Custom.Minimize,
                                        contentDescription = Messages.ICO_DSC_MINIMIZE,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                            var isCloseHovered by remember { mutableStateOf(false) }
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier
                                    .width(48.dp)
                                    .onPointerEvent(PointerEventType.Enter, onEvent = { isCloseHovered = true })
                                    .onPointerEvent(PointerEventType.Exit, onEvent = { isCloseHovered = false })
                                    .background(if (isCloseHovered) Color(0xffc42b1c) else Color(0x00c42b1c))
                                    .fillMaxHeight()
                                    .clickable(onClick = onCloseRequest)
                            ) {
                                Icon(
                                    imageVector = Icons.Custom.Close,
                                    contentDescription = Messages.ICO_DSC_CLOSE,
                                    modifier = Modifier.size(14.dp),
                                    tint = if (isCloseHovered) Color.White else MaterialTheme.colors.onSurface
                                )
                            }
                        }
                    }
                }
            }
            content()
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun DialogDecoration(
    icon: Painter,
    title: @Composable (defaultFontSize: TextUnit) -> Unit,
    modifier: Modifier,
    onCloseRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier
            .border(Dp.Hairline, Color.Gray, RoundedCornerShape(8.dp))
            .clip(RoundedCornerShape(8.dp))
    ) {
        Column {
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth().height(30.dp)
                ) {
                    Spacer(Modifier.width(8.dp))
                    Image(
                        painter = icon,
                        contentDescription = Messages.ICO_DSC_TITLE_BAR_ICON,
                        modifier = Modifier.height(20.dp)
                    )
                    Spacer(Modifier.width(2.dp))
                    title(defaultFontSize)
                    Spacer(modifier = Modifier.weight(1f))
                    var isHovered by remember { mutableStateOf(false) }
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier
                            .width(48.dp)
                            .onPointerEvent(PointerEventType.Enter, onEvent = { isHovered = true })
                            .onPointerEvent(PointerEventType.Exit, onEvent = { isHovered = false })
                            .background(if (isHovered) Color(0xffc42b1c) else Color(0x00c42b1c))
                            .fillMaxHeight()
                            .clickable(onClick = onCloseRequest)
                    ) {
                        Icon(
                            imageVector = Icons.Custom.Close,
                            contentDescription = Messages.ICO_DSC_CLOSE,
                            modifier = Modifier.size(14.dp),
                            tint = if (isHovered) Color.White else MaterialTheme.colors.onSurface
                        )
                    }
                }
            }
            content()
        }
    }
}

//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
// These are to retain the Windows minimize, restore, and close animations for the application window.
// See https://github.com/JetBrains/compose-multiplatform/issues/3388
// and https://github.com/JetBrains/jewel/pull/173
// and https://github.com/kalibetre/CustomDecoratedJFrame
// and https://gist.github.com/Guerra24/429de6cadda9318b030a7d12d0ad58d4
// and https://medium.com/@kalbetre/customizing-the-title-bar-of-an-application-window-50a4ac3ed27e
// and https://github.com/JetBrains/compose-multiplatform/issues/3295#issuecomment-1668607200
// and https://learn.microsoft.com/en-us/windows/win32/api/uxtheme/ns-uxtheme-margins
// and https://learn.microsoft.com/en-us/windows/win32/api/dwmapi/nf-dwmapi-dwmextendframeintoclientarea
// and https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-showwindow
// and https://learn.microsoft.com/en-us/windows/win32/dwm/customframe
// and https://learn.microsoft.com/en-us/windows/apps/develop/title-bar
// and https://github.com/deimos1877/BorderlessWindow
// and https://github.com/java-native-access/jna/issues/1430
// and https://stackoverflow.com/q/16765561
// and https://stackoverflow.com/q/1742218
// and https://stackoverflow.com/q/3979800
// and https://stackoverflow.com/q/62374427
// and https://bugs.openjdk.org/browse/JDK-8211907
// and https://bugs.openjdk.org/browse/JDK-8037575
// and https://github.com/jphp-group/jphp-gui-win-helpers-ext/blob/master/src-jvm/main/java/net/rebzzel/jphp/windows/extender/classes/Ext4JphpWindows.java
// and https://github.com/ocornut/imgui/issues/5315#issue-1236695428
// and https://github.com/ocornut/imgui/issues/5315
// and https://stackoverflow.com/q/22165258
// and https://stackoverflow.com/q/51008461
// and https://stackoverflow.com/q/34638183
// and https://stackoverflow.com/a/18522099
// and https://stackoverflow.com/a/27254024
// and https://stackoverflow.com/a/76366710
// Instead of (window as? ComposeWindow)?.isMinimized = true call the following to preserve the animation
// Requires the following dependencies:
// implementation("net.java.dev.jna:jna-jpms:5.13.0")
// implementation("net.java.dev.jna:jna-platform-jpms:5.13.0")
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////
//////////////////////////////////////////////////////////////////////////////////////////////////////

@Suppress("FunctionName")
private interface User32Ex : User32 {
    fun SetWindowLong(hWnd: HWND, nIndex: Int, wndProc: WindowProc): LONG_PTR
    fun SetWindowLongPtr(hWnd: HWND, nIndex: Int, wndProc: WindowProc): LONG_PTR
    fun CallWindowProc(proc: LONG_PTR, hWnd: HWND, uMsg: Int, uParam: WPARAM, lParam: LPARAM): LRESULT
}

// See https://stackoverflow.com/q/62240901
@Structure.FieldOrder(
    "leftBorderWidth",
    "rightBorderWidth",
    "topBorderHeight",
    "bottomBorderHeight"
)
data class WindowMargins(
    @JvmField var leftBorderWidth: Int,
    @JvmField var rightBorderWidth: Int,
    @JvmField var topBorderHeight: Int,
    @JvmField var bottomBorderHeight: Int
) : Structure(), Structure.ByReference

private class CustomWindowProcedure(private val windowHandle: HWND) : WindowProc {

    private val logger = logger(name = CustomWindowProcedure::class.simpleName ?: "")
    // See https://learn.microsoft.com/en-us/windows/win32/winmsg/about-messages-and-message-queues#system-defined-messages
    private val WM_NCCALCSIZE = 0x0083
    private val WM_NCHITTEST = 0x0084
    private val GWLP_WNDPROC = -4
    private val margins = WindowMargins(
        leftBorderWidth = 0,
        topBorderHeight = 0,
        rightBorderWidth = -1,
        bottomBorderHeight = -1
    )
    private val USER32EX = runCatching { Native.load("user32", User32Ex::class.java, W32APIOptions.DEFAULT_OPTIONS) }
        .onFailure { logger.warn(it) { "Could not load user32 library" } }
        .getOrNull()
    // The default window procedure to call its methods when the default method behaviour is desired/sufficient
    private val defaultWndProc = if (is64Bit()) {
        USER32EX?.SetWindowLongPtr(windowHandle, GWLP_WNDPROC, this) ?: LONG_PTR(-1)
    } else {
        USER32EX?.SetWindowLong(windowHandle, GWLP_WNDPROC, this) ?: LONG_PTR(-1)
    }

    init {
        enableResizability()
        enableBorderAndShadow()
        // enableTransparency(alpha = 255.toByte())
    }

    override fun callback(hWnd: HWND, uMsg: Int, wParam: WPARAM, lParam: LPARAM): LRESULT {
        return when (uMsg) {
            // Returns 0 to make the window not draw the non-client area (title bar and border)
            // thus effectively making all the window our client area
            WM_NCCALCSIZE -> { LRESULT(0) }
            // The CallWindowProc function is used to pass messages that
            // are not handled by our custom callback function to the default windows procedure
            WM_NCHITTEST -> { USER32EX?.CallWindowProc(defaultWndProc, hWnd, uMsg, wParam, lParam) ?: LRESULT(0) }
            WM_DESTROY -> { USER32EX?.CallWindowProc(defaultWndProc, hWnd, uMsg, wParam, lParam) ?: LRESULT(0) }
            else -> { USER32EX?.CallWindowProc(defaultWndProc, hWnd, uMsg, wParam, lParam) ?: LRESULT(0) }
        }
    }

    /**
     * For this to take effect, also set `resizable` argument of Compose Window to `true`.
     */
    private fun enableResizability() {
        val style = USER32EX?.GetWindowLong(windowHandle, GWL_STYLE) ?: return
        val newStyle = style or WS_CAPTION
        USER32EX.SetWindowLong(windowHandle, GWL_STYLE, newStyle)
    }

    /**
     * To disable window border and shadow, pass (0, 0, 0, 0) as window margins
     * (or, simply, don't call this function).
     */
    private fun enableBorderAndShadow() {
        val dwmApi = "dwmapi"
            .runCatching(NativeLibrary::getInstance)
            .onFailure { logger.warn(it) { "Could not load dwmapi library" } }
            .getOrNull()
        // dwmApi
        //     ?.runCatching { getFunction("DwmSetWindowAttribute") }
        //     ?.getOrNull()
        //     ?.invoke(arrayOf(windowHandle, 2, IntByReference(2), 4))
        dwmApi
            ?.runCatching { getFunction("DwmExtendFrameIntoClientArea") }
            ?.onFailure { logger.warn(it) { "Could not enable window native decorations (border/shadow/rounded corners)" } }
            ?.getOrNull()
            ?.invoke(arrayOf(windowHandle, margins))
    }

    /**
     * See https://learn.microsoft.com/en-us/windows/win32/api/winuser/nf-winuser-setlayeredwindowattributes
     * @param alpha 0 for transparent, 255 for opaque
     */
    private fun enableTransparency(alpha: Byte) {
        val defaultStyle = User32.INSTANCE.GetWindowLong(windowHandle, GWL_EXSTYLE)
        val newStyle = defaultStyle or WS_EX_LAYERED
        USER32EX?.SetWindowLong(windowHandle, GWL_EXSTYLE, newStyle)
        USER32EX?.SetLayeredWindowAttributes(windowHandle, 0, alpha, LWA_ALPHA)
    }

    private fun is64Bit(): Boolean {
        val bitMode = System.getProperty("com.ibm.vm.bitmode")
        val model = System.getProperty("sun.arch.data.model", bitMode)
        return model == "64"
    }
}
