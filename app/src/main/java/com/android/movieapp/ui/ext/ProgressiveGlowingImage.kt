package com.android.movieapp.ui.ext

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.rounded.AccountCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Brush.Companion.linearGradient
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.android.movieapp.ui.theme.Black30

@Composable
fun CircleGlowingImage(
    url: String,
    glow: Boolean
) {
    val glowColor by remember { mutableStateOf(Color.White) }
    val animatedGlowColor by animateColorAsState(
        targetValue = glowColor,
        animationSpec = tween(1500), label = ""
    )
    if (glow) {
        Box(
            Modifier
                .glow(
                    color = animatedGlowColor,
                    radius = 40.dp,
                    alpha = 0.2f,
                    offsetY = 12.dp
                )
        ) {
            CircleGlowingImage(url)
        }
    } else {
        CircleGlowingImage(url)
    }
}

@Composable
fun ProgressiveGlowingImage(
    url: String,
    glow: Boolean,
    failRatio: Float = 0.6666667f,
) {
    val glowColor by remember { mutableStateOf(Color.White) }
    val animatedGlowColor by animateColorAsState(
        targetValue = glowColor,
        animationSpec = tween(1500), label = ""
    )
    if (glow) {
        Box(
            Modifier
                .glow(
                    color = animatedGlowColor,
                    radius = 15.dp,
                    alpha = 0.3f,
                    offsetY = 12.dp
                )
        ) {
            ProgressiveGlowingImage(url, failRatio)
        }
    } else {
        ProgressiveGlowingImage(url, failRatio)
    }
}


@Composable
fun CircleGlowingImage(
    url: String,
) {
    var isSuccess by remember { mutableStateOf<Boolean?>(null) }
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .allowHardware(true)
            .size(Size.ORIGINAL)
            .build(),
        error = rememberVectorPainter(image = Icons.Rounded.AccountCircle),
        onSuccess = { _ ->
            isSuccess = true
        },
        onError = {
            isSuccess = false
        },
    )

    val imageModifier = Modifier
        .fillMaxWidth()
        .aspectRatio(1f)
        .then(
            when (isSuccess) {
                true -> Modifier.shadow(4.dp, CircleShape)
                else -> Modifier
            }
        )

    Box {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = imageModifier
        )

        if (isSuccess == null) {
            LoadingShimmerEffect { brush ->
                Spacer(
                    modifier = imageModifier
                        .background(brush)
                )
            }
        }
    }
}

@Composable
fun ProgressiveGlowingImage(
    url: String,
    failRatio: Float
) {
    var showShimmer by remember { mutableStateOf(true) }
    val painter = rememberAsyncImagePainter(
        model = ImageRequest.Builder(LocalContext.current)
            .data(url)
            .crossfade(true)
            .build(),
        error = ColorPainter(MaterialTheme.colorScheme.onSecondary),
        onSuccess = { _ ->
            showShimmer = false
        },
        onError = {
            showShimmer = false
        },
    )

    val imageModifier: Modifier = Modifier
        .fillMaxWidth()
        .aspectRatio(failRatio)
        .then(
            if (painter.state is AsyncImagePainter.State.Success) Modifier.shadow(
                4.dp,
                RoundedCornerShape(6.dp)
            )
            else Modifier
        )

    Box {
        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = imageModifier
        )

        if (showShimmer) {
            LoadingShimmerEffect { brush ->
                Spacer(
                    modifier = imageModifier
                        .background(brush)
                )
            }
        }
    }
}

fun Modifier.glow(
    color: Color,
    alpha: Float = 0.2f,
    borderRadius: Dp = 0.dp,
    radius: Dp = 20.dp,
    offsetY: Dp = 0.dp,
    offsetX: Dp = 0.dp
) = this.drawBehind {
    val transparentColor = Color.Transparent.toArgb()
    val shadowColor = color.copy(alpha).toArgb()
    this.drawIntoCanvas {
        val paint = Paint()
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.color = transparentColor
        frameworkPaint.setShadowLayer(
            radius.toPx(),
            offsetX.toPx(),
            offsetY.toPx(),
            shadowColor
        )
        it.drawRoundRect(
            0f,
            0f,
            this.size.width,
            this.size.height,
            borderRadius.toPx(),
            borderRadius.toPx(),
            paint
        )
    }
}

@Composable
fun LoadingShimmerEffect(content: @Composable (brush: Brush) -> Unit) {

    val gradient = listOf(
        Color.LightGray.copy(alpha = 0.9f),
        Color.LightGray.copy(alpha = 0.3f),
        Color.LightGray.copy(alpha = 0.9f)
    )

    val transition = rememberInfiniteTransition(label = "")

    val translateAnimation = transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1000,
                easing = FastOutLinearInEasing
            )
        ), label = ""
    )
    val brush = linearGradient(
        colors = gradient,
        start = Offset(200f, 200f),
        end = Offset(
            x = translateAnimation.value,
            y = translateAnimation.value
        )
    )
    content(brush)
}


@Composable
fun ZoomableImage(imageUrl: String, navController: NavController) {

    var scale by remember { mutableFloatStateOf(1f) }
    var offsetX by remember { mutableFloatStateOf(0f) }
    var offsetY by remember { mutableFloatStateOf(0f) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Black30)
    ) {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown()
                        do {
                            val event = awaitPointerEvent()
                            scale *= event.calculateZoom()
                            val offset = event.calculatePan()
                            offsetX += offset.x
                            offsetY += offset.y
                        } while (event.changes.any { it.pressed })
                    }
                }
        ) {
            val painter = rememberAsyncImagePainter(
                model = ImageRequest.Builder(LocalContext.current)
                    .data(imageUrl)
                    .size(Size.ORIGINAL)
                    .build()
            )

            Image(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer(
                        scaleX = scale,
                        scaleY = scale,
                        translationX = offsetX,
                        translationY = offsetY
                    ),
                painter = painter,
                contentDescription = "Preview image"
            )
        }
        IconButton(
            onClick = { navController.popBackStack() },
            modifier = Modifier
                .statusBarsPadding()
                .padding(8.dp)
                .background(MaterialTheme.colorScheme.primary, CircleShape)
                .size(36.dp)
                .align(
                    Alignment.TopEnd
                )
        ) {
            Icon(
                painter = rememberVectorPainter(image = Icons.Outlined.Close),
                "Close",
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }
    }

}