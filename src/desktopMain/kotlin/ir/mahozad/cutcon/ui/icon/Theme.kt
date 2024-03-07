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
        imageVector = Icons.Custom.Theme,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Theme: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Theme") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(11.7782f, 4.2218f)
                lineToRelative(7.7782f, 7.7782f)
                lineToRelative(-7.7782f, 7.7782f)
                lineToRelative(-7.7782f, -7.7782f)
                close()
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(12.4853f, 4.9289f)
                lineTo(9.6569f, 2.1005f)
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(20.7782f, 18.7782f)
                moveToRelative(-2.0f, 0.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, 4.0f, 0.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, -4.0f, 0.0f)
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveToRelative(19.0461f, 17.7782f)
                lineToRelative(1.7321f, -3.0f)
                lineToRelative(1.7321f, 3.0f)
                close()
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(4.0f, 12.0f)
                lineTo(19.0f, 12.0f)
                lineTo(12.0f, 19.0f)
                close()
            }
        }
        return icon!!
    }
