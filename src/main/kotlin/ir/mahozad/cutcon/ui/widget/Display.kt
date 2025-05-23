package ir.mahozad.cutcon.ui.widget

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.GenericShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.calculateMaxSizeInFrame
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.AspectRatio
import ir.mahozad.cutcon.ui.theme.mediaDisplay

@Composable
fun Display(
    image: ImageBitmap?,
    aspectRatio: AspectRatio,
    modifier: Modifier
) {
    var frameSize by remember { mutableStateOf(IntSize.Zero) }
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .onGloballyPositioned { frameSize = it.size }
    ) {
        if (image != null) {
            val imageSize = calculateMaxSizeInFrame(
                frameWidth = frameSize.width.dp,
                frameHeight = frameSize.height.dp,
                displayDensity = LocalDensity.current.density,
                desiredAspectRatio = aspectRatio.ratio ?: (image.width.toFloat() / image.height)
            )
            Image(
                bitmap = image,
                // Clipping is needed here as well for when the image is smaller than the frame
                modifier = Modifier.size(imageSize).clip(RoundedCornerShape(8.dp)),
                alignment = Alignment.Center,
                contentScale = ContentScale.FillBounds,
                filterQuality = FilterQuality.Low,
                contentDescription = Messages.IMG_DSC_DISPLAY_IMAGE
            )
        }
        TransitionEffect2(isTransitioning = image == null)
    }
}

@Composable
private fun TransitionEffect1(isTransitioning: Boolean) {
    val heightFactor by animateFloatAsState(if (isTransitioning) 1f else 0f)
    Transition(0f, heightFactor)
}

@Composable
private fun TransitionEffect2(isTransitioning: Boolean) {
    var top by remember { mutableFloatStateOf(0f) }
    var bottom by remember { mutableFloatStateOf(0f) }
    val topFactor by animateFloatAsState(
        targetValue = top,
        animationSpec = tween(350),
        finishedListener = {
            if (it > 0.5f) {
                top = 0f
                bottom = 0f
            }
        }
    )
    val bottomFactor by animateFloatAsState(
        targetValue = bottom,
        animationSpec = tween(350),
    )
    LaunchedEffect(isTransitioning) {
        if (isTransitioning) {
            bottom = 1f
        } else {
            top = 1f
        }
    }
    Transition(topFactor, bottomFactor)
}

@Composable
private fun Transition(topFactor: Float, bottomFactor: Float) {
    val clipShape = remember(topFactor, bottomFactor) {
        GenericShape { size, _ ->
            addRect(
                Rect(
                    top = size.height * topFactor,
                    left = 0f,
                    right = size.width,
                    bottom = size.height * bottomFactor
                )
            )
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clip(clipShape)
            .background(MaterialTheme.colors.mediaDisplay)
    ) {
        Image(
            painter = painterResource("logo.svg"),
            contentDescription = Messages.ICO_DSC_LOGO,
            modifier = Modifier.size(128.dp)
        )
    }
}
