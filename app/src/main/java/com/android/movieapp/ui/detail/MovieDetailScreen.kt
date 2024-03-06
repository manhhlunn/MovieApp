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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import androidx.compose.ui.platform.LocalLifecycleOwner
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
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import coil.request.ImageRequest
import coil.size.Size
import com.android.movieapp.NavScreen
import com.android.movieapp.R
import com.android.movieapp.models.entities.FavoriteMovie
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.WatchedMovie
import com.android.movieapp.models.network.Cast
import com.android.movieapp.models.network.Crew
import com.android.movieapp.models.network.GenreItemResponse
import com.android.movieapp.models.network.ImageResponse
import com.android.movieapp.models.network.Keyword
import com.android.movieapp.models.network.MovieDetail
import com.android.movieapp.models.network.ProductionCompany
import com.android.movieapp.models.network.Video
import com.android.movieapp.models.network.toPerson
import com.android.movieapp.network.Api
import com.android.movieapp.repository.ConfigureRepository
import com.android.movieapp.repository.FavoriteRepository
import com.android.movieapp.repository.MovieRepository
import com.android.movieapp.ui.ext.BottomArcShape
import com.android.movieapp.ui.ext.CircleGlowingImage
import com.android.movieapp.ui.ext.ProgressiveGlowingImage
import com.android.movieapp.ui.ext.dpToPx
import com.android.movieapp.ui.ext.ifNull
import com.android.movieapp.ui.ext.ifNullOrEmpty
import com.android.movieapp.ui.ext.makeGPTSummary
import com.android.movieapp.ui.ext.makeGPTTranslate
import com.android.movieapp.ui.ext.makeGenerativeModelChatSummary
import com.android.movieapp.ui.ext.makeGenerativeModelChatTranslate
import com.android.movieapp.ui.ext.makeGenerativeModelFlow
import com.android.movieapp.ui.ext.makeWikiRequest
import com.android.movieapp.ui.ext.openChromeCustomTab
import com.android.movieapp.ui.ext.springAnimation
import com.android.movieapp.ui.ext.translateToVi
import com.android.movieapp.ui.theme.AppYellow
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt


@Composable
fun MovieDetailScreen(
    navController: NavController,
    viewModel: MovieDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    Box(contentAlignment = Alignment.Center) {
        val movie = uiState.movie
        if (movie != null) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                val (backIcon, favIcon, watched, backdrop, poster, title, ids, genres, specs, overview, actions) = createRefs()
                val (castSection, crewSection, imagesSection, videos, companies, keywords) = createRefs()
                val startGuideline = createGuidelineFromStart(16.dp)
                val endGuideline = createGuidelineFromEnd(16.dp)

                Backdrop(
                    backdropUrl = Api.getBackdropPath(movie.backdropPath),
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

                IconButton(onClick = {
                    viewModel.onFavoriteChange()
                }, modifier = Modifier
                    .constrainAs(favIcon) {
                        start.linkTo(poster.end)
                        top.linkTo(backdrop.bottom)
                        end.linkTo(parent.end)
                    })
                {
                    Icon(
                        Icons.Rounded.Favorite,
                        contentDescription = "Favorite",
                        modifier = Modifier.scale(1.2f),
                        tint = if (uiState.isFavorite) Color.Red else MaterialTheme.colorScheme.secondary
                    )
                }

                IconButton(onClick = {
                    viewModel.onWatchedChange()
                }, modifier = Modifier
                    .constrainAs(watched) {
                        start.linkTo(favIcon.start)
                        top.linkTo(favIcon.bottom, margin = 20.dp)
                        end.linkTo(favIcon.end)
                    })
                {
                    Icon(
                        painterResource(id = R.drawable.ic_lib),
                        contentDescription = "Watched",
                        tint = if (uiState.isWatched) Color.Blue else MaterialTheme.colorScheme.secondary
                    )
                }

                Poster(posterUrl = Api.getPosterPath(movie.posterPath), modifier = Modifier
                    .width(120.dp)
                    .padding(top = 120.dp)
                    .constrainAs(poster) {
                        centerAround(backdrop.bottom)
                        linkTo(startGuideline, endGuideline)
                    })

                Title(
                    movie.title,
                    movie.originalTitle,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .constrainAs(title) {
                            top.linkTo(poster.bottom)
                            linkTo(start = startGuideline, end = endGuideline)
                        })

                IdChips(
                    uiState.ids,
                    modifier = Modifier.constrainAs(ids) {
                        top.linkTo(title.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    }
                ) { social ->
                    if (social.type == SocialType.Wikipedia) {
                        scope.launch {
                            context.makeWikiRequest(social.id)
                        }
                    } else context.openChromeCustomTab(
                        when (social.type) {
                            SocialType.Facebook -> "https://www.facebook.com/${social.id}"
                            SocialType.IMDb -> "https://www.imdb.com/title/${social.id}"
                            SocialType.Instagram -> "https://www.instagram.com/${social.id}"
                            SocialType.Twitter -> "https://twitter.com/${social.id}"
                            SocialType.Tiktok -> "https://www.tiktok.com/@${social.id}"
                            else -> return@IdChips
                        }
                    )
                }

                GenreChips(
                    uiState.genres,
                    modifier = Modifier.constrainAs(genres) {
                        top.linkTo(ids.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                ) {
                    navController.navigate(
                        NavScreen.KeyDetailScreen.navigateWithArgument(
                            NavScreen.KeyDetailScreen.KeyDetail(
                                name = it.name,
                                genre = it.id
                            )
                        )
                    )
                }

                MovieFields(
                    movie,
                    uiState.detail,
                    modifier = Modifier.constrainAs(specs) {
                        top.linkTo(genres.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                )

                Text(
                    text = movie.overview ?: "",
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
                        .clickable {
                            viewModel.translateOverview()
                        }
                )

                ActionChips(modifier = Modifier.constrainAs(actions) {
                    top.linkTo(overview.bottom, 16.dp)
                    linkTo(startGuideline, endGuideline)
                }, actionState = uiState.actionState) {
                    viewModel.actionDetail(it)
                }

                SectionView(
                    items = uiState.cast,
                    headerResId = R.string.cast,
                    itemContent = { item, _ ->
                        CastItemView(modifier = Modifier.width(140.dp), item) {
                            navController.navigate(
                                NavScreen.PersonDetailScreen.navigateWithArgument(
                                    it.toPerson()
                                )
                            )
                        }
                    },
                    modifier = Modifier.constrainAs(castSection) {
                        top.linkTo(actions.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                )

                SectionView(
                    items = uiState.crew,
                    headerResId = R.string.crew,
                    itemContent = { item, _ ->
                        CrewItemView(modifier = Modifier.width(140.dp), item) {
                            navController.navigate(
                                NavScreen.PersonDetailScreen.navigateWithArgument(
                                    it.toPerson()
                                )
                            )
                        }
                    },
                    modifier = Modifier.constrainAs(crewSection) {
                        top.linkTo(castSection.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                )

                SectionView(
                    items = uiState.images,
                    headerResId = R.string.images,
                    itemContent = { item, _ ->
                        DetailImage(item) {
                            navController.navigate(
                                NavScreen.PreviewImageDialog.navigateWithArgument(
                                    it
                                )
                            )
                        }
                    },
                    modifier = Modifier.constrainAs(imagesSection) {
                        top.linkTo(crewSection.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                )

                SectionView(
                    items = uiState.videos,
                    headerResId = R.string.trailers,
                    itemContent = { item, _ ->
                        VideoThumbnail(item, lifecycleOwner)
                    },
                    modifier = Modifier.constrainAs(videos) {
                        top.linkTo(imagesSection.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    }
                )

                SectionView(
                    items = uiState.companies,
                    headerResId = R.string.companies,
                    itemContent = { item, _ ->
                        ProductionCompanyView(item) {
                            navController.navigate(
                                NavScreen.KeyDetailScreen.navigateWithArgument(
                                    NavScreen.KeyDetailScreen.KeyDetail(
                                        name = it.name,
                                        company = it.id
                                    )
                                )
                            )
                        }
                    },
                    modifier = Modifier.constrainAs(companies) {
                        top.linkTo(videos.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                )

                KeywordLayout(
                    keywords = uiState.keywords,
                    modifier = Modifier.constrainAs(keywords) {
                        top.linkTo(companies.bottom, 16.dp)
                        bottom.linkTo(parent.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    }) {
                    navController.navigate(
                        NavScreen.KeyDetailScreen.navigateWithArgument(
                            NavScreen.KeyDetailScreen.KeyDetail(
                                name = it.name,
                                keyword = it.id
                            )
                        )
                    )
                }
            }
        } else {
            CircularProgressIndicator()
        }
    }
}


@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    movieRepository: MovieRepository,
    private val favoriteRepository: FavoriteRepository,
    private val configureRepository: ConfigureRepository
) : ViewModel() {

    fun onFavoriteChange() {
        viewModelScope.launch {
            uiState.value.movie?.let { movie ->
                val isFavorite = uiState.value.isFavorite
                if (isFavorite) favoriteRepository.deleteFavoriteMovie(movie.id ?: return@launch)
                else favoriteRepository.addFavoriteMovie(
                    FavoriteMovie(
                        adult = movie.adult,
                        backdropPath = movie.backdropPath,
                        genreIds = movie.genreIds,
                        id = movie.id,
                        originalLanguage = movie.originalLanguage,
                        originalTitle = movie.originalTitle,
                        overview = movie.overview,
                        popularity = movie.popularity,
                        posterPath = movie.posterPath,
                        releaseDate = movie.releaseDate,
                        title = movie.title,
                        video = movie.video,
                        voteAverage = movie.voteAverage,
                        voteCount = movie.voteCount
                    )
                )
                _uiState.update {
                    it.copy(
                        isFavorite = !isFavorite
                    )
                }
            }
        }
    }

    fun onWatchedChange() {
        viewModelScope.launch {
            uiState.value.movie?.let { movie ->
                val isWatched = uiState.value.isWatched
                if (isWatched) favoriteRepository.deleteWatchedMovie(movie.id ?: return@launch)
                else favoriteRepository.addWatchedMovie(
                    WatchedMovie(
                        adult = movie.adult,
                        backdropPath = movie.backdropPath,
                        genreIds = movie.genreIds,
                        id = movie.id,
                        originalLanguage = movie.originalLanguage,
                        originalTitle = movie.originalTitle,
                        overview = movie.overview,
                        popularity = movie.popularity,
                        posterPath = movie.posterPath,
                        releaseDate = movie.releaseDate,
                        title = movie.title,
                        video = movie.video,
                        voteAverage = movie.voteAverage,
                        voteCount = movie.voteCount
                    )
                )
                _uiState.update {
                    it.copy(
                        isWatched = !isWatched
                    )
                }
            }
        }
    }

    fun actionDetail(action: Action) {
        _uiState.update {
            val new = it.actionState.toMutableList()
            new[action.ordinal] = true
            it.copy(
                actionState = new
            )
        }
        when (action) {
            Action.GenerativeModel -> generativeModel()
            Action.GG_TRANSLATE -> translateAI(false)
            Action.GPT_TRANSLATE -> translateAI(true)
            Action.GG_CHAT -> summaryChatAI(false)
            Action.GPT_CHAT -> summaryChatAI(true)
        }
    }

    private fun translateAI(isGPT: Boolean) {
        viewModelScope.launch {
            uiState.value.movie?.overview?.let { overview ->
                val translate =
                    if (isGPT) makeGPTTranslate(overview) else makeGenerativeModelChatTranslate(
                        overview
                    )
                if (translate != null) {
                    _uiState.update {
                        val new = it.actionState.toMutableList()
                        new[if (isGPT) Action.GPT_TRANSLATE.ordinal else Action.GG_TRANSLATE.ordinal] =
                            false
                        it.copy(
                            movie = it.movie?.copy(
                                overview = translate
                            ),
                            actionState = new
                        )
                    }
                }
            }
        }
    }

    private fun summaryChatAI(isGPT: Boolean) {
        viewModelScope.launch {
            uiState.value.movie?.let { movie ->
                val ext = StringBuilder()
                val language = configureRepository.getLanguages()
                    .firstOrNull { it.iso6391 == movie.originalLanguage }?.englishName
                if (!language.isNullOrEmpty() && !movie.originalTitle.isNullOrEmpty()) {
                    ext.append("(")
                    ext.append("$language : ${movie.originalTitle}")
                }
                if (!movie.releaseDate.isNullOrEmpty()) {
                    ext.append(if (ext.isEmpty()) "(" else " - ")
                    ext.append(movie.releaseDate.substring(0..3))
                }
                if (ext.isNotEmpty()) ext.append(")")
                val name = "Movie ${movie.title} $ext"
                val output =
                    if (isGPT) makeGPTSummary(name) else makeGenerativeModelChatSummary(name)
                _uiState.update {
                    val new = it.actionState.toMutableList()
                    new[if (isGPT) Action.GPT_CHAT.ordinal else Action.GG_CHAT.ordinal] = false
                    it.copy(
                        movie = it.movie?.copy(
                            overview = output ?: it.movie.overview
                        ),
                        actionState = new
                    )
                }
            }
        }
    }

    private fun generativeModel() {
        viewModelScope.launch {
            uiState.value.movie?.let { movie ->
                val ext = StringBuilder()
                val language = configureRepository.getLanguages()
                    .firstOrNull { it.iso6391 == movie.originalLanguage }?.englishName
                if (!language.isNullOrEmpty() && !movie.originalTitle.isNullOrEmpty()) {
                    ext.append("(")
                    ext.append("$language : ${movie.originalTitle}")
                }
                if (!movie.releaseDate.isNullOrEmpty()) {
                    ext.append(if (ext.isEmpty()) "(" else " - ")
                    ext.append(movie.releaseDate.substring(0..3))
                }
                if (ext.isNotEmpty()) ext.append(")")
                val name = "Movie ${movie.title} $ext"
                makeGenerativeModelFlow(name)
                    .collect { value ->
                        _uiState.update {
                            val new = it.actionState.toMutableList()
                            new[Action.GenerativeModel.ordinal] = false
                            it.copy(
                                movie = it.movie?.copy(
                                    overview = value
                                ),
                                actionState = new
                            )
                        }
                    }
            }
        }
    }

    fun translateOverview() {
        viewModelScope.launch {
            uiState.value.movie?.overview?.let { overview ->
                val translate = translateToVi(overview)
                if (translate != null) {
                    _uiState.update {
                        it.copy(
                            movie = it.movie?.copy(
                                overview = translate
                            )
                        )
                    }
                }
            }
        }
    }

    private val _uiState = MutableStateFlow(UIStateMovieDetail())
    val uiState: StateFlow<UIStateMovieDetail> = _uiState

    init {
        viewModelScope.launch {
            savedStateHandle.get<Movie>(NavScreen.MovieDetailScreen.MOVIE_DETAIL)?.let { movie ->
                _uiState.update {
                    it.copy(
                        movie = movie
                    )
                }
                movie.id?.let { id ->
                    val detail = async { movieRepository.getMovieDetail(id) }
                    val credit = async { movieRepository.getMovieCredit(id) }
                    val isFavorite = async { favoriteRepository.isFavoriteMovie(id) }
                    val isWatched = async { favoriteRepository.isWatchedMovie(id) }
                    val images = async { movieRepository.getMovieImages(id) }
                    val keywords = async { movieRepository.getMovieKeywords(id) }
                    val videos = async { movieRepository.getMovieVideos(id) }
                    val ids = async { movieRepository.getIds(id) }
                    _uiState.update {
                        it.copy(
                            cast = credit.await()?.cast ?: emptyList(),
                            crew = credit.await()?.crew ?: emptyList(),
                            isFavorite = isFavorite.await(),
                            isWatched = isWatched.await(),
                            images = images.await(),
                            keywords = keywords.await(),
                            videos = videos.await(),
                            genres = detail.await()?.genres ?: emptyList(),
                            companies = detail.await()?.productionCompanies ?: emptyList(),
                            detail = detail.await(),
                            ids = ids.await()
                        )
                    }
                }
            }
        }
    }


}

data class UIStateMovieDetail(
    val movie: Movie? = null,
    val detail: MovieDetail? = null,
    val isFavorite: Boolean = false,
    val isWatched: Boolean = false,
    val genres: List<GenreItemResponse> = emptyList(),
    val cast: List<Cast> = emptyList(),
    val crew: List<Crew> = emptyList(),
    val images: List<ImageResponse> = emptyList(),
    val keywords: List<Keyword> = emptyList(),
    val companies: List<ProductionCompany> = emptyList(),
    val videos: List<Video> = emptyList(),
    val ids: List<SocialData> = emptyList(),
    val actionState: List<Boolean> = Action.entries.map { false }
)

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
private fun MovieFields(movie: Movie, detail: MovieDetail?, modifier: Modifier) {
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






