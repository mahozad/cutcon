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
        imageVector = Icons.Custom.PinOn,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.PinOn: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.PinOn") {
            path(fill = SolidColor(defaultIconColor)) {
                moveToRelative(13.0f, 16.0f)
                horizontalLineToRelative(6.0f)
                lineTo(17.0f, 14.0f)
                verticalLineTo(5.0f)
                lineTo(19.0f, 3.0f)
                horizontalLineTo(5.0f)
                lineToRelative(2.0f, 2.0f)
                verticalLineToRelative(9.0f)
                lineToRelative(-2.0f, 2.0f)
                horizontalLineToRelative(6.0f)
                verticalLineToRelative(6.0f)
                horizontalLineToRelative(2.0f)
            }
        }
        return icon!!
    }
