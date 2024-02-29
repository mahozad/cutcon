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
        imageVector = Icons.Custom.Rewind5En,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Rewind5En: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Rewind5En") {
            path(
                name = "minus",
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2f
            ) {
                moveTo(4f, 12f)
                horizontalLineToRelative(6f)
            }
            path(
                name = "five",
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2f
            ) {
                moveTo(18f, 8f)
                horizontalLineToRelative(-5f)
                verticalLineToRelative(4f)
                horizontalLineToRelative(4f)
                verticalLineToRelative(4f)
                horizontalLineToRelative(-5f)
            }
        }
        return icon!!
    }
