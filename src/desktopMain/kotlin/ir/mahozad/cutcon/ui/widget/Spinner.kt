package ir.mahozad.cutcon.ui.widget

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.LocalContentColor
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun Spinner(modifier: Modifier = Modifier) {
    CircularProgressIndicator(
        color = LocalContentColor.current.copy(alpha = 0.8f),
        modifier = modifier,
        strokeWidth = 2.dp
    )
}
