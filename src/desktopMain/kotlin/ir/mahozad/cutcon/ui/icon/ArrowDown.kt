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
        imageVector = Icons.Custom.ArrowDown,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.ArrowDown: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.ArrowDown") {
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(2.0f, 8.0f)
                horizontalLineTo(22f)
                lineTo(12f, 18f)
                close()
            }
        }
        return icon!!
    }
