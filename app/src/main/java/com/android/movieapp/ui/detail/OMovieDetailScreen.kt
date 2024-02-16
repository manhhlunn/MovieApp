package com.android.movieapp.ui.detail

import android.content.Context
import android.content.pm.ActivityInfo
import android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT
import android.content.res.Configuration
import android.widget.Toast
import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.media3.common.C.TRACK_TYPE_AUDIO
import androidx.media3.common.C.TRACK_TYPE_TEXT
import androidx.media3.common.C.TRACK_TYPE_VIDEO
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.dash.DashMediaSource
import androidx.media3.exoplayer.hls.HlsMediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import androidx.media3.ui.TrackSelectionDialogBuilder
import androidx.navigation.NavController
import com.android.movieapp.NavScreen
import com.android.movieapp.R
import com.android.movieapp.models.entities.MovieHistory
import com.android.movieapp.models.network.Category
import com.android.movieapp.models.network.MyMovie
import com.android.movieapp.models.network.NetworkResponse
import com.android.movieapp.models.network.OMovieDetailResponse
import com.android.movieapp.repository.OMovieRepository
import com.android.movieapp.ui.ext.OnLifecycleEvent
import com.android.movieapp.ui.ext.ifNull
import com.android.movieapp.ui.ext.isEnableSelect
import com.android.movieapp.ui.ext.makeTimeString
import com.android.movieapp.ui.ext.openChromeCustomTab
import com.android.movieapp.ui.ext.setScreenOrientation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

sealed class MediaState {
    data object Init : MediaState()
    data object Loading : MediaState()
    data class Error(val error: Exception) : MediaState()
    data class Playing(val isPlay: Boolean) : MediaState()
}

