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
        imageVector = Icons.Custom.AspectRatio,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.AspectRatio: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.AspectRatio") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(4.0f, 20.0f)
                lineToRelative(0.0f, -16.0f)
                lineToRelative(16.0f, -0.0f)
                lineToRelative(0.0f, 16.0f)
                close()
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(12.0f, 8.0f)
                horizontalLineTo(8.0f)
                verticalLineToRelative(4.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(16.0f, 12.0f)
                verticalLineToRelative(4.0f)
                horizontalLineToRelative(-4.0f)
            }
        }
        return icon!!
    }
