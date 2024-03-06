package com.android.movieapp.ui.detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.painter.ColorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.android.movieapp.R
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.network.Cast
import com.android.movieapp.models.network.Crew
import com.android.movieapp.models.network.GenreItemResponse
import com.android.movieapp.models.network.ImageResponse
import com.android.movieapp.models.network.Keyword
import com.android.movieapp.models.network.MovieDetail
import com.android.movieapp.models.network.PersonDetail
import com.android.movieapp.models.network.ProductionCompany
import com.android.movieapp.models.network.TvDetail
import com.android.movieapp.models.network.Video
import com.android.movieapp.network.Api
import com.android.movieapp.ui.ext.BottomArcShape
import com.android.movieapp.ui.ext.CircleGlowingImage
import com.android.movieapp.ui.ext.ProgressiveGlowingImage
import com.android.movieapp.ui.ext.dpToPx
import com.android.movieapp.ui.ext.ifNull
import com.android.movieapp.ui.ext.ifNullOrEmpty
import com.android.movieapp.ui.ext.openChromeCustomTab
import com.android.movieapp.ui.ext.springAnimation
import com.android.movieapp.ui.theme.AppYellow
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlin.math.roundToInt

@Composable
fun TvFields(tv: Tv, detail: TvDetail?, modifier: Modifier) {
    val default = stringResource(id = R.string.default_value)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            ValueField(
                stringResource(R.string.first_air_date),
                tv.firstAirDate.ifNullOrEmpty(default)
            )
            ValueField(stringResource(R.string.vote_average), tv.voteAverage.ifNull(default))
            ValueField(stringResource(R.string.votes), tv.voteCount.ifNull(default))
            ValueField(stringResource(R.string.popularity), tv.popularity.ifNull(default))
        }
        detail?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = modifier) {
                ValueField(stringResource(R.string.runtime), it.episodeRunTime.toString())
                ValueField(stringResource(R.string.status), it.status ?: default)
                ValueField(stringResource(R.string.type), it.type ?: default)
            }
        }
    }
}

@Composable
fun TvSeason(
    season: TvDetail.Season,
    overviewClick: () -> Unit
) {
    Row {
        Column(
            modifier = Modifier.width(180.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ProgressiveGlowingImage(
                Api.getPosterPath(season.posterPath),
                true
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "${season.name}",
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "(${season.airDate ?: stringResource(id = R.string.default_value)})",
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(1.dp)
            )
            Text(
                text = "${season.episodeCount ?: stringResource(id = R.string.default_value)} episode",
                color = MaterialTheme.colorScheme.primary,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(1.dp)
            )
        }
        if (!season.overview.isNullOrEmpty()) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = season.overview,
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(fontSize = 14.sp),
                modifier = Modifier
                    .widthIn(max = 300.dp)
                    .padding(2.dp)
                    .clickable {
                        overviewClick.invoke()
                    }
            )
        }
    }
}

@Composable
fun PersonFields(person: PersonDetail, modifier: Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            val default = stringResource(id = R.string.default_value)
            ValueField(stringResource(R.string.birth_day), person.birthday.ifNullOrEmpty(default))
            ValueField(stringResource(R.string.death_day), person.deathday.ifNullOrEmpty(default))
            ValueField(stringResource(R.string.popularity), person.popularity.ifNull(default))
        }
        person.placeOfBirth?.let {
            Spacer(modifier = Modifier.height(16.dp))
            ValueField(stringResource(R.string.place_of_birth), it)
        }
    }
}

@Composable
fun PersonProfile(url: String, modifier: Modifier) {
    val isScaled = remember { mutableStateOf(false) }
    val scale = animateFloatAsState(
        targetValue = if (isScaled.value) 1.6f else 1f,
        animationSpec = springAnimation,
        label = "scale"
    ).value

    Box(
        modifier = modifier
            .scale(scale)
            .clickable { isScaled.value = !isScaled.value },
    ) {
        CircleGlowingImage(
            url,
            true
        )
    }
}

