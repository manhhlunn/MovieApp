package com.android.movieapp.ui.detail

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
import android.content.res.Configuration
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.annotation.MainThread
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
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C
import androidx.media3.common.C.TRACK_TYPE_AUDIO
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.C.TRACK_TYPE_VIDEO
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride
import androidx.media3.common.Tracks
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.MergingMediaSource
import androidx.media3.exoplayer.source.SingleSampleMediaSource
import androidx.media3.ui.DefaultTrackNameProvider
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TrackNameProvider
import androidx.navigation.NavController
import com.android.movieapp.NavScreen
import com.android.movieapp.R
import com.android.movieapp.models.entities.MediaHistory
import com.android.movieapp.models.network.Category
import com.android.movieapp.models.network.Episode
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.models.network.OMovieDetail
import com.android.movieapp.models.network.SearchResultItem
import com.android.movieapp.models.network.SourceLink
import com.android.movieapp.models.network.Subtitle
import com.android.movieapp.models.network.SuperStreamResponse
import com.android.movieapp.repository.MediaRepository
import com.android.movieapp.ui.ext.ProgressiveGlowingImage
import com.android.movieapp.ui.ext.buildExoplayer
import com.android.movieapp.ui.ext.getRenderers
import com.android.movieapp.ui.ext.isEnableSelect
import com.android.movieapp.ui.ext.makeTimeString
import com.android.movieapp.ui.ext.openChromeCustomTab
import com.android.movieapp.ui.ext.roundOffDecimal
import com.android.movieapp.ui.ext.setScreenOrientation
import com.android.movieapp.ui.media.renderer.CustomTextRenderer
import com.android.movieapp.ui.media.util.SSMediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

typealias TrackValue = Triple<Int, Int, String>
typealias BottomSheetCallback = (TrackValue) -> Unit
typealias BottomSheetValues = List<TrackValue>
typealias BottomSheetState = Triple<TrackValue?, BottomSheetValues, BottomSheetCallback>

sealed class MediaState {
    data object Init : MediaState()
    data object Loading : MediaState()
    data class Error(val error: Exception) : MediaState()
    data class Playing(val isPlay: Boolean) : MediaState()
}

enum class ResizeMode(val value: Int) {
    RESIZE_MODE_FIT(0),
    RESIZE_MODE_FIXED_WIDTH(1),
    RESIZE_MODE_FIXED_HEIGHT(2),
    RESIZE_MODE_FILL(3),
    RESIZE_MODE_ZOOM(4);

    @Composable
    fun getStringValue(): String {
        return when (this) {
            RESIZE_MODE_FIT -> stringResource(R.string.fit)
            RESIZE_MODE_FIXED_WIDTH -> stringResource(R.string.fixed_width)
            RESIZE_MODE_FIXED_HEIGHT -> stringResource(R.string.fixed_height)
            RESIZE_MODE_FILL -> stringResource(R.string.fill)
            RESIZE_MODE_ZOOM -> stringResource(R.string.zoom)
        }
    }

    fun nextMode(): ResizeMode {
        return when (this) {
            RESIZE_MODE_FIT -> RESIZE_MODE_FIXED_WIDTH
            RESIZE_MODE_FIXED_WIDTH -> RESIZE_MODE_FIXED_HEIGHT
            RESIZE_MODE_FIXED_HEIGHT -> RESIZE_MODE_FILL
            RESIZE_MODE_FILL -> RESIZE_MODE_ZOOM
            RESIZE_MODE_ZOOM -> RESIZE_MODE_FIT
        }
    }

