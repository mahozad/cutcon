package ir.mahozad.cutcon.ui.widget

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import ir.mahozad.cutcon.calculateMaxSizeInFrame
import ir.mahozad.cutcon.localization.Messages
import ir.mahozad.cutcon.model.AspectRatio

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
        TransitionEffect(isTransitioning = image == null)
    }
}

@Composable
private fun TransitionEffect(isTransitioning: Boolean) {
    val heightFactor by animateFloatAsState(if (isTransitioning) 1f else 0f)
    val clipShape = remember(heightFactor) {
        GenericShape { size, _ ->
            addRect(
                Rect(
                    top = 0f,
                    left = 0f,
                    right = size.width,
                    bottom = size.height * heightFactor
                )
            )
        }
    }
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
            .clip(clipShape)
            .background(
                if (MaterialTheme.colors.isLight) {
                    Color.hsv(0f, 0f, 0.88f)
                } else {
                    Color.hsv(0f, 0f, 0.18f)
                }
            )
    ) {
        Image(
            painter = painterResource("logo.svg"),
            contentDescription = Messages.ICO_DSC_LOGO,
            modifier = Modifier.size(128.dp)
        )
    }
}
