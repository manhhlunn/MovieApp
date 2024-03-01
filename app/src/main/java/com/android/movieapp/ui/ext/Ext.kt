package com.android.movieapp.ui.ext

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.util.Log
import android.view.View
import androidx.annotation.OptIn
import androidx.browser.customtabs.CustomTabColorSchemeParams
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
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
import com.android.movieapp.network.Api
import com.android.movieapp.network.service.SSLTrustManager
import com.android.movieapp.ui.media.renderer.CustomTextRenderer
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.generationConfig
import com.mannan.translateapi.Language
import com.mannan.translateapi.TranslateAPI
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.lang.reflect.Field
import java.math.RoundingMode
import java.net.URL
import java.security.SecureRandom
import java.text.DecimalFormat
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine


@Composable
fun Int.dpToPx(): Float {
    val density = LocalDensity.current.density
    return remember(this) { this * density }
}

fun String?.ifNullOrEmpty(default: String): String {
    return if (isNullOrEmpty()) default else this
}

fun Number?.ifNull(default: String): String {
    return this?.toString() ?: default
}

fun String.sub(idx: Int): String {
    if (this.length <= idx) {
        return this
    }
    return this.substring(0, idx) + "..."
}

fun Context.openChromeCustomTab(url: String) {
    val schemeParams = CustomTabColorSchemeParams.Builder().build()
    val customTabsIntent =
        CustomTabsIntent.Builder().setDefaultColorSchemeParams(schemeParams).build().apply {
            intent.putExtra("com.google.android.apps.chrome.EXTRA_OPEN_NEW_INCOGNITO_TAB", true)
        }
    customTabsIntent.launchUrl(this, Uri.parse(url))
}

@Composable
fun getColumnCount(): Int {
    val configuration = LocalConfiguration.current
    return (configuration.screenWidthDp / 120)
}

suspend fun translateToVi(text: String) = suspendCoroutine {
    val translateAPI = TranslateAPI(
        Language.AUTO_DETECT,
        Language.VIETNAMESE,
        text
    )

    translateAPI.setTranslateListener(object : TranslateAPI.TranslateListener {
        override fun onSuccess(translatedText: String?) {
            it.resume(translatedText)
        }

        override fun onFailure(errorText: String?) {
            it.resume(null)
        }
    })
}

suspend fun makeGPTSummary(name: String) =
    makeGPTRequest("Plot summary of $name")

suspend fun makeGenerativeModelChatSummary(name: String) =
    makeGenerativeModelChatRequest("Plot summary of $name")

suspend fun makeGPTTranslate(content: String) =
    makeGPTRequest("Translate the following content into Vietnamese: $content.")

suspend fun makeGenerativeModelChatTranslate(content: String) =
    makeGenerativeModelChatRequest("Translate the following content into Vietnamese: $content.")


suspend fun makeGPTRequest(prompt: String): String? = withContext(Dispatchers.IO) {
    try {
        val url =
            "https://api.openai.com/v1/chat/completions"
        val jsonObject = JSONObject()
        val message = JSONObject()
        message.put("role", "user")
        message.put(
            "content",
            prompt
        )
        val messages = JSONArray()
        messages.put(message)
        jsonObject.put("model", "gpt-3.5-turbo-1106")
        jsonObject.put("messages", messages)
        val requestBody = jsonObject.toString()
            .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder().url(url)
            .post(requestBody)
            .header("Content-Type", "application/json")
            .header(
                "Authorization",
                "Bearer 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"
            )
            .build()
        Log.d("BBB", "makeGPTRequest:${jsonObject}")
        val client = OkHttpClient().newBuilder().apply {
            connectTimeout(10, TimeUnit.MINUTES)
            writeTimeout(10, TimeUnit.MINUTES)
            readTimeout(10, TimeUnit.MINUTES)
        }.build()
        val jsonString =
            client.newCall(request).execute().body?.string() ?: return@withContext null
        Log.d("BBB", "makeGPTRequest:${jsonString}")
        val json = JSONObject(jsonString)
        json.getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    } catch (e: Exception) {
        Log.d("BBB", "makeGPTRequest:${e.message}")
        null
    }
}