    fun prevMode(): ResizeMode {
        return when (this) {
            RESIZE_MODE_FIT -> RESIZE_MODE_ZOOM
            RESIZE_MODE_FIXED_WIDTH -> RESIZE_MODE_FIT
            RESIZE_MODE_FIXED_HEIGHT -> RESIZE_MODE_FIXED_WIDTH
            RESIZE_MODE_FILL -> RESIZE_MODE_FIXED_HEIGHT
            RESIZE_MODE_ZOOM -> RESIZE_MODE_FILL
        }
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

    val isShowController =
        controllerShowTime > 0 || controllerShowTime == -1L || (mediaState as? MediaState.Playing)?.isPlay != true || isSettingsEnabled

    LaunchedEffect(key1 = controllerShowTime, key2 = isShowController) {
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

@Composable
fun OMovieDetailScreen(
    navController: NavController,
    viewModel: OMovieDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerIndex by viewModel.playerIndex.collectAsStateWithLifecycle()
    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val serverData by viewModel.serverData.collectAsStateWithLifecycle()
    val serverList by viewModel.serverList.collectAsStateWithLifecycle()
    val qualityTracks by viewModel.qualityTracks.collectAsStateWithLifecycle()
    val audioTracks by viewModel.audioTracks.collectAsStateWithLifecycle()
    val subtitleTracks by viewModel.subtitleTracks.collectAsStateWithLifecycle()
    val subtitleOffset by viewModel.subtitleOffset.collectAsStateWithLifecycle()
    var bottomSheetState by rememberSaveable { mutableStateOf<BottomSheetState?>(null) }

    BottomSheetSelectTracks(
        bottomSheetState = bottomSheetState,
        dismiss = {
            bottomSheetState = null
        }
    )

    val trackCallBack: BottomSheetCallback = { index ->
        viewModel.changeTrack(index.first, index.second)
        bottomSheetState = null
    }

    val context = LocalContext.current
    val configureScreen = LocalConfiguration.current

    val exitFullscreen = {
        context.setScreenOrientation(SCREEN_ORIENTATION_USER_PORTRAIT)
    }

    LaunchedEffect(key1 = context) {
        viewModel.error.collect {
            Toast.makeText(context, it.error.message, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        val movie = uiState
        if (movie != null) {
            when (configureScreen.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    BackHandler {
                        exitFullscreen()
                    }
                    CustomPlayerView(
                        context = context,
                        modifier = Modifier.fillMaxSize(),
                        exoPlayer = viewModel.exoPlayer,
                        fullScreen = true,
                        mediaState = mediaState,
                        duration = duration,
                        isMultipleServer = serverList.size > 1,
                        subtitleOffset = subtitleOffset,
                        onServerChange = {
                            val values = serverList.mapIndexed { index, episode ->
                                Triple(index, 0, episode.serverName ?: "Server $index")
                            }
                            val currentIndex = values.getOrNull(serverData.first)
                            val callback: BottomSheetCallback = { index ->
                                viewModel.changeServer(index.first)
                                bottomSheetState = null
                            }
                            bottomSheetState = Triple(currentIndex, values, callback)
                        },
                        onQualityChange = {
                            bottomSheetState =
                                Triple(qualityTracks.first, qualityTracks.second, trackCallBack)
                        },
                        onAudioChange = {
                            bottomSheetState =
                                Triple(audioTracks.first, audioTracks.second, trackCallBack)
                        },
                        onSubtitleChange = {
                            bottomSheetState =
                                Triple(subtitleTracks.first, subtitleTracks.second, trackCallBack)
                        },
                        onNextEpisode = {
                            viewModel.nextEpisode()
                        }
                    ) {
                        viewModel.onSubtitleOffsetChange(it)
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .background(Color.Black)
                            .fillMaxSize()
                    ) {
                        Box {
                            CustomPlayerView(
                                context = context,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16 / 9f),
                                exoPlayer = viewModel.exoPlayer,
                                fullScreen = false,
                                mediaState = mediaState,
                                duration = duration,
                                isMultipleServer = serverList.size > 1,
                                subtitleOffset = subtitleOffset,
                                onServerChange = {
                                    val values = serverList.mapIndexed { index, episode ->
                                        Triple(index, 0, episode.serverName ?: "Server $index")
                                    }
                                    val currentIndex = values.getOrNull(serverData.first)
                                    val callback: BottomSheetCallback = { index ->
                                        viewModel.changeServer(index.first)
                                        bottomSheetState = null
                                    }
                                    bottomSheetState = Triple(currentIndex, values, callback)
                                },
                                onQualityChange = {
                                    bottomSheetState =
                                        Triple(
                                            qualityTracks.first,
                                            qualityTracks.second,
                                            trackCallBack
                                        )
                                },
                                onAudioChange = {
                                    bottomSheetState =
                                        Triple(audioTracks.first, audioTracks.second, trackCallBack)
                                },
                                onSubtitleChange = {
                                    bottomSheetState =
                                        Triple(
                                            subtitleTracks.first,
                                            subtitleTracks.second,
                                            trackCallBack
                                        )
                                },
                                onNextEpisode = {
                                    viewModel.nextEpisode()
                                }
                            ) {
                                viewModel.onSubtitleOffsetChange(it)
                            }

                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            if (!movie.trailerUrl.isNullOrEmpty()) IconButton(
                                onClick = { context.openChromeCustomTab(movie.trailerUrl) },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_youtube),
                                    contentDescription = "Trailer",
                                    tint = Color.White
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                        ) {

                            Text(
                                text = movie.name ?: "",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    shadow = Shadow(
                                        color = MaterialTheme.colorScheme.secondary,
                                        offset = Offset(0f, 0f),
                                        blurRadius = 0.5f
                                    )
                                ),
                                modifier = Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 12.dp,
                                    bottom = 6.dp
                                )
                            )

                            Text(
                                text = movie.content ?: "",
                                color = Color.White,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                            )

                            CategoryChips(
                                movie.category ?: emptyList(),
                                Modifier.padding(vertical = 12.dp)
                            )

                            SectionView(
                                items = serverData.second?.serverData ?: emptyList(),
                                headerResId = R.string.server,
                                modifier = Modifier,
                                header = serverData.second?.serverName,
                                color = Color.White,
                                itemContent = { item, idx ->
                                    Text(
                                        text = item.name ?: "",
                                        color = if (idx == playerIndex) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                        style = MaterialTheme.typography.bodyMedium.copy(
                                            shadow = Shadow(
                                                color = Color.Black,
                                                offset = Offset(0f, 0f),
                                                blurRadius = 0.5f
                                            )
                                        ),
                                        modifier = Modifier
                                            .background(
                                                if (idx == playerIndex) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                                RoundedCornerShape(20)
                                            )
                                            .padding(horizontal = 12.dp, vertical = 6.dp)
                                            .clickable {
                                                viewModel.changeEpisode(idx)
                                            },
                                    )
                                })

                        }
                    }
                }
            }
        } else {
            CircularProgressIndicator()
        }
    }
}


@Composable
fun SuperStreamDetailScreen(
    navController: NavController,
    viewModel: SuperStreamDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerIndex by viewModel.playerIndex.collectAsStateWithLifecycle()
    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()
    val serverIndex by viewModel.serverIndex.collectAsStateWithLifecycle()
    val sourceLinks by viewModel.sourceLinks.collectAsStateWithLifecycle()
    val qualityTracks by viewModel.qualityTracks.collectAsStateWithLifecycle()
    val audioTracks by viewModel.audioTracks.collectAsStateWithLifecycle()
    val subtitleTracks by viewModel.subtitleTracks.collectAsStateWithLifecycle()
    val subtitleOffset by viewModel.subtitleOffset.collectAsStateWithLifecycle()
    var bottomSheetState by rememberSaveable { mutableStateOf<BottomSheetState?>(null) }

    BottomSheetSelectTracks(
        bottomSheetState = bottomSheetState,
        dismiss = {
            bottomSheetState = null
        }
    )

    val trackCallBack: BottomSheetCallback = { index ->
        viewModel.changeTrack(index.first, index.second)
        bottomSheetState = null
    }

    val context = LocalContext.current
    val configureScreen = LocalConfiguration.current

    val exitFullscreen = {
        context.setScreenOrientation(SCREEN_ORIENTATION_USER_PORTRAIT)
    }

    LaunchedEffect(key1 = context) {
        viewModel.error.collect {
            Toast.makeText(context, it.error.message, Toast.LENGTH_LONG).show()
            navController.popBackStack()
        }
    }

    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .fillMaxSize()
    ) {
        val movie = uiState
        if (movie != null) {
            when (configureScreen.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    BackHandler {
                        exitFullscreen()
                    }
                    CustomPlayerView(
                        context = context,
                        modifier = Modifier.fillMaxSize(),
                        exoPlayer = viewModel.exoPlayer,
                        fullScreen = true,
                        mediaState = mediaState,
                        duration = duration,
                        isMultipleServer = sourceLinks.size > 1,
                        subtitleOffset = subtitleOffset,
                        onServerChange = {
                            val values =
                                sourceLinks.mapIndexed { index, src -> Triple(index, 0, src.name) }
                            val server = values.getOrNull(serverIndex)
                            val callback: BottomSheetCallback = { index ->
                                viewModel.changeServer(index.first)
                                bottomSheetState = null
                            }
                            bottomSheetState = Triple(server, values, callback)
                        },
                        onQualityChange = {
                            bottomSheetState =
                                Triple(qualityTracks.first, qualityTracks.second, trackCallBack)
                        },
                        onAudioChange = {
                            bottomSheetState =
                                Triple(audioTracks.first, audioTracks.second, trackCallBack)
                        },
                        onSubtitleChange = {
                            bottomSheetState =
                                Triple(subtitleTracks.first, subtitleTracks.second, trackCallBack)
                        },
                        onNextEpisode = {
                            viewModel.nextEpisode()
                        }
                    ) {
                        viewModel.onSubtitleOffsetChange(it)
                    }
                }

                else -> {
                    Column(
                        modifier = Modifier
                            .background(Color.Black)
                            .fillMaxSize()
                    ) {
                        Box {
                            CustomPlayerView(
                                context = context,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .aspectRatio(16 / 9f),
                                exoPlayer = viewModel.exoPlayer,
                                fullScreen = false,
                                mediaState = mediaState,
                                duration = duration,
                                isMultipleServer = sourceLinks.size > 1,
                                subtitleOffset = subtitleOffset,
                                onServerChange = {
                                    val values =
                                        sourceLinks.mapIndexed { index, src ->
                                            Triple(
                                                index,
                                                0,
                                                src.name
                                            )
                                        }
                                    val server = values.getOrNull(serverIndex)
                                    val callback: BottomSheetCallback = { index ->
                                        viewModel.changeServer(index.first)
                                        bottomSheetState = null
                                    }
                                    bottomSheetState = Triple(server, values, callback)
                                },
                                onQualityChange = {
                                    bottomSheetState =
                                        Triple(
                                            qualityTracks.first,
                                            qualityTracks.second,
                                            trackCallBack
                                        )
                                },
                                onAudioChange = {
                                    bottomSheetState =
                                        Triple(audioTracks.first, audioTracks.second, trackCallBack)
                                },
                                onSubtitleChange = {
                                    bottomSheetState =
                                        Triple(
                                            subtitleTracks.first,
                                            subtitleTracks.second,
                                            trackCallBack
                                        )
                                },
                                onNextEpisode = {
                                    viewModel.nextEpisode()
                                }
                            ) {
                                viewModel.onSubtitleOffsetChange(it)
                            }

                            IconButton(
                                onClick = { navController.popBackStack() },
                                modifier = Modifier.align(Alignment.TopStart)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "Back",
                                    tint = Color.White
                                )
                            }

                            if (!movie.trailerUrl.isNullOrEmpty()) IconButton(
                                onClick = { context.openChromeCustomTab(movie.trailerUrl ?: "") },
                                modifier = Modifier.align(Alignment.TopEnd)
                            ) {
                                Icon(
                                    painterResource(id = R.drawable.ic_youtube),
                                    contentDescription = "Trailer",
                                    tint = Color.White
                                )
                            }
                        }

                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .verticalScroll(rememberScrollState()),
                        ) {

                            Text(
                                text = movie.name ?: "",
                                color = Color.White,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontSize = 20.sp,
                                    fontWeight = FontWeight.Bold,
                                    shadow = Shadow(
                                        color = MaterialTheme.colorScheme.secondary,
                                        offset = Offset(0f, 0f),
                                        blurRadius = 0.5f
                                    )
                                ),
                                modifier = Modifier.padding(
                                    start = 20.dp,
                                    end = 20.dp,
                                    top = 12.dp,
                                    bottom = 6.dp
                                )
                            )

                            Text(
                                text = movie.content ?: "",
                                color = Color.White,
                                modifier = Modifier.padding(start = 20.dp, end = 20.dp)
                            )

                            CategoryChips(
                                movie.getListCategories(),
                                Modifier.padding(vertical = 12.dp)
                            )

                            val episode =
                                (movie as? SuperStreamResponse.SuperStreamTvDetail)?.data?.episode
                                    ?: return
                            if (episode.isNotEmpty()) {
                                episode.forEachIndexed { index, item ->
                                    EpisodeItem(
                                        item,
                                        index == playerIndex
                                    ) {
                                        viewModel.changeEpisode(index)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            CircularProgressIndicator()
        }
    }
}


@HiltViewModel
class OMovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
    private val mediaRepository: MediaRepository
) : BaseMovieDetailViewModel() {

    override val exoPlayer by lazy {
        context.createExoPlayer()
    }

    override val trackNameProvider by lazy {
        context.createTrackNameProvider()
    }

    private val _uiState = MutableStateFlow<OMovieDetail?>(null)
    val uiState = _uiState.asStateFlow()

    private val _error = Channel<NetworkResponse.Error>()
    val error = _error.receiveAsFlow()

    private val _playerIndex = MutableStateFlow(0)
    val playerIndex = _playerIndex.asStateFlow()

    private val _serverData = MutableStateFlow<Pair<Int, OMovieDetail.Episode?>>(0 to null)
    val serverData = _serverData.asStateFlow()

    private val _serverList = MutableStateFlow<List<OMovieDetail.Episode>>(emptyList())
    val serverList = _serverList.asStateFlow()

    override suspend fun saveHistory() {
        val id = uiState.value?.slug ?: return
        mediaRepository.insertHistory(
            MediaHistory(
                id = id,
                serverIdx = serverData.value.first,
                index = playerIndex.value,
                position = exoPlayer.currentPosition
            )
        )
    }

    override fun nextEpisode() {
        val newIndex = playerIndex.value + 1
        if (newIndex < (serverData.value.second?.serverData?.size ?: 0)) {
            changeEpisode(newIndex)
        }
    }

    fun changeServer(serverIndex: Int) {
        val data = serverList.value.getOrNull(serverIndex)
        val position = exoPlayer.currentPosition
        _serverData.value = serverIndex to data
        updatePlayer(if (position > 0) position else 0)
    }

    fun changeEpisode(index: Int) {
        _playerIndex.value = index
        updatePlayer(0L)
    }

    private fun updatePlayer(position: Long) {
        val episode = serverData.value.second?.serverData?.getOrNull(playerIndex.value) ?: return
        val url = episode.linkM3u8 ?: return
        prepare(SourceLink(episode.name ?: "${1}", url), initialPlaybackPosition = position)
    }

    init {
        viewModelScope.launch {
            exoPlayer.addListener(this@OMovieDetailViewModel)
            savedStateHandle.get<String>(NavScreen.OMovieDetailScreen.SLUG)?.let { slug ->
                val response = when (val res = mediaRepository.getOMovieDetail(slug)) {
                    is NetworkResponse.Error -> mediaRepository.getOMovieDetail2(slug)
                    is NetworkResponse.Success -> res
                }

                when (response) {
                    is NetworkResponse.Error -> _error.send(response)
                    is NetworkResponse.Success -> handleData(response, slug)
                }
            }
        }
    }

    private suspend fun handleData(response: NetworkResponse.Success<OMovieDetail>, slug: String) {
        _uiState.value = response.data
        val movieHistory = mediaRepository.getMediaHistory(slug)
        if (!response.data.episodes.isNullOrEmpty()) {
            _serverList.value = response.data.episodes
            _playerIndex.value = movieHistory?.index ?: 0
            val serverIndex = movieHistory?.serverIdx ?: 0
            val data = serverList.value.getOrNull(serverIndex)
            _serverData.value = serverIndex to data
            updatePlayer(movieHistory?.position ?: 0)
        }
    }
}


@HiltViewModel
class SuperStreamDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    @ApplicationContext context: Context,
    private val mediaRepository: MediaRepository
) : BaseMovieDetailViewModel() {

    override val exoPlayer by lazy {
        context.createExoPlayer()
    }

    override val trackNameProvider by lazy {
        context.createTrackNameProvider()
    }

    private val _uiState = MutableStateFlow<SuperStreamResponse?>(null)
    val uiState = _uiState.asStateFlow()

    private val _error = Channel<NetworkResponse.Error>()
    val error = _error.receiveAsFlow()

    private val _playerIndex = MutableStateFlow(0)
    val playerIndex = _playerIndex.asStateFlow()

    private val _serverIndex = MutableStateFlow(0)
    val serverIndex = _serverIndex.asStateFlow()

    private val _sourceLinks = MutableStateFlow<List<SourceLink>>(emptyList())
    val sourceLinks = _sourceLinks.asStateFlow()

    private var _tmpSubtitles: List<Subtitle> = emptyList()
    private var _tmpEpisodes: List<Episode> = emptyList()

    private suspend fun getDetail(id: String, type: SSMediaType) = when (type) {
        SSMediaType.Movies -> mediaRepository.getSuperStreamMovieDetail(id)
        SSMediaType.Series -> mediaRepository.getSuperStreamTvShowDetail(id)
    }

    override suspend fun saveHistory() {
        val id = uiState.value?.id ?: return
        mediaRepository.insertHistory(
            MediaHistory(
                id = id,
                serverIdx = serverIndex.value,
                index = playerIndex.value,
                position = exoPlayer.currentPosition
            )
        )
    }

    fun changeServer(serverIndex: Int) {
        _serverIndex.value = serverIndex
        val sourceLink = sourceLinks.value.getOrNull(serverIndex) ?: return
        val position = exoPlayer.currentPosition
        prepare(sourceLink, _tmpSubtitles, if (position > 0) position else 0)
    }

    fun changeEpisode(index: Int) {
        viewModelScope.launch {
            _playerIndex.value = index
            getEpisodeLink(index)?.let { value -> updatePlayer(value, 0L) }
        }
    }

    override fun nextEpisode() {
        val newIndex = playerIndex.value + 1
        if (newIndex < (_tmpEpisodes.size)) {
            changeEpisode(newIndex)
        }
    }

    private fun updatePlayer(data: Pair<List<SourceLink>, List<Subtitle>>, position: Long) {
        _sourceLinks.value = data.first
        _tmpSubtitles = data.second
        val serverIndex = serverIndex.value
        prepare(
            sourceLinks.value.getOrNull(serverIndex) ?: return,
            _tmpSubtitles,
            if (position > 0) position else 0
        )
    }

    private suspend fun getEpisodeLink(index: Int): Pair<List<SourceLink>, List<Subtitle>>? {
        val episode = _tmpEpisodes.getOrNull(index) ?: return null
        return mediaRepository.getSourceLinksSuperStream(
            episode.tid ?: episode.id ?: return null,
            episode.season,
            episode.episode,
            episode.id,
            episode.imdbId
        )
    }


    init {
        viewModelScope.launch {
            exoPlayer.prepare()
            exoPlayer.addListener(this@SuperStreamDetailViewModel)
            val media =
                savedStateHandle.get<SearchResultItem>(NavScreen.SuperStreamMovieDetailScreen.SS_MOVIE)
            if (media?.id != null) {
                when (val response = getDetail(media.id, media.filmType)) {
                    is NetworkResponse.Error -> _error.send(response)
                    is NetworkResponse.Success -> {
                        _uiState.value = response.data
                        val movieHistory = mediaRepository.getMediaHistory(response.data.id)
                        if (movieHistory != null) {
                            _serverIndex.value = movieHistory.serverIdx
                            _playerIndex.value = movieHistory.index
                        }
                        val data = when (response.data) {
                            is SuperStreamResponse.SuperStreamMovieDetail -> mediaRepository.getSourceLinksSuperStream(
                                response.data.data?.id ?: return@launch,
                                null,
                                null,
                                response.data.data.id,
                                response.data.data.imdbId
                            )

                            is SuperStreamResponse.SuperStreamTvDetail -> {
                                if (response.data.data?.episode.isNullOrEmpty().not()) {
                                    _tmpEpisodes = response.data.data?.episode ?: return@launch
                                    getEpisodeLink(playerIndex.value) ?: return@launch
                                } else return@launch
                            }
                        }

                        updatePlayer(data, movieHistory?.position ?: 0L)
                    }
                }
            }
        }
    }
}



@OptIn(UnstableApi::class)
abstract class BaseMovieDetailViewModel : ViewModel(), Player.Listener {

