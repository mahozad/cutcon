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
        imageVector = Icons.Custom.Curtain,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Curtain: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Curtain") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(3.0f, 3.0f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(18.0f)
                horizontalLineToRelative(-18.0f)
                close()
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(8.0f, 3.0f)
                curveTo(8.0f, 9.0f, 7.0f, 10.0f, 4.0f, 13.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(12.0f, 3.0f)
                curveToRelative(0.0f, 8.0f, 3.0f, 10.0f, 9.0f, 10.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(16.0f, 3.0f)
                curveToRelative(0.0f, 6.0f, 1.0f, 7.0f, 4.0f, 10.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(12.0f, 3.0f)
                curveTo(12.0f, 11.0f, 9.0f, 13.0f, 3.0f, 13.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(3.0f, 13.0f)
                curveToRelative(4.0f, 0.0f, 4.0f, 5.0f, 4.0f, 8.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(21.0f, 13.0f)
                curveToRelative(-4.0f, 0.0f, -4.0f, 5.0f, -4.0f, 8.0f)
            }
        }
        return icon!!
    }
