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
        imageVector = Icons.Custom.Notification,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Notification: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Notification") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(4.0f, 17.0f)
                lineTo(20.0f, 17.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(7.0f, 17.0f)
                verticalLineToRelative(-8.0f)
                curveToRelative(0.0f, -2.7614f, 2.2386f, -5.0f, 5.0f, -5.0f)
                curveToRelative(2.7614f, 0.0f, 5.0f, 2.2386f, 5.0f, 5.0f)
                verticalLineToRelative(8.0f)
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveToRelative(14.0f, 19.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, -1.0f, 1.732f)
                arcToRelative(2.0f, 2.0f, 0.0f, false, true, -2.0f, 0.0f)
                arcTo(2.0f, 2.0f, 0.0f, false, true, 10.0f, 19.0f)
            }
            path(
                fill = SolidColor(defaultIconColor),
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 1.0f
            ) {
                moveToRelative(11.0f, 4.0f)
                verticalLineToRelative(-2.0f)
                horizontalLineToRelative(2.0f)
                verticalLineToRelative(2.0f)
                close()
            }
        }
        return icon!!
    }
