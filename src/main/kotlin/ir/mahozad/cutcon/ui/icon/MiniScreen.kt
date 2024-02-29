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
        imageVector = Icons.Custom.MiniScreen,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.MiniScreen: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.MiniScreen") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(9.0f, 20.0f)
                horizontalLineTo(7.0f)
                moveTo(4.0f, 21.0f)
                verticalLineTo(19.0f)
                moveTo(4.0f, 17.0f)
                verticalLineTo(15.0f)
                moveTo(4.0f, 13.0f)
                verticalLineTo(11.0f)
                moveTo(4.0f, 9.0f)
                verticalLineTo(7.0f)
                moveTo(3.0f, 4.0f)
                horizontalLineToRelative(2.0f)
                moveToRelative(2.0f, 0.0f)
                horizontalLineToRelative(2.0f)
                moveToRelative(2.0f, 0.0f)
                horizontalLineToRelative(2.0f)
                moveToRelative(2.0f, 0.0f)
                horizontalLineToRelative(2.0f)
                moveToRelative(2.0f, 0.0f)
                horizontalLineToRelative(2.0f)
                moveToRelative(-1.0f, 3.0f)
                verticalLineToRelative(2.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(20.0f, 20.0f)
                verticalLineToRelative(-8.0f)
                horizontalLineToRelative(-8.0f)
                verticalLineToRelative(8.0f)
                close()
            }
        }
        return icon!!
    }
