package ir.mahozad.cutcon.ui.dialog

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutSine
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import ir.mahozad.cutcon.defaultFontSize
import ir.mahozad.cutcon.ui.DialogDecoration
import kotlinx.coroutines.delay
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

private val displayAnimationDuration = 200.milliseconds
private val dismissAnimationDuration = Duration.ZERO

@Composable
fun AnimatedDialog(
    title: String,
    modifier: Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable () -> Unit
) {
    // See https://medium.com/bilue/expanding-dialog-in-jetpack-compose-a6be40deab86
    var isDialogDisplayed by remember { mutableStateOf(false) }
    var dismissTrigger by remember { mutableStateOf(false) }
    LaunchedEffect(key1 = Unit) { isDialogDisplayed = true }
    LaunchedEffect(key1 = dismissTrigger) {
        if (dismissTrigger) {
            isDialogDisplayed = false
            delay(dismissAnimationDuration)
            onDismissRequest()
        }
    }
    Dialog(/* Called when clicking outside the dialog: */ onDismissRequest = { dismissTrigger = true }) {
        AnimatedVisibility(
            visible = isDialogDisplayed,
            enter = scaleIn(tween(displayAnimationDuration.inWholeMilliseconds.toInt(), easing = EaseOutSine), initialScale = 0.4f),
            exit = fadeOut(tween(dismissAnimationDuration.inWholeMilliseconds.toInt(), easing = EaseOutSine))
        ) {
            DialogDecoration(
                icon = painterResource("logo.svg"),
                title = { Text(text = title, fontSize = (defaultFontSize.value - 1).sp) },
                modifier = modifier,
                onCloseRequest = { dismissTrigger = true }
            ) {
                content()
            }
        }
    }
}