@OptIn(UnstableApi::class)
@Composable
fun CustomPlayerView(
    context: Context,
    modifier: Modifier,
    lifecycle: Lifecycle.Event,
    exoPlayer: ExoPlayer,
    fullScreen: Boolean,
    mediaState: MediaState,
    duration: Long
) {
    var currentPosition by remember { mutableLongStateOf(0) }
    var controllerShowTime by remember { mutableLongStateOf(2000L) }

    var zoomMode by remember { mutableStateOf(false) }
    var speedMode by remember { mutableStateOf(false) }

    var scale by remember { mutableFloatStateOf(1f) }

    val isShowController =
        controllerShowTime > 0 || controllerShowTime == -1L || (mediaState as? MediaState.Playing)?.isPlay != true

    LaunchedEffect(key1 = controllerShowTime, key2 = isShowController) {
        if (controllerShowTime > 0) {
            delay(100L)
            controllerShowTime -= 100L
        }
        if (exoPlayer.currentPosition > 0 &&  controllerShowTime != -1L) currentPosition = exoPlayer.currentPosition
    }

    Box {
        AndroidView(
            factory = {
                PlayerView(it).apply {
                    player = exoPlayer
                    useController = false
                    resizeMode =
                        if (zoomMode) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT
                }
            },
            update = {
                it.resizeMode =
                    if (zoomMode) AspectRatioFrameLayout.RESIZE_MODE_ZOOM else AspectRatioFrameLayout.RESIZE_MODE_FIT
                when (lifecycle) {
                    Lifecycle.Event.ON_PAUSE -> {
                        it.onPause()
                        it.player?.pause()
                    }

                    Lifecycle.Event.ON_RESUME -> {
                        it.onResume()
                    }

                    else -> Unit
                }
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
            ) {
                IconButton(
                    onClick = {
                        controllerShowTime = 2000L
                        zoomMode = !zoomMode
                    }
                ) {
                    Icon(
                        painterResource(id = if (zoomMode) R.drawable.baseline_width_full_24 else R.drawable.baseline_width_normal_24),
                        contentDescription = "Full width",
                        tint = Color.White
                    )
                }

                IconButton(
                    onClick = {
                        controllerShowTime = 2000L
                        scale -= 0.02f
                    }
                ) {
                    Icon(
                        painterResource(id = R.drawable.baseline_zoom_out_24),
                        contentDescription = "Zoom out",
                        tint = Color.White
                    )
                }
            }

            if (fullScreen && isShowController) Column(
                modifier = Modifier
                    .align(Alignment.TopEnd)
            ) {
                IconButton(onClick = {
                    controllerShowTime = 2000L
                    exoPlayer.setPlaybackSpeed(if (speedMode) 1f else 1.5f)
                    speedMode = !speedMode
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_speed_24),
                        contentDescription = "Change speed mode",
                        tint = if (speedMode) Color.Yellow else Color.White
                    )
                }

                IconButton(onClick = {
                    controllerShowTime = 2000L
                    scale += 0.02f
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_zoom_in_24),
                        contentDescription = "Zoom in",
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
                            TrackSelectionDialogBuilder(
                                context,
                                "Audio",
                                exoPlayer,
                                TRACK_TYPE_AUDIO
                            ).build().show()
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
                            TrackSelectionDialogBuilder(
                                context,
                                "Subtitles",
                                exoPlayer,
                                TRACK_TYPE_TEXT
                            ).build().show()
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
                            TrackSelectionDialogBuilder(
                                context,
                                "Quality",
                                exoPlayer,
                                TRACK_TYPE_VIDEO
                            ).build().show()
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

                IconButton(onClick = {
                    controllerShowTime = 2000L
                    context.setScreenOrientation(
                        if (fullScreen) ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                        else ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    )
                }) {
                    Icon(
                        painterResource(id = if (fullScreen) R.drawable.baseline_fullscreen_exit_24 else R.drawable.baseline_fullscreen_24),
                        contentDescription = "Full Screen",
                        tint = Color.White
                    )
                }
            }

            if (isShowController) Row(
                modifier = Modifier
                    .fillMaxWidth(0.86f)
                    .align(Alignment.Center),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    controllerShowTime = 2000L
                    exoPlayer.seekToPrevious()
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_skip_previous_24),
                        contentDescription = "To previous",
                        tint = Color.White,
                        modifier = Modifier.scale(1.2f)
                    )
                }

                IconButton(onClick = {
                    controllerShowTime = 2000L
                    exoPlayer.seekBack()
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_fast_rewind_24),
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
                        painterResource(id = R.drawable.baseline_fast_forward_24),
                        contentDescription = "Seek forward",
                        tint = Color.White,
                        modifier = Modifier.scale(1.8f)
                    )
                }

                IconButton(onClick = {
                    controllerShowTime = 2000L
                    exoPlayer.seekToNext()
                }) {
                    Icon(
                        painterResource(id = R.drawable.baseline_skip_next_24),
                        contentDescription = "To next",
                        tint = Color.White,
                        modifier = Modifier.scale(1.2f)
                    )
                }
            }
        }
    }
}


