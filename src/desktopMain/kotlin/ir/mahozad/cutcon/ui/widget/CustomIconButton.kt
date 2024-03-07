package ir.mahozad.cutcon.ui.widget

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.defaultIconSize

@Composable
fun CustomIcon(
    icon: ImageVector,
    description: String,
    tint: Color? = null
) {
    if (tint == null) {
        Icon(
            imageVector = icon,
            contentDescription = description,
            modifier = Modifier
                .padding(12.dp)
                .size(defaultIconSize)
        )
    } else {
        Icon(
            imageVector = icon,
            tint = tint,
            contentDescription = description,
            modifier = Modifier
                .padding(12.dp)
                .size(defaultIconSize)
        )
    }
}
