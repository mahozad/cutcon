package ir.mahozad.cutcon.ui.icon

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.materialIcon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathNode
import androidx.compose.ui.graphics.vector.group
import androidx.compose.ui.graphics.vector.path
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.defaultIconColor

@Preview
@Composable
private fun IconPreview() {
    Image(
        imageVector = Icons.Custom.CameraFill,
        modifier = Modifier
            .size(256.dp)
            .background(Color.Yellow),
        contentDescription = null
    )
}

private var icon: ImageVector? = null
val Icons.Custom.CameraFill: ImageVector
    get() {
        if (icon != null) {
            return icon!!
        }
        icon = materialIcon(name = "Custom.CameraFill") {
            group(
                clipPathData = listOf(
                    PathNode.MoveTo(0.0f, 0.0f),
                    PathNode.RelativeVerticalTo(24.0f),
                    PathNode.RelativeHorizontalTo(24.0f),
                    PathNode.RelativeVerticalTo(-24.0f),
                    PathNode.RelativeHorizontalTo(-24.0f),
                    PathNode.Close,
                    PathNode.MoveTo(12.0f, 8.0f),
                    PathNode.RelativeCurveTo(2.2f, 0.0f, 4.0f, 1.8f, 4.0f, 4.0f),
                    PathNode.RelativeReflectiveCurveTo(-1.8f, 4.0f, -4.0f, 4.0f),
                    PathNode.RelativeReflectiveCurveTo(-4.0f, -1.8f, -4.0f, -4.0f),
                    PathNode.RelativeReflectiveCurveTo(1.8f, -4.0f, 4.0f, -4.0f),
                    PathNode.Close,
                    PathNode.MoveTo(12.0f, 10.0f),
                    PathNode.RelativeCurveTo(-1.12f, 0.0f, -2.0f, 0.884f, -2.0f, 2.0f),
                    PathNode.RelativeReflectiveCurveTo(0.884f, 2.0f, 2.0f, 2.0f),
                    PathNode.RelativeReflectiveCurveTo(2.0f, -0.884f, 2.0f, -2.0f),
                    PathNode.RelativeReflectiveCurveTo(-0.884f, -2.0f, -2.0f, -2.0f),
                    PathNode.Close
                )
            ) {
                path(
                    fill = SolidColor(defaultIconColor),
                    stroke = SolidColor(defaultIconColor),
                    pathFillType = PathFillType.EvenOdd,
                    strokeLineWidth = 2.0f
                ) {
                    moveToRelative(3.0f, 19.0f)
                    verticalLineToRelative(-13.0f)
                    horizontalLineToRelative(4.0f)
                    lineToRelative(2.0f, -3.0f)
                    horizontalLineToRelative(6.0f)
                    lineToRelative(2.0f, 3.0f)
                    horizontalLineToRelative(4.0f)
                    verticalLineToRelative(13.0f)
                    close()
                }
            }
        }
        return icon!!
    }