    abstract val exoPlayer: ExoPlayer

    abstract val trackNameProvider: TrackNameProvider

    private val _mediaState: MutableStateFlow<MediaState> = MutableStateFlow(MediaState.Init)
    val mediaState = _mediaState.asStateFlow()

    private val _duration: MutableStateFlow<Long> = MutableStateFlow(0L)
    val duration = _duration.asStateFlow()

    private val _subtitleTracks =
        MutableStateFlow<Pair<TrackValue?, BottomSheetValues>>(null to emptyList())
    val subtitleTracks = _subtitleTracks.asStateFlow()

    private val _audioTracks =
        MutableStateFlow<Pair<TrackValue?, BottomSheetValues>>(null to emptyList())
    val audioTracks = _audioTracks.asStateFlow()

    private val _qualityTracks =
        MutableStateFlow<Pair<TrackValue?, BottomSheetValues>>(null to emptyList())
    val qualityTracks = _qualityTracks.asStateFlow()

    private val _subtitleOffset = MutableStateFlow(0L)
    val subtitleOffset = _subtitleOffset.asStateFlow()

    private var currentTextRenderer: CustomTextRenderer? = null

    @OptIn(UnstableApi::class)
    fun Context.createExoPlayer(): ExoPlayer {
        return buildExoplayer().setRenderersFactory { eventHandler, videoRendererEventListener, audioRendererEventListener, textRendererOutput, metadataRendererOutput ->
            getRenderers(
                eventHandler = eventHandler,
                videoRendererEventListener = videoRendererEventListener,
                audioRendererEventListener = audioRendererEventListener,
                textRendererOutput = textRendererOutput,
                metadataRendererOutput = metadataRendererOutput,
                subtitleOffset = subtitleOffset.value,
                onTextRendererChange = {
                    currentTextRenderer = it
                }
            )
        }.build()
    }

