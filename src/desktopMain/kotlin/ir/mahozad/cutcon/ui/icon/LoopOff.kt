package ir.mahozad.cutcon.ui.icon

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.defaultIconColor

@Preview
@Composable
private fun IconPreview() {
    Image(
        imageVector = Icons.Custom.LoopOff,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.LoopOff: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.LoopOff") {
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(12.0f, 9.0f)
                lineTo(16.0254f, 5.5f)
                lineTo(12.0f, 2.0f)
                verticalLineToRelative(2.5f)
                curveToRelative(-2.6779f, 0.0f, -5.1572f, 1.4309f, -6.4961f, 3.75f)
                curveToRelative(-1.3389f, 2.3191f, -1.3389f, 5.1809f, 0.0f, 7.5f)
                lineToRelative(1.7324f, -1.0f)
                curveToRelative(-0.9833f, -1.7031f, -0.9833f, -3.7969f, 0.0f, -5.5f)
                curveTo(8.2196f, 7.5469f, 10.0334f, 6.5f, 12.0f, 6.5f)
                close()
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(12.0f, 15.0f)
                lineTo(7.9746f, 18.5f)
                lineTo(12.0f, 22.0f)
                verticalLineToRelative(-2.5f)
                curveToRelative(2.6779f, 0.0f, 5.1572f, -1.4309f, 6.4961f, -3.75f)
                curveToRelative(1.3389f, -2.3191f, 1.3389f, -5.1809f, 0.0f, -7.5f)
                lineToRelative(-1.7324f, 1.0f)
                curveToRelative(0.9833f, 1.7031f, 0.9833f, 3.7969f, 0.0f, 5.5f)
                curveTo(15.7804f, 16.4531f, 13.9666f, 17.5f, 12.0f, 17.5f)
                close()
            }
        }
        return icon!!
    }
