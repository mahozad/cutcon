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
        imageVector = Icons.Custom.Settings,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.Settings: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.Settings") {
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(12.0f, 12.0f)
                moveToRelative(-2.0f, 0.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, 4.0f, 0.0f)
                arcToRelative(2.0f, 2.0f, 0.0f, true, true, -4.0f, 0.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(14.0f, 18.0f)
                verticalLineToRelative(3.0f)
                horizontalLineToRelative(-4.0f)
                verticalLineToRelative(-3.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(18.1962f, 13.268f)
                lineToRelative(2.5981f, 1.5f)
                lineToRelative(-2.0f, 3.4641f)
                lineToRelative(-2.5981f, -1.5f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(16.1962f, 7.268f)
                lineToRelative(2.5981f, -1.5f)
                lineToRelative(2.0f, 3.4641f)
                lineToRelative(-2.5981f, 1.5f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveTo(10.0f, 6.0f)
                verticalLineTo(3.0f)
                horizontalLineToRelative(4.0f)
                verticalLineToRelative(3.0f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(5.8038f, 10.732f)
                lineToRelative(-2.5981f, -1.5f)
                lineToRelative(2.0f, -3.4641f)
                lineToRelative(2.5981f, 1.5f)
            }
            path(
                fill = null,
                stroke = SolidColor(defaultIconColor),
                strokeLineWidth = 2.0f
            ) {
                moveToRelative(7.8038f, 16.7321f)
                lineToRelative(-2.5981f, 1.5f)
                lineToRelative(-2.0f, -3.4641f)
                lineToRelative(2.5981f, -1.5f)
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveToRelative(13.0f, 5.0723f)
                verticalLineToRelative(2.0293f)
                curveToRelative(1.0616f, 0.2165f, 2.0261f, 0.7706f, 2.7441f, 1.582f)
                lineTo(17.5f, 7.6719f)
                curveTo(16.3897f, 6.2614f, 14.7767f, 5.3288f, 13.0f, 5.0723f)
                close()
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveToRelative(18.4996f, 9.4022f)
                lineToRelative(-1.7574f, 1.0146f)
                curveToRelative(0.3434f, 1.0276f, 0.3457f, 2.14f, 0.002f, 3.1675f)
                lineToRelative(1.7541f, 1.0148f)
                curveToRelative(0.6664f, -1.6668f, 0.6675f, -3.53f, 0.001f, -5.1969f)
                close()
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveToRelative(17.4996f, 16.3299f)
                lineToRelative(-1.7574f, -1.0146f)
                curveToRelative(-0.7182f, 0.8112f, -1.6804f, 1.3694f, -2.7422f, 1.5855f)
                lineToRelative(-0.002f, 2.0265f)
                curveToRelative(1.7767f, -0.2563f, 3.3909f, -1.1869f, 4.5013f, -2.5973f)
                close()
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(11.0f, 18.9277f)
                lineTo(11.0f, 16.8984f)
                curveTo(9.9384f, 16.682f, 8.9739f, 16.1278f, 8.2559f, 15.3164f)
                lineTo(6.5f, 16.3281f)
                curveTo(7.6103f, 17.7386f, 9.2233f, 18.6712f, 11.0f, 18.9277f)
                close()
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveToRelative(5.5004f, 14.5978f)
                lineToRelative(1.7574f, -1.0146f)
                curveToRelative(-0.3433f, -1.0276f, -0.3457f, -2.14f, -0.002f, -3.1675f)
                lineTo(5.5017f, 9.4009f)
                curveToRelative(-0.6664f, 1.6668f, -0.6676f, 3.53f, -0.001f, 5.1969f)
                close()
            }
            path(fill = SolidColor(defaultIconColor)) {
                moveTo(6.5004f, 7.6701f)
                lineTo(8.2578f, 8.6847f)
                curveTo(8.9761f, 7.8736f, 9.9383f, 7.3154f, 11.0f, 7.0993f)
                lineToRelative(0.002f, -2.0265f)
                curveTo(9.2253f, 5.3291f, 7.6111f, 6.2597f, 6.5007f, 7.6701f)
                close()
            }
        }
        return icon!!
    }