@Composable
fun Backdrop(backdropUrl: String, modifier: Modifier) {
    Card(
        elevation = CardDefaults.cardElevation(16.dp),
        shape = BottomArcShape(arcHeight = 100.dpToPx()),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.background.copy(alpha = 0.1f)),
        modifier = modifier
    ) {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(backdropUrl)
                .allowHardware(true)
                .crossfade(true)
                .size(Size.ORIGINAL)
                .build(),
            error = ColorPainter(MaterialTheme.colorScheme.onSecondary)
        )
        Image(
            painter = painter,
            contentScale = ContentScale.FillWidth,
            contentDescription = "Backdrop image",
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    (painter.state as? AsyncImagePainter.State.Success)
                        ?.painter
                        ?.intrinsicSize
                        ?.let { intrinsicSize ->
                            val ratio = intrinsicSize.width / intrinsicSize.height
                            Modifier
                                .aspectRatio(ratio)
                        } ?: Modifier.aspectRatio(16 / 9f)
                )
        )
    }
}

@Composable
fun Title(title: String?, originalTitle: String?, modifier: Modifier) {
    val context = LocalContext.current
    Column(modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = title ?: "",
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                shadow = Shadow(
                    color = MaterialTheme.colorScheme.secondary,
                    offset = Offset(0f, 0f),
                    blurRadius = 0.5f
                )
            ),
            modifier = Modifier.clickable {
                val clipboard =
                    context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                val clip = ClipData.newPlainText(
                    title, title
                )
                clipboard.setPrimaryClip(clip)
                context.openChromeCustomTab("https://bard.google.com/chat")
            }
        )
        if (!originalTitle.isNullOrBlank() && title != originalTitle) {
            Text(
                text = "( $originalTitle )",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    fontSize = 16.sp,
                    fontStyle = FontStyle.Italic,
                    textAlign = TextAlign.Center,
                    shadow = Shadow(
                        color = MaterialTheme.colorScheme.secondary,
                        offset = Offset(0f, 0f),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier.clickable {
                    context.openChromeCustomTab("https://www.google.com/search?q=$title($originalTitle)")
                }
            )
        }
    }
}

@Composable
fun GenreChips(
    genres: List<GenreItemResponse>,
    modifier: Modifier,
    onClick: (GenreItemResponse) -> Unit
) {
    Row(
        modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        genres.forEachIndexed { index, genre ->
            Text(
                text = genre.name ?: "",
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
                    .clickable { onClick.invoke(genre) },
            )

            if (index != genres.lastIndex) {
                Spacer(modifier = Modifier.width(8.dp))
            }
        }
    }
}

@Keep
enum class SocialType(@DrawableRes val res: Int) {
    Facebook(R.drawable.ic_facebook),
    IMDb(R.drawable.ic_imdb),
    Instagram(R.drawable.ic_instagram),
    Twitter(R.drawable.ic_twitter),
    Wikipedia(R.drawable.ic_wikipedia),
    Tiktok(R.drawable.ic_tiktok)
}

@Keep
data class SocialData(
    val type: SocialType,
    val id: String
)

