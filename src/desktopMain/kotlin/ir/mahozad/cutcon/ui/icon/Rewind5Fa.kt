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
        imageVector = Icons.Custom.Rewind5Fa,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Rewind5Fa: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Rewind5Fa") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f,
            ) {
                moveToRelative(2.0f, 13.0f)
                horizontalLineToRelative(6.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f,
            ) {
                moveToRelative(14.0f, 8.0f)
                lineToRelative(-3.0f, 5.0f)
                lineToRelative(-1.0f, 2.0f)
                lineToRelative(1.0f, 2.0f)
                horizontalLineToRelative(3.0f)
                verticalLineToRelative(-3.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f,
            ) {
                moveToRelative(14.0f, 8.0f)
                lineToRelative(3.0f, 5.0f)
                lineToRelative(1.0f, 2.0f)
                lineToRelative(-1.0f, 2.0f)
                lineToRelative(-3.0f, 0.0f)
                lineToRelative(-0.0f, -3.0f)
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(14.0f, 6.0586f)
                lineTo(12.834f, 8.002f)
                lineTo(14.0f, 9.9434f)
                lineTo(15.166f, 8.0f)
                close()
            }
        }
        return icon!!
    }
