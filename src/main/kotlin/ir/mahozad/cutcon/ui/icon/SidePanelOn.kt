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
        imageVector = Icons.Custom.SidePanelOn,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.SidePanelOn: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.SidePanelOn") {
            path(
                name = "main",
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2f
            ) {
                moveTo(16f, 20f)
                verticalLineToRelative(-16f)
                horizontalLineToRelative(-13f)
                verticalLineToRelative(16f)
                close()
            }
            path(
                name = "side",
                fill = SolidColor(defaultIconColor),
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2f
            ) {
                moveTo(21f, 20f)
                verticalLineToRelative(-16f)
                horizontalLineToRelative(-5f)
                verticalLineToRelative(16f)
                close()
            }
        }
        return icon!!
    }
