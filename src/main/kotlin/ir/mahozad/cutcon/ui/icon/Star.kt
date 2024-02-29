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
                moveToRelative(12.0f, 4.0f)
                lineToRelative(2.3511f, 4.7639f)
                lineToRelative(5.2573f, 0.7639f)
                lineToRelative(-3.8042f, 3.7082f)
                lineToRelative(0.8981f, 5.2361f)
                lineTo(12.0f, 16.0f)
                lineTo(7.2977f, 18.4721f)
                lineTo(8.1958f, 13.2361f)
                lineTo(4.3915f, 9.5279f)
                lineTo(9.6489f, 8.7639f)
                close()
            }
        }
        return icon!!
    }
