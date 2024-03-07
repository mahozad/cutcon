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
        imageVector = Icons.Custom.Undo,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Undo: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Undo") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(9.0f, 12.0f)
                lineTo(5.0f, 8.0f)
                lineTo(9.0f, 4.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(5.0f, 18.0f)
                horizontalLineToRelative(10.0f)
                curveToRelative(2.7614f, 0.0f, 5.0f, -2.2386f, 5.0f, -5.0f)
                curveToRelative(0.0f, -2.7614f, -2.2386f, -5.0f, -5.0f, -5.0f)
                horizontalLineTo(6.0f)
            }
        }
        return icon!!
    }
