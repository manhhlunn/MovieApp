package com.android.movieapp.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
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
import com.android.movieapp.ui.ext.getObject
import com.android.movieapp.ui.ext.makeGPTSummary
import com.android.movieapp.ui.ext.makeGPTTranslate
import com.android.movieapp.ui.ext.makeGenerativeModelChatSummary
import com.android.movieapp.ui.ext.makeGenerativeModelChatTranslate
import com.android.movieapp.ui.ext.makeGenerativeModelFlow
import com.android.movieapp.ui.ext.makeWikiRequest
import com.android.movieapp.ui.ext.openChromeCustomTab
import com.android.movieapp.ui.ext.translateToVi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject


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
                                    it.filePath ?: return@DetailImage
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
            savedStateHandle.getObject<Movie>(NavScreen.MovieDetailScreen.MOVIE_DETAIL)
                ?.let { movie ->
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








