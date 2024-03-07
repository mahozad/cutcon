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
        imageVector = Icons.Custom.Shutter,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Shutter: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Shutter") {
            path(fill = SolidColor(defaultIconColor)) {
                moveToRelative(12.0f, 2.0f)
                curveToRelative(-2.6209f, 0.0f, -5.136f, 1.0345f, -7.0039f, 2.8731f)
                lineToRelative(3.5391f, 6.1269f)
                lineToRelative(5.1172f, -8.8613f)
                curveTo(13.1064f, 2.0468f, 12.5537f, 2.0004f, 12.0f, 2.0f)
                close()
                moveTo(14.6758f, 2.3652f)
                lineTo(11.1328f, 8.5f)
                horizontalLineToRelative(10.2324f)
                curveToRelative(-1.1199f, -2.9974f, -3.6066f, -5.2778f, -6.6895f, -6.1348f)
                close()
                moveTo(4.3008f, 5.666f)
                curveTo(2.8241f, 7.4476f, 2.011f, 9.6861f, 2.0f, 12.0f)
                curveToRelative(0.0098f, 0.8447f, 0.1266f, 1.6847f, 0.3477f, 2.5f)
                horizontalLineToRelative(7.0527f)
                close()
                moveTo(14.5996f, 9.4999f)
                lineTo(19.6992f, 18.3339f)
                curveTo(21.1759f, 16.5524f, 21.989f, 14.3139f, 22.0f, 12.0f)
                curveToRelative(-3.0E-4f, -0.8434f, -0.1072f, -1.6834f, -0.3184f, -2.5f)
                close()
                moveTo(15.4648f, 12.9999f)
                lineTo(10.3516f, 21.8574f)
                curveToRelative(0.5446f, 0.093f, 1.096f, 0.1407f, 1.6484f, 0.1426f)
                curveToRelative(2.6209f, 0.0f, 5.136f, -1.0345f, 7.0039f, -2.8731f)
                close()
                moveTo(2.6602f, 15.4999f)
                curveToRelative(1.1232f, 2.9864f, 3.604f, 5.2575f, 6.6777f, 6.1133f)
                lineToRelative(3.5293f, -6.1133f)
                close()
            }
        }
        return icon!!
    }