@Composable
fun OMovieDetailScreen(
    navController: NavController,
    viewModel: OMovieDetailViewModel
) {
    OnLifecycleEvent { _, event ->
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                viewModel.saveHistory()
            }

            else -> {}
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerIndex by viewModel.playerIndex.collectAsStateWithLifecycle()
    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()

    var lifecycle by remember {
        mutableStateOf(Lifecycle.Event.ON_CREATE)
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configureScreen = LocalConfiguration.current

    val exitFullscreen = {
        context.setScreenOrientation(SCREEN_ORIENTATION_USER_PORTRAIT)
    }

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
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
        val movie = uiState?.movie
        if (movie != null) {
            when (configureScreen.orientation) {
                Configuration.ORIENTATION_LANDSCAPE -> {
                    BackHandler {
                        exitFullscreen()
                    }
                    CustomPlayerView(
                        context = context,
                        modifier = Modifier.fillMaxSize(),
                        lifecycle = lifecycle,
                        exoPlayer = viewModel.exoPlayer,
                        fullScreen = true,
                        mediaState = mediaState,
                        duration = duration
                    )
                }

                else -> {
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        val (backIcon, trailer, backdrop, poster, title, categories, specs, overview, episodes) = createRefs()
                        val startGuideline = createGuidelineFromStart(16.dp)
                        val endGuideline = createGuidelineFromEnd(16.dp)

                        Backdrop(
                            backdropUrl = movie.posterUrl ?: "",
                            Modifier
                                .constrainAs(backdrop) {
                                    top.linkTo(parent.top)
                                })

                        IconButton(onClick = {
                            navController.popBackStack()
                        }, modifier = Modifier
                            .constrainAs(backIcon) {
                                start.linkTo(parent.start)
                                top.linkTo(backdrop.bottom)
                                end.linkTo(poster.start)
                            })
                        {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.scale(1.2f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }

                        if (!movie.trailerUrl.isNullOrEmpty()) IconButton(onClick = {
                            context.openChromeCustomTab(movie.trailerUrl)
                        }, modifier = Modifier
                            .constrainAs(trailer) {
                                start.linkTo(poster.end)
                                top.linkTo(backdrop.bottom)
                                end.linkTo(parent.end)
                            })
                        {
                            Icon(
                                painterResource(id = R.drawable.ic_youtube),
                                contentDescription = "Trailer",
                                modifier = Modifier.scale(1.2f),
                            )
                        }

                        Poster(posterUrl = movie.thumbUrl ?: "", modifier = Modifier
                            .width(120.dp)
                            .padding(top = 120.dp)
                            .constrainAs(poster) {
                                centerAround(backdrop.bottom)
                                linkTo(startGuideline, endGuideline)
                            })

                        Title(
                            movie.name,
                            movie.originName,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .constrainAs(title) {
                                    top.linkTo(poster.bottom)
                                    linkTo(start = startGuideline, end = endGuideline)
                                })

                        CategoryChips(
                            uiState?.movie?.category ?: emptyList(),
                            modifier = Modifier.constrainAs(categories) {
                                top.linkTo(title.bottom, 16.dp)
                                linkTo(startGuideline, endGuideline)
                            },
                        )

                        OMovieFields(
                            movie,
                            modifier = Modifier.constrainAs(specs) {
                                top.linkTo(categories.bottom, 16.dp)
                                linkTo(startGuideline, endGuideline)
                            },
                        )

                        Text(
                            text = movie.content ?: "",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                lineHeight = 24.sp
                            ),
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .padding(horizontal = 16.dp)
                                .constrainAs(overview) {
                                    top.linkTo(specs.bottom, 16.dp)
                                    linkTo(startGuideline, endGuideline)
                                }
                        )

                        val episodesList = uiState?.episodes
                        if (!episodesList.isNullOrEmpty()) {
                            Column(modifier = Modifier
                                .constrainAs(episodes) {
                                    top.linkTo(overview.bottom, 16.dp)
                                    linkTo(startGuideline, endGuideline)
                                    bottom.linkTo(parent.bottom, 16.dp)
                                }) {

                                CustomPlayerView(
                                    context = context,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16 / 9f),
                                    lifecycle = lifecycle,
                                    exoPlayer = viewModel.exoPlayer,
                                    fullScreen = false,
                                    mediaState = mediaState,
                                    duration = duration
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                episodesList.forEach {
                                    SectionView(
                                        items = it.serverData ?: emptyList(),
                                        headerResId = R.string.server,
                                        modifier = Modifier,
                                        header = "${it.serverName} (App)",
                                        itemContent = { item, idx ->
                                            Text(
                                                text = item.name ?: "",
                                                color = if (idx == playerIndex?.second && playerIndex?.first == it) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    shadow = Shadow(
                                                        color = Color.Black,
                                                        offset = Offset(0f, 0f),
                                                        blurRadius = 0.5f
                                                    )
                                                ),
                                                modifier = Modifier
                                                    .background(
                                                        if (idx == playerIndex?.second && playerIndex?.first == it) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                                        RoundedCornerShape(20)
                                                    )
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                                    .clickable {
                                                        viewModel.changeEpisode(it, idx)
                                                    },
                                            )
                                        }
                                    )

                                    SectionView(
                                        items = it.serverData ?: emptyList(),
                                        headerResId = R.string.server,
                                        modifier = Modifier,
                                        header = "${it.serverName} (Web)",
                                        itemContent = { item, _ ->
                                            Text(
                                                text = item.name ?: "",
                                                color = MaterialTheme.colorScheme.primary,
                                                style = MaterialTheme.typography.bodyMedium.copy(
                                                    shadow = Shadow(
                                                        color = Color.Black,
                                                        offset = Offset(0f, 0f),
                                                        blurRadius = 0.5f
                                                    )
                                                ),
                                                modifier = Modifier
                                                    .background(
                                                        MaterialTheme.colorScheme.onSecondary,
                                                        RoundedCornerShape(20)
                                                    )
                                                    .padding(horizontal = 12.dp, vertical = 6.dp)
                                                    .clickable {
                                                        context.openChromeCustomTab(
                                                            item.linkEmbed ?: ""
                                                        )
                                                    }
                                            )
                                        }
                                    )
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

@Composable
fun MyMovieDetailScreen(
    navController: NavController,
    viewModel: MyMovieDetailViewModel
) {

    OnLifecycleEvent { _, event ->
        // do stuff on event
        when (event) {
            Lifecycle.Event.ON_PAUSE -> {
                viewModel.saveHistory()
            }

            else -> {}
        }
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val playerIndex by viewModel.playerIndex.collectAsStateWithLifecycle()
    val mediaState by viewModel.mediaState.collectAsStateWithLifecycle()
    val duration by viewModel.duration.collectAsStateWithLifecycle()

    var lifecycle by remember {
        mutableStateOf(Lifecycle.Event.ON_CREATE)
    }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val configureScreen = LocalConfiguration.current

    val exitFullscreen = {
        context.setScreenOrientation(SCREEN_ORIENTATION_USER_PORTRAIT)
    }

    DisposableEffect(key1 = lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            lifecycle = event
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
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
                        lifecycle = lifecycle,
                        exoPlayer = viewModel.exoPlayer,
                        fullScreen = true,
                        mediaState = mediaState,
                        duration = duration
                    )
                }

                else -> {
                    ConstraintLayout(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState()),
                    ) {
                        val (backIcon, backdrop, poster, title, episodes) = createRefs()
                        val startGuideline = createGuidelineFromStart(16.dp)
                        val endGuideline = createGuidelineFromEnd(16.dp)

                        Backdrop(
                            backdropUrl = movie.thumbUrl ?: "",
                            Modifier
                                .constrainAs(backdrop) {
                                    top.linkTo(parent.top)
                                })

                        IconButton(onClick = {
                            navController.popBackStack()
                        }, modifier = Modifier
                            .constrainAs(backIcon) {
                                start.linkTo(parent.start)
                                top.linkTo(backdrop.bottom)
                                end.linkTo(poster.start)
                            })
                        {
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                modifier = Modifier.scale(1.2f),
                                tint = MaterialTheme.colorScheme.primary
                            )
                        }


                        Poster(posterUrl = movie.posterUrl ?: "", modifier = Modifier
                            .width(120.dp)
                            .padding(top = 120.dp)
                            .constrainAs(poster) {
                                centerAround(backdrop.bottom)
                                linkTo(startGuideline, endGuideline)
                            })

                        Title(
                            movie.name,
                            movie.originName,
                            modifier = Modifier
                                .padding(horizontal = 20.dp, vertical = 8.dp)
                                .constrainAs(title) {
                                    top.linkTo(poster.bottom)
                                    linkTo(start = startGuideline, end = endGuideline)
                                })

                        val episodesValue = uiState?.episodes
                        if (episodesValue != null) {
                            Column(modifier = Modifier
                                .constrainAs(episodes) {
                                    top.linkTo(title.bottom, 16.dp)
                                    linkTo(startGuideline, endGuideline)
                                    bottom.linkTo(parent.bottom, 16.dp)
                                }) {

                                CustomPlayerView(
                                    context = context,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .aspectRatio(16 / 9f),
                                    lifecycle = lifecycle,
                                    exoPlayer = viewModel.exoPlayer,
                                    fullScreen = false,
                                    mediaState = mediaState,
                                    duration = duration
                                )

                                Spacer(modifier = Modifier.height(12.dp))

                                SectionView(
                                    items = episodesValue.serverData ?: emptyList(),
                                    headerResId = R.string.server,
                                    modifier = Modifier,
                                    header = "${episodesValue.serverName}",
                                    itemContent = { item, idx ->
                                        Text(
                                            text = item.name ?: "",
                                            color = if (idx == playerIndex?.second && playerIndex?.first == episodesValue) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                                            style = MaterialTheme.typography.bodyMedium.copy(
                                                shadow = Shadow(
                                                    color = Color.Black,
                                                    offset = Offset(0f, 0f),
                                                    blurRadius = 0.5f
                                                )
                                            ),
                                            modifier = Modifier
                                                .background(
                                                    if (idx == playerIndex?.second && playerIndex?.first == episodesValue) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                                    RoundedCornerShape(20)
                                                )
                                                .padding(horizontal = 12.dp, vertical = 6.dp)
                                                .clickable {
                                                    viewModel.changeEpisode(episodesValue, idx)
                                                },
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            }
        } else {
            navController.popBackStack()
        }
    }
}

@HiltViewModel
class OMovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val exoPlayer: ExoPlayer,
    private val oMovieRepository: OMovieRepository
) : BaseMovieDetailViewModel(exoPlayer) {

    private val _uiState = MutableStateFlow<OMovieDetailResponse?>(null)
    val uiState: StateFlow<OMovieDetailResponse?> = _uiState

    private val _error = Channel<NetworkResponse.Error>()
    val error = _error.receiveAsFlow()

    fun saveHistory() {
        viewModelScope.launch {
            val slug = uiState.value?.movie?.slug ?: return@launch
            oMovieRepository.insertHistory(
                MovieHistory(
                    slug = slug,
                    serverName = playerIndex.value?.first?.serverName,
                    index = playerIndex.value?.second ?: 0,
                    position = exoPlayer.currentPosition
                )
            )
        }
    }

    init {
        viewModelScope.launch {
            exoPlayer.prepare()
            exoPlayer.addListener(this@OMovieDetailViewModel)
            savedStateHandle.get<String>(NavScreen.OMovieDetailScreen.slug)?.let { slug ->
                when (val response = oMovieRepository.getMovieDetail(slug)) {
                    is NetworkResponse.Error -> _error.send(response)
                    is NetworkResponse.Success -> {
                        _uiState.value = response.data
                        val movieHistory = oMovieRepository.getMovieHistory(slug)
                        if (movieHistory != null) {
                            val episode =
                                response.data.episodes?.firstOrNull { it.serverName == movieHistory.serverName }
                            if (episode != null) {
                                prepareEpisode(episode, movieHistory.index, movieHistory.position)
                                return@launch
                            }
                        }
                        val episode = response.data.episodes?.firstOrNull()
                        if (episode != null) {
                            prepareEpisode(episode, 0)
                        }
                    }
                }
            }
        }
    }
}

@HiltViewModel
class MyMovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    val exoPlayer: ExoPlayer,
    private val oMovieRepository: OMovieRepository
) : BaseMovieDetailViewModel(exoPlayer) {

    private val _uiState = MutableStateFlow<MyMovie?>(null)
    val uiState: StateFlow<MyMovie?> = _uiState

    fun saveHistory() {
        viewModelScope.launch {
            val slug = uiState.value?.id ?: return@launch
            oMovieRepository.insertHistory(
                MovieHistory(
                    slug = slug,
                    serverName = playerIndex.value?.first?.serverName,
                    index = playerIndex.value?.second ?: 0,
                    position = exoPlayer.currentPosition
                )
            )
        }
    }

    init {
        viewModelScope.launch {
            exoPlayer.prepare()
            exoPlayer.addListener(this@MyMovieDetailViewModel)
            savedStateHandle.get<MyMovie>(NavScreen.MyMovieDetailScreen.myMovie)?.let { movie ->
                _uiState.value = movie
                if (movie.id != null) {
                    val movieHistory = oMovieRepository.getMovieHistory(movie.id)
                    val episode = movie.episodes
                    if (episode != null) {
                        if (movieHistory != null) {
                            prepareEpisode(episode, movieHistory.index, movieHistory.position)
                        } else {
                            prepareEpisode(episode, 0)
                        }
                    }
                }
            }
        }
    }
}


abstract class BaseMovieDetailViewModel(
    private val exoPlayer: ExoPlayer
) : ViewModel(), Player.Listener {

    private val _playerIndex = MutableStateFlow<Triple<OMovieDetailResponse.Episode, Int, Int>?>(null)
    val playerIndex: StateFlow<Triple<OMovieDetailResponse.Episode, Int, Int>?> = _playerIndex

    private val _mediaState: MutableStateFlow<MediaState> = MutableStateFlow(MediaState.Init)
    val mediaState: StateFlow<MediaState> = _mediaState

    private val _duration: MutableStateFlow<Long> = MutableStateFlow(0L)
    val duration: StateFlow<Long> = _duration

    override fun onCleared() {
        super.onCleared()
        exoPlayer.release()
    }

    fun changeEpisode(
        it: OMovieDetailResponse.Episode,
        idx: Int
    ) {
        if (playerIndex.value?.first == it) {
            exoPlayer.seekTo(idx, 0)
        } else {
            viewModelScope.launch {
                prepareEpisode(it, idx)
            }
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        super.onMediaItemTransition(mediaItem, reason)
        _playerIndex.update {
            it?.copy(
                second = exoPlayer.currentMediaItemIndex
            )
        }
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
            Player.STATE_ENDED -> exoPlayer.seekToNext()
        }
    }
    override fun onIsPlayingChanged(isPlaying: Boolean) {
        super.onIsPlayingChanged(isPlaying)
        _mediaState.value = MediaState.Playing(isPlaying)
    }

    override fun onPlayerError(error: PlaybackException) {
        super.onPlayerError(error)
        _mediaState.value = MediaState.Error(error)
    }

    @OptIn(UnstableApi::class)
    suspend fun prepareEpisode(
        episode: OMovieDetailResponse.Episode,
        idx: Int,
        position: Long = 0
    ) {
        val mediaItems = episode.serverData?.mapNotNull {
            return@mapNotNull if (!it.linkMpd.isNullOrEmpty()) {
                DashMediaSource.Factory(DefaultHttpDataSource.Factory())
                    .createMediaSource(
                        MediaItem.Builder()
                            .setUri(it.linkMpd)
                            .setMimeType(MimeTypes.APPLICATION_MPD)
                            .build()
                    )
            } else if (!it.linkM3u8.isNullOrEmpty()) {
                HlsMediaSource.Factory(DefaultHttpDataSource.Factory())
                    .createMediaSource(
                        MediaItem.Builder()
                            .setUri(it.linkM3u8)
                            .setMimeType(MimeTypes.APPLICATION_M3U8)
                            .build()
                    )
            } else null
        }
        if (!mediaItems.isNullOrEmpty()) {
            withContext(Dispatchers.Main) {
                val maxIndex = mediaItems.size - 1
                exoPlayer.setMediaSources(mediaItems)
                exoPlayer.seekTo(idx, position)
                _playerIndex.value = Triple(episode, idx, maxIndex)
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
private fun OMovieFields(detail: OMovieDetailResponse.OMovieDetail, modifier: Modifier) {
    val default = stringResource(id = R.string.default_value)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            ValueField(
                "Năm sản xuất",
                detail.year.ifNull(default),
            )
            ValueField("Trạng thái", detail.episodeCurrent ?: default)
            ValueField("Số tập", detail.episodeTotal ?: default)
            ValueField("Thời lượng", detail.time ?: default)
        }

        Spacer(modifier = Modifier.height(6.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = modifier) {
            ValueField("Chất lượng", detail.quality ?: default)
            ValueField("Ngôn ngữ", detail.lang ?: default)
            ValueField("Lượt xem", detail.view.ifNull(default))
        }
    }
}












