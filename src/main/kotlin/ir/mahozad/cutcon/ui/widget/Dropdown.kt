package ir.mahozad.cutcon.ui.widget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.defaultInputHeight
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.ui.icon.ArrowDown
import ir.mahozad.cutcon.ui.icon.Icons

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun CustomExposedDropDown(
    label: @Composable BoxScope.() -> Unit,
    items: List<@Composable RowScope.() -> Unit>,
    isEnabled: Boolean,
    modifier: Modifier = Modifier,
    startRoundness: Dp,
    endRoundness: Dp,
    onChange: (Int) -> Unit,
) {
    var isExpanded by remember { mutableStateOf(false) }
    val iconRotation by animateFloatAsState(if (isExpanded) 180f else 0f)

    ExposedDropdownMenuBox(
        expanded = isExpanded,
        modifier = modifier.width(IntrinsicSize.Min),
        onExpandedChange = { if (isEnabled) isExpanded = it }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = defaultInputHeight)
                .clip(
                    RoundedCornerShape(
                        topStart = startRoundness,
                        bottomStart = startRoundness,
                        topEnd = endRoundness,
                        bottomEnd = endRoundness
                    )
                )
                .background(color = MaterialTheme.colors.onSurface.copy(alpha = TextFieldDefaults.BackgroundOpacity))
                .clickable(enabled = isEnabled, onClick = {})
        ) {
            label()
            Icon(
                imageVector = Icons.Custom.ArrowDown,
                contentDescription = Messages.ICO_DSC_OPEN_DROPDOWN,
                tint = if (isEnabled) {
                    MaterialTheme.colors.primary
                } else {
                    MaterialTheme.colors.onSurface.copy(alpha = ContentAlpha.disabled)
                },
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 12.dp)
                    .size(12.dp)
                    .rotate(iconRotation)
            )
        }
        if (isEnabled) {
            ExposedDropdownMenu(
                expanded = isExpanded,
                onDismissRequest = { isExpanded = false },
            ) {
                items.forEachIndexed { i, item ->
                    DropdownMenuItem(
                        content = item,
                        onClick = {
                            onChange(i)
                            isExpanded = false
                        }
                    )
                }
            }
        }
    }
}
