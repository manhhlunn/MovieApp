package com.android.movieapp.ui.player

import android.content.Context
import android.content.pm.ActivityInfo
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.C.TRACK_TYPE_AUDIO
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.C.TRACK_TYPE_VIDEO
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.android.movieapp.R
import com.android.movieapp.models.network.Category
import com.android.movieapp.models.network.Episode
import com.android.movieapp.ui.ext.ProgressiveGlowingImage
import com.android.movieapp.ui.ext.makeTimeString
import com.android.movieapp.ui.ext.roundOffDecimal
import com.android.movieapp.ui.ext.setScreenOrientation
import kotlinx.coroutines.delay

@Composable
fun SelectItemRow(title: String, selected: Boolean, onChange: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (selected) Modifier.background(Color.DarkGray) else Modifier)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onChange.invoke() }
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp
            ),
            color = Color.White,
        )
    }
}

@OptIn(UnstableApi::class)
@Composable
fun CustomPlayerView(
    context: Context,
    modifier: Modifier,
    exoPlayer: ExoPlayer,
    fullScreen: Boolean,
    mediaState: MediaState,
    duration: Long,
    isMultipleServer: Boolean,
    subtitleOffset: Long,
    isInPipMode: Boolean,
    onServerChange: () -> Unit,
    onQualityChange: () -> Unit,
    onSubtitleChange: () -> Unit,
    onAudioChange: () -> Unit,
    onNextEpisode: () -> Unit,
    onNewSubtitleOffset: (Long) -> Unit
) {
    var currentPosition by remember { mutableLongStateOf(0) }
    var controllerShowTime by remember { mutableLongStateOf(2000L) }

    var isSettingsEnabled by remember { mutableStateOf(false) }
    var resizeMode by remember { mutableStateOf(ResizeMode.RESIZE_MODE_FIT) }
    var speed by remember { mutableFloatStateOf(1f) }
    var scale by remember { mutableFloatStateOf(1f) }

    val isShowController = if (isInPipMode) false else controllerShowTime > 0
            || controllerShowTime == -1L
            || (mediaState as? MediaState.Playing)?.isPlay != true
            || isSettingsEnabled

    LaunchedEffect(key1 = controllerShowTime) {
        if (controllerShowTime > 0) {
            delay(100L)
            controllerShowTime -= 100L
        }
        val exoPlayerPosition = exoPlayer.currentPosition
        if (exoPlayerPosition > 0 && controllerShowTime != -1L) currentPosition = exoPlayerPosition
    }

    Box {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    this.player = exoPlayer
                    this.useController = false
                    this.resizeMode = resizeMode.value
                }
            },
            update = {
                it.resizeMode = resizeMode.value
            },
            modifier = modifier
                .background(Color.Black)
                .scale(scale)
        )

        Box(
            modifier = modifier
                .background(if (isShowController) Color(0x33000000) else Color.Transparent)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = {
                            controllerShowTime = 2000L
                        },
                        onDoubleTap = {
                            exoPlayer.seekForward()
                        },
                        onLongPress = {
                            speed = if (speed == 1f) 2f else 1f
                        }
                    )
                }
        ) {
            if (fullScreen && isShowController) Column(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .then(
                        if (isSettingsEnabled) Modifier
                            .fillMaxHeight()
                            .background(Color(0x33000000)) else Modifier
                    )
            ) {
                IconButton(
                    onClick = {
                        isSettingsEnabled = !isSettingsEnabled
                    }
                ) {
                    Icon(
                        painterResource(id = R.drawable.baseline_settings_24),
                        contentDescription = "Settings",
                        tint = Color.White
                    )
                }

                if (isSettingsEnabled) Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            scale -= 0.02f
                        }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = "Zoom -",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = stringResource(
                            R.string.zoom_percent,
                            (scale * 100.0).roundOffDecimal()
                        ),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        maxLines = 1,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            scale += 0.02f
                        }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                            contentDescription = "Zoom +",
                            tint = Color.White
                        )
                    }
                }

                if (isSettingsEnabled) Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            resizeMode = resizeMode.prevMode()
                        }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = "ResizeMode -",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = resizeMode.getStringValue(),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        maxLines = 1,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            resizeMode = resizeMode.nextMode()
                        }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                            contentDescription = "ResizeMode +",
                            tint = Color.White
                        )
                    }
                }

                if (isSettingsEnabled) Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            speed -= 0.1f
                            exoPlayer.setPlaybackSpeed(speed)
                        }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = "Speed -",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = stringResource(R.string.speed_x, speed.toDouble().roundOffDecimal()),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        maxLines = 1,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            speed += 0.1f
                            exoPlayer.setPlaybackSpeed(speed)
                        }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                            contentDescription = "Speed +",
                            tint = Color.White
                        )
                    }
                }

                if (isSettingsEnabled) Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = {
                            onNewSubtitleOffset.invoke(subtitleOffset - 250L)
                        }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_arrow_back_ios_24),
                            contentDescription = "Offset -",
                            tint = Color.White
                        )
                    }

                    Text(
                        text = stringResource(
                            R.string.offset_s,
                            (subtitleOffset / 1000.0).roundOffDecimal()
                        ),
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        maxLines = 1,
                        color = Color.White
                    )

                    IconButton(
                        onClick = {
                            onNewSubtitleOffset.invoke(subtitleOffset + 250L)
                        }
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_arrow_forward_ios_24),
                            contentDescription = "Offset +",
                            tint = Color.White
                        )
                    }
                }
            }

            if (fullScreen && isShowController) Box(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                IconButton(onClick = {
                    controllerShowTime = 2000L
                    onNextEpisode.invoke()
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_skip_next_24),
                        contentDescription = "Next episode",
                        tint = Color.White
                    )
                }
            }



            if (isShowController) Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter),
            ) {
                Spacer(modifier = Modifier.width(12.dp))

                Text(
                    text = currentPosition.makeTimeString(),
                    fontWeight = FontWeight.Normal,
                    fontSize = 12.sp,
                    maxLines = 1,
                    color = Color.White
                )

                Slider(
                    value = currentPosition.toFloat(),
                    valueRange = 0f..duration.toFloat(),
                    onValueChange = {
                        controllerShowTime = -1L
                        currentPosition = it.toLong()
                        exoPlayer.seekTo(it.toLong())
                    },
                    onValueChangeFinished = {
                        controllerShowTime = 2000L
                    },
                    colors = SliderDefaults.colors(
                        thumbColor = Color.White,
                        activeTrackColor = Color.White,
                        inactiveTrackColor = Color.DarkGray
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .padding(horizontal = 8.dp)
                )

                Text(
                    text = duration.makeTimeString(),
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    maxLines = 1,
                    color = Color.White
                )

                if (exoPlayer.isEnableSelect(TRACK_TYPE_AUDIO)) {
                    IconButton(
                        onClick = {
                            controllerShowTime = 2000L
                            onAudioChange.invoke()
                        }, modifier = Modifier
                            .size(20.dp)
                            .padding(start = 6.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_audiotrack_24),
                            contentDescription = "Audio",
                            tint = Color.White
                        )
                    }
                }

                if (exoPlayer.isEnableSelect(TRACK_TYPE_TEXT)) {
                    IconButton(
                        onClick = {
                            controllerShowTime = 2000L
                            onSubtitleChange.invoke()
                        }, modifier = Modifier
                            .size(20.dp)
                            .padding(start = 6.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_subtitles_24),
                            contentDescription = "Subtitles",
                            tint = Color.White
                        )
                    }
                }

                if (exoPlayer.isEnableSelect(TRACK_TYPE_VIDEO)) {
                    IconButton(
                        onClick = {
                            controllerShowTime = 2000L
                            onQualityChange.invoke()
                        }, modifier = Modifier
                            .size(20.dp)
                            .padding(start = 6.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_hd_24),
                            contentDescription = "Quality",
                            tint = Color.White
                        )
                    }
                }

                if (isMultipleServer) {
                    IconButton(
                        onClick = {
                            controllerShowTime = 2000L
                            onServerChange.invoke()
                        }, modifier = Modifier
                            .size(20.dp)
                            .padding(start = 6.dp)
                    ) {
                        Icon(
                            painterResource(id = R.drawable.baseline_cloud_24),
                            contentDescription = "Server",
                            tint = Color.White
                        )
                    }
                }

                IconButton(
                    onClick = {
                        controllerShowTime = 2000L
                        context.setScreenOrientation(
                            if (fullScreen) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                            else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                        )
                    }
                ) {
                    Icon(
                        painterResource(id = if (fullScreen) R.drawable.baseline_fullscreen_exit_24 else R.drawable.baseline_fullscreen_24),
                        contentDescription = "Full Screen",
                        tint = Color.White
                    )
                }
            }

            if (isShowController) Row(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    controllerShowTime = 2000L
                    exoPlayer.seekBack()
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_replay_10_24),
                        contentDescription = "Seek back",
                        tint = Color.White,
                        modifier = Modifier.scale(1.8f)
                    )
                }

                IconButton(
                    onClick = {
                        controllerShowTime = 2000L
                        if ((mediaState as MediaState.Playing).isPlay) exoPlayer.pause()
                        else exoPlayer.play()
                    },
                    enabled = mediaState is MediaState.Playing
                ) {
                    when (mediaState) {
                        is MediaState.Error -> Icon(
                            painterResource(R.drawable.baseline_error_24),
                            contentDescription = "Error",
                            tint = Color.Red,
                            modifier = Modifier.scale(2f)
                        )

                        is MediaState.Playing -> Icon(
                            painterResource(if (mediaState.isPlay) R.drawable.baseline_pause_24 else R.drawable.baseline_play_arrow_24),
                            contentDescription = "PlayPause",
                            tint = Color.White,
                            modifier = Modifier.scale(2f)
                        )

                        else -> CircularProgressIndicator(color = Color.White)
                    }
                }

                IconButton(onClick = {
                    controllerShowTime = 2000L
                    exoPlayer.seekForward()
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_forward_10_24),
                        contentDescription = "Seek forward",
                        tint = Color.White,
                        modifier = Modifier.scale(1.8f)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryChips(
    categories: List<Category>,
    modifier: Modifier
) {
    Row(
        modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        categories.forEachIndexed { index, category ->
            Text(
                text = category.name ?: "",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 0f),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier
                    .background(MaterialTheme.colorScheme.onSecondary, RoundedCornerShape(50))
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            )

            if (index != categories.lastIndex) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}


@Composable
fun EpisodeItem(episode: Episode, isPlaying: Boolean = false, onChange: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (isPlaying) Modifier.background(Color.DarkGray) else Modifier)
            .padding(horizontal = 16.dp, vertical = 6.dp)
            .clickable { onChange.invoke() },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Box(modifier = Modifier.weight(3f)) {
            ProgressiveGlowingImage(
                episode.thumbs ?: "",
                true,
                failRatio = 16 / 9f
            )
        }

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(7f)) {
            Text(
                text = "S${episode.season}E${episode.episode} - ${episode.title} (${episode.runtime}m)",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                ),
                color = Color.White,
            )

            Text(
                text = episode.synopsis ?: "",
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 12.sp,
                    lineHeight = 12.sp
                ),
                color = Color.LightGray
            )
        }
    }
}

typealias TrackValue = Triple<Int, Int, String>
typealias BottomSheetCallback = (TrackValue) -> Unit
typealias BottomSheetValues = List<TrackValue>
typealias BottomSheetState = Triple<TrackValue?, BottomSheetValues, BottomSheetCallback>

@kotlin.OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BottomSheetSelectTracks(bottomSheetState: BottomSheetState?, dismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    if (bottomSheetState != null) {
        ModalBottomSheet(
            onDismissRequest = dismiss,
            sheetState = sheetState,
            containerColor = Color.Black,
            tonalElevation = 12.dp
        ) {
            Column(
                Modifier
                    .padding(vertical = 12.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                bottomSheetState.second.forEach { item ->
                    SelectItemRow(
                        title = item.third,
                        selected = item == bottomSheetState.first,
                        onChange = {
                            bottomSheetState.third.invoke(item)
                        }
                    )
                }
            }
        }
    }
}