suspend fun Context.makeWikiRequest(id: String) = withContext(Dispatchers.IO) {
    try {
        val json =
            URL("https://www.wikidata.org/w/api.php?action=wbgetentities&format=json&props=sitelinks&ids=$id&sitefilter=enwiki").readText()
        val jsonObject = JSONObject(json)
        val entitiesObject = jsonObject.getJSONObject("entities")
        val q851095Object = entitiesObject.getJSONObject(id)
        val siteLinksObject = q851095Object.getJSONObject("sitelinks")
        val enWikiObject = siteLinksObject.getJSONObject("enwiki")
        val title = enWikiObject.getString("title")
        openChromeCustomTab("https://en.wikipedia.org/wiki/$title")
    } catch (e: Exception) {
        Log.d("BBB", "makeWikiRequest:${e.message}")
    }
}

fun makeGenerativeModelFlow(
    name: String
): Flow<String> {
    val config = generationConfig {
        temperature = 0f
    }

    val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = Api.AI_API_KEY,
        config
    )

    var fullResponse = ""
    return generativeModel.generateContentStream("Summary of information including review, score, genre, characters, full plot, and type of ending of $name")
        .map { chunk ->
            fullResponse += chunk.text
            fullResponse
        }
}

suspend fun makeGenerativeModelChatRequest(
    content: String
): String? {
    val config = generationConfig {
        temperature = 0f
    }

    val generativeModel = GenerativeModel(
        modelName = "gemini-pro",
        apiKey = Api.AI_API_KEY,
        config
    )

    val chat = generativeModel.startChat()
    return chat.sendMessage(content).text
}

fun Context.setScreenOrientation(orientation: Int) {
    val activity = this.findActivity() ?: return
    activity.requestedOrientation = orientation
}


fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}

@Composable
fun OnLifecycleEvent(onEvent: (owner: LifecycleOwner, event: Lifecycle.Event) -> Unit) {
    val eventHandler = rememberUpdatedState(onEvent)
    val lifecycleOwner = rememberUpdatedState(LocalLifecycleOwner.current)

    DisposableEffect(lifecycleOwner.value) {
        val lifecycle = lifecycleOwner.value.lifecycle
        val observer = LifecycleEventObserver { owner, event ->
            eventHandler.value(owner, event)
        }

        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }
}

fun Long.makeTimeString(): String {
    if (this < 0) return ""
    var sec = this / 1000
    val day = sec / 86400
    sec %= 86400
    val hour = sec / 3600
    sec %= 3600
    val minute = sec / 60
    sec %= 60
    return when {
        day > 0 -> "%d:%02d:%02d:%02d".format(day, hour, minute, sec)
        hour > 0 -> "%d:%02d:%02d".format(hour, minute, sec)
        else -> "%d:%02d".format(minute, sec)
    }
}

suspend fun <T, R> Iterable<T>.mapAsync(
    mapper: suspend (T) -> R
): List<R> = coroutineScope { map { async { mapper(it) } }.awaitAll() }

fun <A, B> List<A>.asyncMapIndexed(f: suspend (index: Int, A) -> B): List<B> = runBlocking {
    mapIndexed { index, a -> async { f(index, a) } }.map { it.await() }
}

fun Any.string(): String {
    val result = StringBuilder()
    val newLine = System.getProperty("line.separator")
    result.append(this.javaClass.name)
    result.append(" Object {")
    result.append(newLine)

    //determine fields declared in this class only (no fields of superclass)
    val fields: Array<Field> = this.javaClass.declaredFields

    //print field names paired with their values
    for (field in fields) {
        result.append("  ")
        try {
            result.append(field.name)
            result.append(": ")
            result.append(field.get(this))
        } catch (ex: IllegalAccessException) {
            println(ex)
        }
        result.append(newLine)
    }
    result.append("}")
    return result.toString()
}

fun Double.roundOffDecimal(): String {
    val df = DecimalFormat("#.#")
    df.roundingMode = RoundingMode.CEILING
    return df.format(this)
}

@UnstableApi
fun ExoPlayer.isEnableSelect(type: Int): Boolean {
    var count = 0
    for (group in currentTracks.groups) {
        if (group.type == type) {
            count += group.mediaTrackGroup.length
        }
    }
    return count > 0
}

@OptIn(UnstableApi::class)
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

@OptIn(UnstableApi::class)
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

fun OkHttpClient.Builder.ignoreAllSSLErrors(): OkHttpClient.Builder {
    val naiveTrustManager = SSLTrustManager()

    val insecureSocketFactory = SSLContext.getInstance("SSL").apply {
        val trustAllCerts = arrayOf<TrustManager>(naiveTrustManager)
        init(null, trustAllCerts, SecureRandom())
    }.socketFactory

    sslSocketFactory(insecureSocketFactory, naiveTrustManager)
    hostnameVerifier { _, _ -> true }
    return this
}