@Composable
fun IdChips(
    socials: List<SocialData>,
    modifier: Modifier,
    onClick: (SocialData) -> Unit
) {
    Row(
        modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        socials.forEachIndexed { index, social ->
            Image(
                painter = painterResource(social.type.res),
                contentDescription = social.type.name,
                contentScale = ContentScale.FillWidth,
                modifier = Modifier
                    .width(36.dp)
                    .clickable {
                        onClick.invoke(social)
                    }
            )
            if (index != socials.size - 1) Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

@Composable
fun ActionChips(
    actionState: List<Boolean>,
    modifier: Modifier,
    onClick: (Action) -> Unit
) {
    Row(
        modifier
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Action.entries.forEachIndexed { index, action ->
            if (actionState[index]) CircularProgressIndicator(
                Modifier
                    .width(36.dp)
            )
            else Image(
                painter = painterResource(action.res),
                contentDescription = action.name,
                contentScale = ContentScale.FillWidth,
                colorFilter = ColorFilter.tint(MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .width(36.dp)
                    .clickable {
                        onClick.invoke(action)
                    }
            )
            if (index != Action.entries.size - 1) Spacer(modifier = Modifier.width(12.dp))
        }
    }
}

@Composable
fun MovieFields(movie: Movie, detail: MovieDetail?, modifier: Modifier) {
    val default = stringResource(id = R.string.default_value)
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
            ValueField(
                stringResource(R.string.release_date),
                movie.releaseDate.ifNullOrEmpty(default)
            )
            ValueField(stringResource(R.string.vote_average), movie.voteAverage.ifNull(default))
            ValueField(stringResource(R.string.votes), movie.voteCount.ifNull(default))
            ValueField(stringResource(R.string.popularity), movie.popularity.ifNull(default))
        }
        detail?.let {
            Spacer(modifier = Modifier.height(6.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp), modifier = modifier) {
                ValueField(stringResource(R.string.budget), it.budget.ifNull(default))
                ValueField(stringResource(R.string.revenue), it.revenue.ifNull(default))
                ValueField(stringResource(R.string.runtime), it.runtime.ifNull(default))
                ValueField(stringResource(R.string.status), it.status ?: default)
            }
        }
    }
}


@Composable
fun ValueField(name: String, value: String?) {
    Column {
        Text(
            text = name,
            color = MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium.copy(fontSize = 13.sp),
            modifier = Modifier.align(Alignment.CenterHorizontally),
        )
        Text(
            text = value ?: "",
            color = AppYellow,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(1f, 2f),
                    blurRadius = 0.5f
                )
            ),
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(top = 4.dp),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun <T : Any> SectionView(
    items: List<T>,
    @StringRes headerResId: Int,
    itemContent: @Composable (T, Int) -> Unit,
    modifier: Modifier,
    header: String? = null,
    color: Color? = null
) {
    Column(modifier = modifier.fillMaxWidth()) {
        if (items.isNotEmpty()) {
            SectionHeader(header ?: stringResource(headerResId), items.size, color)
            LazyRow(
                modifier = Modifier.testTag(LocalContext.current.getString(headerResId)),
                contentPadding = PaddingValues(16.dp),
            ) {
                items(
                    count = items.size,
                    itemContent = { index ->
                        itemContent(items[index], index)
                        Spacer(modifier = Modifier.width(16.dp))
                    },
                )
            }
        }
    }
}


@Composable
private fun SectionHeader(
    header: String,
    count: Int,
    color: Color? = null
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp),
    ) {
        Text(
            text = "$header ($count)",
            color = color ?: MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                shadow = Shadow(
                    color = MaterialTheme.colorScheme.secondary,
                    offset = Offset(0f, 0f),
                    blurRadius = 0.5f
                )
            )
        )
    }
}

@Composable
fun CastItemView(
    modifier: Modifier = Modifier,
    cast: Cast,
    onExpandDetails: (Cast) -> Unit
) {
    Column(
        modifier = modifier
            .padding(6.dp)
            .clickable {
                onExpandDetails.invoke(cast)
            }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val posterUrl = Api.getPosterPath(cast.profilePath)
        CircleGlowingImage(
            url = posterUrl,
            glow = true
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = cast.name ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 1f
                )
            ),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            maxLines = 2,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = cast.character ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 1f
                )
            ),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            maxLines = 2,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun CrewItemView(
    modifier: Modifier = Modifier,
    crew: Crew,
    onExpandDetails: (Crew) -> Unit
) {
    Column(
        modifier = modifier
            .padding(6.dp)
            .clickable {
                onExpandDetails.invoke(crew)
            }, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val posterUrl = Api.getPosterPath(crew.profilePath)
        CircleGlowingImage(
            url = posterUrl,
            glow = true
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = crew.name ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 1f
                )
            ),
            color = MaterialTheme.colorScheme.primary,
            fontSize = 13.sp,
            maxLines = 2,
            textAlign = TextAlign.Center,
            lineHeight = 14.sp,
            overflow = TextOverflow.Ellipsis
        )
        Text(
            text = crew.job ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 1f
                )
            ),
            color = MaterialTheme.colorScheme.secondary,
            fontSize = 12.sp,
            maxLines = 2,
            lineHeight = 14.sp,
            textAlign = TextAlign.Center,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun DetailImage(image: ImageResponse, onClick: (ImageResponse) -> Unit) {
    val width by remember {
        mutableIntStateOf((200 * (image.aspectRatio ?: 1.0)).roundToInt())
    }
    Box(modifier = Modifier
        .width(width.dp)
        .clickable {
            onClick.invoke(image)
        }) {
        ProgressiveGlowingImage(
            Api.getOriginalPath(image.filePath),
            true,
            image.aspectRatio?.toFloat() ?: 1f
        )
    }
}

