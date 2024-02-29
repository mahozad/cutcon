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
        imageVector = Icons.Custom.Folder,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Folder: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Folder") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(3.0f, 4.0f)
                verticalLineToRelative(15.0f)
                horizontalLineToRelative(18.0f)
                verticalLineToRelative(-13.0f)
                horizontalLineToRelative(-9.0f)
                lineToRelative(-2.0f, -2.0f)
                close()
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveToRelative(3.0f, 7.0f)
                horizontalLineToRelative(9.0f)
                verticalLineToRelative(-2.0f)
                lineToRelative(-2.0f, -1.0f)
                lineTo(3.0f, 4.0f)
                close()
            }
        }
        return icon!!
    }
