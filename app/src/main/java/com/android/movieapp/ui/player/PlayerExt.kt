package com.android.movieapp.ui.player

import android.annotation.TargetApi
import android.app.Activity
import android.app.PendingIntent
import android.app.PictureInPictureParams
import android.app.RemoteAction
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.graphics.drawable.Icon
import android.os.Build
import android.os.Build.VERSION.SDK_INT
import android.os.Handler
import android.util.Rational
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.media3.common.C
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.audio.AudioRendererEventListener
import androidx.media3.exoplayer.metadata.MetadataOutput
import androidx.media3.exoplayer.text.TextOutput
import androidx.media3.exoplayer.text.TextRenderer
import androidx.media3.exoplayer.trackselection.DefaultTrackSelector
import androidx.media3.exoplayer.video.VideoRendererEventListener
import com.android.movieapp.R
import com.android.movieapp.ui.media.renderer.CustomTextRenderer

const val ACTION_PIP_CONTROL = "player_pip_control"
const val PLAYER_PIP_EVENT = "player_pip_event"

enum class PlayerEvents {
    PLAY,
    PAUSE;

    companion object {
        fun fromInt(value: Int): PlayerEvents = entries.first { it.ordinal == value }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun Activity.getRemoteAction(
    @DrawableRes icon: Int,
    @StringRes label: Int,
    event: PlayerEvents,
): RemoteAction {
    val text = getString(label)
    return RemoteAction(
        /* icon = */ Icon.createWithResource(this, icon),
        /* title = */ text,
        /* contentDescription = */ text,
        /* intent = */ getPendingIntent(event.ordinal)
    )
}

private fun Activity.getPendingIntent(event: Int): PendingIntent {
    return PendingIntent.getBroadcast(
        /* context = */ this,
        /* requestCode = */ event,
        /* intent = */ Intent(ACTION_PIP_CONTROL).apply {
            putExtra(PLAYER_PIP_EVENT, event)
        },
        /* flags = */ PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
}

fun Activity.sendPauseBroadcast() {
    val intent = Intent(ACTION_PIP_CONTROL).apply {
        putExtra(PLAYER_PIP_EVENT, PlayerEvents.PAUSE.ordinal)
    }
    sendBroadcast(intent)
}

@TargetApi(Build.VERSION_CODES.O)
fun Activity.updatePiPParams(
    mediaState: MediaState
): PictureInPictureParams {
    val params = with(PictureInPictureParams.Builder()) {
        val width = 16
        val height = 9
        setAspectRatio(Rational(width, height))
        val remoteActions = mutableListOf<RemoteAction>()
        when (mediaState) {
            is MediaState.Playing -> remoteActions.add(
                if (mediaState.isPlay) getRemoteAction(
                    R.drawable.baseline_pause_24,
                    R.string.pause,
                    PlayerEvents.PAUSE
                )
                else getRemoteAction(
                    R.drawable.baseline_play_arrow_24,
                    R.string.play,
                    PlayerEvents.PLAY
                )
            )

            else -> {}
        }

        setActions(remoteActions)
        build()
    }

    setPictureInPictureParams(params)
    return params
}

@androidx.annotation.OptIn(UnstableApi::class)
fun ExoPlayer.isEnableSelect(type: Int): Boolean {
    var count = 0
    for (group in currentTracks.groups) {
        if (group.type == type) {
            count += group.mediaTrackGroup.length
        }
    }
    return count > 0
}

@androidx.annotation.OptIn(UnstableApi::class)
internal fun Context.getRenderers(
    eventHandler: Handler,
    videoRendererEventListener: VideoRendererEventListener,
    audioRendererEventListener: AudioRendererEventListener,
    textRendererOutput: TextOutput,
    metadataRendererOutput: MetadataOutput,
    subtitleOffset: Long,
    onTextRendererChange: (CustomTextRenderer) -> Unit,
): Array<Renderer> {
    return DefaultRenderersFactory(this)
        .createRenderers(
            eventHandler,
            videoRendererEventListener,
            audioRendererEventListener,
            textRendererOutput,
            metadataRendererOutput
        ).map {
            if (it is TextRenderer) {
                CustomTextRenderer(
                    offset = subtitleOffset,
                    output = textRendererOutput,
                    outputLooper = eventHandler.looper,
                ).also(onTextRendererChange)
            } else it
        }.toTypedArray()
}

@androidx.annotation.OptIn(UnstableApi::class)
fun Context.buildExoplayer(): ExoPlayer.Builder {
    val loadControl = DefaultLoadControl.Builder()
        .setBufferDurationsMs(32 * 1024, 64 * 1024, 10 * 1024, 10 * 1024)
        .build()

    return ExoPlayer.Builder(this).apply {
        setTrackSelector(DefaultTrackSelector(this@buildExoplayer).apply {
            setParameters(
                buildUponParameters()
                    .setPreferredTextLanguage("vi")
                    .setForceHighestSupportedBitrate(true)
            )
        })
        setHandleAudioBecomingNoisy(true)
        setLoadControl(loadControl)
        setSeekBackIncrementMs(10000L)
        setSeekForwardIncrementMs(10000L)
        setWakeMode(C.WAKE_MODE_NETWORK)
    }
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

@Composable
internal fun PlayerPipReceiver(
    action: String,
    onReceive: (intent: Intent?) -> Unit,
) {
    val context = LocalContext.current
    val currentOnReceive by rememberUpdatedState(newValue = onReceive)

    DisposableEffect(context, action) {
        val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(
                context: Context?,
                intent: Intent,
            ) {
                currentOnReceive(intent)
            }
        }

        if (SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(
                broadcastReceiver, IntentFilter(action),
                Context.RECEIVER_EXPORTED,
            )
        } else {
            context.registerReceiver(
                broadcastReceiver, IntentFilter(action),
            )
        }

        onDispose {
            context.unregisterReceiver(broadcastReceiver)
        }
    }
}