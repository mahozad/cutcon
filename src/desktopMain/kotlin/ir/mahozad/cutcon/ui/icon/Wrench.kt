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
        imageVector = Icons.Custom.Wrench,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Wrench: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Wrench") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(12.4049f, 15.405f)
                lineToRelative(5.0358f, 5.0358f)
                lineToRelative(3.0f, -3.0f)
                lineToRelative(-5.0358f, -5.0358f)
                lineToRelative(-0.808f, -0.808f)
                curveToRelative(0.272f, -0.6625f, 0.412f, -1.3717f, 0.4121f, -2.0879f)
                curveToRelative(0.0f, -3.0376f, -2.4624f, -5.5f, -5.5f, -5.5f)
                curveToRelative(-0.8221f, 0.003f, -1.633f, 0.1908f, -2.373f, 0.5488f)
                lineToRelative(4.6309f, 4.6309f)
                lineToRelative(-2.5781f, 2.5781f)
                lineToRelative(-4.6309f, -4.6309f)
                curveToRelative(-0.358f, 0.7401f, -0.5455f, 1.551f, -0.5488f, 2.373f)
                curveToRelative(0.0f, 3.0376f, 2.4624f, 5.5f, 5.5f, 5.5f)
                curveToRelative(0.7162f, -1.0E-4f, 1.4254f, -0.1401f, 2.0879f, -0.4121f)
                close()
            }
        }
        return icon!!
    }