@Composable
fun VideoThumbnail(
    video: Video,
    lifecycleOwner: LifecycleOwner
) {
    AndroidView(
        modifier = Modifier
            .width(320.dp)
            .clip(RoundedCornerShape(12.dp)),
        factory = { context ->
            YouTubePlayerView(context = context).apply {
                lifecycleOwner.lifecycle.addObserver(this)
                addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                    override fun onReady(youTubePlayer: YouTubePlayer) {
                        video.key?.let { youTubePlayer.cueVideo(it, 0f) }
                    }
                })
            }
        })
}


@Composable
fun ProductionCompanyView(
    productionCompany: ProductionCompany,
    onExpandDetails: (ProductionCompany) -> Unit
) {
    if (productionCompany.logoPath.isNullOrEmpty()) {
        Text(
            text = productionCompany.name ?: "",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.primary,
            overflow = TextOverflow.Ellipsis,
            style = MaterialTheme.typography.bodyMedium.copy(
                fontSize = 16.sp
            ),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(120.dp)
                .clickable(
                    onClick = {
                        onExpandDetails.invoke(productionCompany)
                    }
                )
        )

    } else {
        val painter = rememberAsyncImagePainter(
            model = ImageRequest.Builder(LocalContext.current)
                .data(Api.getOriginalPath(productionCompany.logoPath))
                .allowHardware(true)
                .crossfade(true)
                .size(Size.ORIGINAL)
                .build(),
            error = ColorPainter(MaterialTheme.colorScheme.onSecondary)
        )

        Image(
            painter = painter,
            contentDescription = null,
            contentScale = ContentScale.FillWidth,
            modifier = Modifier
                .width(100.dp)
                .clickable(
                    onClick = {
                        onExpandDetails.invoke(productionCompany)
                    }
                )
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun KeywordLayout(keywords: List<Keyword>, modifier: Modifier, onClick: (Keyword) -> Unit) {
    Column(modifier = modifier) {
        if (keywords.isNotEmpty()) {
            SectionHeader(stringResource(id = R.string.keywords), keywords.size)
            FlowRow(modifier = Modifier.padding(vertical = 8.dp, horizontal = 16.dp)) {
                keywords.forEach {
                    Text(
                        text = it.name ?: "",
                        color = MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 0f),
                                blurRadius = 0.5f
                            )
                        ),
                        modifier = Modifier
                            .padding(end = 6.dp, bottom = 6.dp)
                            .background(
                                MaterialTheme.colorScheme.onSecondary,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                            .clickable {
                                onClick.invoke(it)
                            },
                    )
                }
            }
        }
    }
}

@Composable
fun Poster(posterUrl: String, modifier: Modifier) {
    val isScaled = remember { mutableStateOf(false) }
    val scale = animateFloatAsState(
        targetValue = if (isScaled.value) 1.6f else 1f,
        animationSpec = springAnimation,
        label = "scale"
    ).value

    Card(
        elevation = CardDefaults.cardElevation(5.dp),
        modifier = modifier
            .scale(scale)
            .clickable { isScaled.value = !isScaled.value },
    ) {
        ProgressiveGlowingImage(
            posterUrl,
            true
        )
    }
}


enum class Action(@DrawableRes val res: Int) {
    GenerativeModel(R.drawable.ic_google),
    GG_TRANSLATE(R.drawable.ic_language),
    GPT_TRANSLATE(R.drawable.ic_chatgpt),
    GG_CHAT(R.drawable.ic_google),
    GPT_CHAT(R.drawable.ic_chatgpt)
}