    @OptIn(UnstableApi::class)
    fun Context.createTrackNameProvider(): TrackNameProvider {
        return DefaultTrackNameProvider(this.resources)
    }

    fun onSubtitleOffsetChange(offset: Long) {
        _subtitleOffset.value = offset
        currentTextRenderer?.setRenderOffsetMs(offset)
    }

    override fun onCleared() {
        viewModelScope.launch {
            saveHistory()
            exoPlayer.release()
            super.onCleared()
        }
    }

    abstract suspend fun saveHistory()

    fun changeTrack(tracksIndex: Int, trackIndex: Int) {
        val trackGroup = exoPlayer.currentTracks.groups.getOrNull(tracksIndex)
        if (trackGroup != null) {
            exoPlayer.trackSelectionParameters =
                exoPlayer.trackSelectionParameters
                    .buildUpon()
                    .setOverrideForType(
                        TrackSelectionOverride(trackGroup.mediaTrackGroup, trackIndex)
                    )
                    .build()
        }
    }

    @UnstableApi
    override fun onTracksChanged(tracks: Tracks) {
        super.onTracksChanged(tracks)
        var subtitleIndex: TrackValue? = null
        val subtitles = mutableListOf<TrackValue>()

        var audioIndex: TrackValue? = null
        val audios = mutableListOf<TrackValue>()

        var qualityIndex: TrackValue? = null
        val qualities = mutableListOf<TrackValue>()

        tracks.groups.forEachIndexed { index, trackGroup ->
            for (i in 0 until trackGroup.length) {
                val isSelected = trackGroup.isTrackSelected(i)
                val trackName = trackNameProvider.getTrackName(trackGroup.getTrackFormat(i))
                val trackValue = TrackValue(index, i, trackName)
                when (trackGroup.type) {
                    TRACK_TYPE_TEXT -> {
                        if (isSelected) subtitleIndex = trackValue
                        subtitles.add(trackValue)
                    }

                    TRACK_TYPE_AUDIO -> {
                        if (isSelected) audioIndex = trackValue
                        audios.add(trackValue)
                    }

                    TRACK_TYPE_VIDEO -> {
                        if (isSelected) qualityIndex = trackValue
                        qualities.add(trackValue)
                    }
                }
            }
        }

        _subtitleTracks.value = subtitleIndex to subtitles
        _audioTracks.value = audioIndex to audios
        _qualityTracks.value = qualityIndex to qualities
    }


