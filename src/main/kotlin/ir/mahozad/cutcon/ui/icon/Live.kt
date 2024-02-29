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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.defaultIconColor

@Preview
@Composable
private fun IconPreview() {
    Image(
        imageVector = Icons.Custom.Live,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Live: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Live") {
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(12.0f, 12.0f)
                moveToRelative(-2.0f, 0.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, 4.0f, 0.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, -4.0f, 0.0f)
            }
            path(
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Butt,
            ) {
                moveToRelative(15.0f, 6.8038f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, true, 3.0f, 5.1962f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, true, -3.0f, 5.1962f)
            }
            path(
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Butt
            ) {
                moveToRelative(17.0f, 3.3397f)
                arcToRelative(10.0f, 10.0f, 0.0f, false, true, 5.0f, 8.6603f)
                arcToRelative(10.0f, 10.0f, 0.0f, false, true, -5.0f, 8.6603f)
            }
            path(
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Butt
            ) {
                moveToRelative(9.0f, 17.196f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, true, -3.0f, -5.1962f)
                arcToRelative(6.0f, 6.0f, 0.0f, false, true, 3.0f, -5.1962f)
            }
            path(
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f,
                strokeLineCap = StrokeCap.Butt
            ) {
                moveToRelative(7.0f, 20.66f)
                arcToRelative(10.0f, 10.0f, 0.0f, false, true, -5.0f, -8.6603f)
                arcToRelative(10.0f, 10.0f, 0.0f, false, true, 5.0f, -8.6603f)
            }
        }
        return icon!!
    }
