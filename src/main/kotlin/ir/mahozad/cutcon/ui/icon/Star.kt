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
import androidx.compose.ui.graphics.StrokeJoin.Companion.Round
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.defaultIconColor

@Preview
@Composable
private fun IconPreview() {
    Image(
        imageVector = Icons.Custom.Star,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Star: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Star") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f,
                strokeLineJoin = Round
            ) {
                moveTo(12.0f, 3.5f)
                lineToRelative(2.35f, 4.76f)
                lineToRelative(5.26f, 0.764f)
                lineToRelative(-3.8f, 3.71f)
                lineToRelative(0.898f, 5.24f)
                lineToRelative(-4.7f, -2.47f)
                lineToRelative(-4.7f, 2.47f)
                lineToRelative(0.898f, -5.24f)
                lineToRelative(-3.8f, -3.71f)
                lineToRelative(5.26f, -0.764f)
                close()
            }
        }
        return icon!!
    }