    override fun onPlaybackStateChanged(playbackState: Int) {
        super.onPlaybackStateChanged(playbackState)
        when (playbackState) {
            Player.STATE_IDLE -> _mediaState.value = MediaState.Init
            Player.STATE_READY -> {
                _mediaState.value = MediaState.Playing(exoPlayer.isPlaying)
                _duration.value = exoPlayer.duration
            }

            Player.STATE_BUFFERING -> _mediaState.value = MediaState.Loading
            Player.STATE_ENDED -> nextEpisode()
        }
    }

    abstract fun nextEpisode()

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        _mediaState.value = MediaState.Playing(isPlaying)
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        _mediaState.value = MediaState.Error(error)
    }

    @UnstableApi
    private fun createMediaSource(
        url: String,
        title: String?
    ): MediaSource {
        return if (url.contains(".m3u8")) {
            HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
                .createMediaSource(
                    createMediaItemBuilder(url, title)
                        .setMimeType(MimeTypes.APPLICATION_M3U8)
                        .build()
                )
        } else if (url.contains(".mpd")) {
            DashMediaSource.Factory(DefaultHttpDataSource.Factory())
                .createMediaSource(
                    createMediaItemBuilder(url, title)
                        .setMimeType(MimeTypes.APPLICATION_MPD)
                        .build()
                )
        } else {
            DefaultMediaSourceFactory(DefaultHttpDataSource.Factory())
                .createMediaSource(
                    createMediaItemBuilder(url, title)
                        .build()
                )
        }
    }

