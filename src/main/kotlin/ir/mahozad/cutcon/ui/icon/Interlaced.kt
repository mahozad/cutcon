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
        imageVector = Icons.Custom.Interlaced,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Interlaced: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Interlaced") {
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(3.0f, 3.0f)
                verticalLineTo(21.0f)
                horizontalLineTo(21.0f)
                verticalLineTo(3.0f)
                close()
                moveTo(11.998f, 5.0f)
                horizontalLineTo(19.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-7.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(7.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(12.002f)
                verticalLineTo(9.0f)
                horizontalLineTo(19.0f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(12.002f)
                verticalLineTo(11.0f)
                horizontalLineTo(19.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-6.998f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(19.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-6.998f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(19.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-6.998f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(19.0f)
                verticalLineToRelative(1.0f)
                horizontalLineToRelative(-6.998f)
                verticalLineToRelative(1.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(18.0f)
                horizontalLineTo(11.998f)
                verticalLineTo(17.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(16.0f)
                horizontalLineTo(11.998f)
                verticalLineTo(15.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(14.0f)
                horizontalLineTo(11.998f)
                verticalLineTo(13.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(12.0f)
                horizontalLineTo(11.998f)
                verticalLineTo(11.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(10.0f)
                horizontalLineToRelative(6.998f)
                verticalLineToRelative(-1.0f)
                horizontalLineTo(5.0f)
                verticalLineTo(8.0f)
                horizontalLineTo(11.998f)
                verticalLineTo(7.0f)
                horizontalLineTo(4.998f)
                verticalLineTo(6.0f)
                horizontalLineToRelative(7.0f)
                close()
            }
        }
        return icon!!
    }
