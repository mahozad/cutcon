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
        imageVector = Icons.Custom.LoopOn,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.LoopOn: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.LoopOn") {
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(12.0f, 1.0f)
                arcTo(11.0f, 11.0f, 0.0f, false, false, 1.0f, 12.0f)
                arcTo(11.0f, 11.0f, 0.0f, false, false, 12.0f, 23.0f)
                arcTo(11.0f, 11.0f, 0.0f, false, false, 23.0f, 12.0f)
                arcTo(11.0f, 11.0f, 0.0f, false, false, 12.0f, 1.0f)
                close()
                moveTo(12.0f, 2.0f)
                lineTo(16.0254f, 5.5f)
                lineTo(12.0f, 9.0f)
                lineTo(12.0f, 6.5f)
                curveTo(10.0334f, 6.5f, 8.2196f, 7.5469f, 7.2363f, 9.25f)
                curveTo(6.253f, 10.9531f, 6.253f, 13.0469f, 7.2363f, 14.75f)
                lineTo(5.5039f, 15.75f)
                curveTo(4.165f, 13.4309f, 4.165f, 10.5691f, 5.5039f, 8.25f)
                curveTo(6.8428f, 5.9309f, 9.3221f, 4.5f, 12.0f, 4.5f)
                lineTo(12.0f, 2.0f)
                close()
                moveTo(18.4961f, 8.25f)
                curveTo(19.835f, 10.5691f, 19.835f, 13.4309f, 18.4961f, 15.75f)
                curveTo(17.1572f, 18.0691f, 14.6779f, 19.5f, 12.0f, 19.5f)
                lineTo(12.0f, 22.0f)
                lineTo(7.9746f, 18.5f)
                lineTo(12.0f, 15.0f)
                lineTo(12.0f, 17.5f)
                curveTo(13.9666f, 17.5f, 15.7804f, 16.4531f, 16.7637f, 14.75f)
                curveTo(17.747f, 13.0469f, 17.747f, 10.9531f, 16.7637f, 9.25f)
                lineTo(18.4961f, 8.25f)
                close()
            }
        }
        return icon!!
    }