    private fun createMediaItemBuilder(url: String, title: String?): MediaItem.Builder {
        return MediaItem.Builder()
            .setUri(url)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setDisplayTitle(title)
                    .build()
            )
    }

    @OptIn(UnstableApi::class)
    private fun createSubtitleSources(subtitles: List<Subtitle>): Array<SingleSampleMediaSource> {
        return subtitles
            .filter {
                it.name?.substring(0..1)?.lowercase() == "vi"
                        || it.name?.substring(0..1)?.lowercase() == "en"
            }
            .map { subtitle ->
                val subtitleConfiguration = MediaItem.SubtitleConfiguration
                    .Builder(Uri.parse(subtitle.url))
                    .setMimeType(getSubtitleMimeType(subtitle))
                    .setLabel(subtitle.name)
                    .setLanguage(subtitle.name?.substring(0..1)?.lowercase())
                    .build()

                SingleSampleMediaSource.Factory(
                    DefaultHttpDataSource.Factory()
                ).createMediaSource(subtitleConfiguration, C.TIME_UNSET)
            }.toTypedArray()
    }

    @OptIn(UnstableApi::class)
    @MainThread
    fun prepare(
        sourceLink: SourceLink,
        subtitles: List<Subtitle> = emptyList(),
        initialPlaybackPosition: Long = 0L,
    ) {
        Log.d("AAA", "prepare:$sourceLink")
        exoPlayer.run {
            val mediaSource = createMediaSource(url = sourceLink.url, title = sourceLink.name)
            setMediaSource(
                /* mediaSource = */ MergingMediaSource(
                    mediaSource,
                    *createSubtitleSources(subtitles)
                ),
                /* startPositionMs = */ initialPlaybackPosition
            )
            prepare()
        }
    }

    private fun getSubtitleMimeType(subtitle: Subtitle): String {
        return when {
            subtitle.url.contains(".vtt", true) -> MimeTypes.TEXT_VTT
            subtitle.url.contains(".ssa", true) -> MimeTypes.TEXT_SSA
            subtitle.url.contains(".ttml", true) || subtitle.url.contains(
                ".xml",
                true
            ) -> MimeTypes.APPLICATION_TTML

            subtitle.url.contains(".srt", true) -> MimeTypes.APPLICATION_SUBRIP
            else -> MimeTypes.APPLICATION_SUBRIP
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










