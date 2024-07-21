package ir.mahozad.cutcon.ui.widget

import androidx.compose.animation.core.spring
import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.SliderDefaults
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.component.DefaultMediaPlayer
import ir.mahozad.cutcon.model.Progress
import ir.mahozad.cutcon.ui.theme.AppTheme
import ir.mahozad.multiplatform.wavyslider.material.WavySlider
import ir.mahozad.multiplatform.wavyslider.material3.WaveAnimationSpecs
import kotlinx.coroutines.delay
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Preview
@Composable
private fun MediaPlayerProgressPreview() {
    AppTheme {
        val progress = Progress(0.27f, 100.seconds)
        MediaPlayerProgress(progress = progress, isWavy = true, onSeek = {})
    }
}

/**
 * The higher the value of this, the better as higher values are more safe to ensure no glitch
 * (with the compromise that when seeking is finished i.e. mouse key is released,
 * it may take longer for the first new media progress to start showing on the bar
 * because *the internal seek variable* would still be showing on the bar)
 * The number below is also related to the frequency of media player progress emission
 * (see [DefaultMediaPlayer.progressFlow] function)
 */
private const val PROGRESS_UPDATE_DELAY = 1_000

/**
 * See the section about progress and seeking in main README for more information.
 */
@Composable
fun MediaPlayerProgress(
    progress: Progress,
    isWavy: Boolean,
    onSeek: (Float) -> Unit
) {
    // Fixes the following problem:
    // when dragging the slider thumb and then releasing it,
    // for a moment the previous position of the thumb was shown
    var lastSeekAttempt by remember { mutableLongStateOf(System.currentTimeMillis()) }
    // See https://stackoverflow.com/q/66386039
    var isSeeking by remember { mutableStateOf(false) }
    var seek by remember { mutableFloatStateOf(progress.fraction) }
    var time by remember { mutableStateOf(progress.time) }
    LaunchedEffect(isSeeking, lastSeekAttempt, progress.time) {
        while (isSeeking || (System.currentTimeMillis() - lastSeekAttempt) < PROGRESS_UPDATE_DELAY) {
            time = (progress.length.inWholeMilliseconds * seek).toDouble().milliseconds
            delay(30.milliseconds)
        }
        time = progress.time
    }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // The width of the timestamp components is fixed (constant).
        // This is to prevent their width to change when their text changes (because the font is not monospace).
        // If, instead, the dynamic width were used (i.e. not specifying a constant width == the default wrap size),
        // a change in timestamps text (and hence their component width)
        // caused the width of the slider to change as well by a tiny amount
        // because the slider is set to take the remaining width of the parent.
        // This caused problem when dragging the slider thumb with mouse
        // (could not drag the thumb continuously, as it got stuck and did not change anymore).
        // Could also have used a monospace font and then this wouldn't be required anymore.
        Timestamp(time = time, textAlign = TextAlign.Start)
        WavySlider(
            value = if (isSeeking || (System.currentTimeMillis() - lastSeekAttempt) < PROGRESS_UPDATE_DELAY) {
                seek
            } else {
                progress.fraction
            },
            incremental = true,
            waveLength = 20.dp,
            waveHeight = if (isWavy) 8.dp else 0.dp,
            onValueChange = {
                isSeeking = true
                seek = it
            },
            onValueChangeFinished = {
                lastSeekAttempt = System.currentTimeMillis()
                isSeeking = false
                onSeek(seek)
            },
            animationSpecs = SliderDefaults.WaveAnimationSpecs.copy(
                waveHeightAnimationSpec = spring(dampingRatio = 1f)
            ),
            // See https://stackoverflow.com/q/69688427
            modifier = Modifier
                // The amount of seeking using keyboard arrow keys when the slider has focus is different from
                // our own manual seeking in the viewModel (which takes effect when slider does not have focus).
                // To see this behaviour do this:
                // seek with mouse (which causes the slider to get focus), then hit keyboard left or right arrow keys
                // See the fixed bug https://github.com/JetBrains/compose-multiplatform/issues/3283
                // If this is not the desired behaviour, uncomment the following line.
                // .focusProperties { canFocus = false }
                .weight(1f)
        )
        Timestamp(time = progress.length, textAlign = TextAlign.End)
    }
}
