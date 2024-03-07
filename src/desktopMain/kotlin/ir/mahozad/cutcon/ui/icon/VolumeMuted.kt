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
        imageVector = Icons.Custom.VolumeMuted,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.VolumeMuted: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.VolumeMuted") {
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(12.0f, 3.0f)
                lineTo(8.0f, 7.0f)
                lineTo(3.0f, 8.0f)
                verticalLineToRelative(8.0f)
                lineToRelative(5.0f, 1.0f)
                lineToRelative(4.0f, 4.0f)
                close()
                moveTo(10.0f, 8.0f)
                verticalLineToRelative(8.0f)
                lineTo(9.0f, 15.0f)
                lineTo(5.0f, 14.0f)
                lineTo(5.0f, 10.0f)
                lineTo(9.0f, 9.0f)
                close()
            }
        }
        return icon!!
    }
