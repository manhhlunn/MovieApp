package com.android.movieapp.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.android.movieapp.models.entities.FavoriteTv
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.entities.WatchedTv
import com.android.movieapp.models.network.Cast
import com.android.movieapp.models.network.Crew
import com.android.movieapp.models.network.GenreItemResponse
import com.android.movieapp.models.network.ImageResponse
import com.android.movieapp.models.network.Keyword
import com.android.movieapp.models.network.ProductionCompany
import com.android.movieapp.models.network.TvDetail
import com.android.movieapp.models.network.Video
import com.android.movieapp.models.network.toPerson
import com.android.movieapp.network.Api
import com.android.movieapp.repository.ConfigureRepository
import com.android.movieapp.repository.FavoriteRepository
import com.android.movieapp.repository.TvRepository
import com.android.movieapp.ui.ext.ProgressiveGlowingImage
import com.android.movieapp.ui.ext.ifNull
import com.android.movieapp.ui.ext.ifNullOrEmpty
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
fun TvDetailScreen(
    navController: NavController,
    viewModel: TvDetailViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    Box(contentAlignment = Alignment.Center) {
        val tv = uiState.tv
        if (tv != null) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                val (backIcon, favIcon, watched, backdrop, poster, title, ids, genres, specs, overview, actions, seasons) = createRefs()
                val (castSection, crewSection, imagesSection, videos, companies, keywords) = createRefs()
                val startGuideline = createGuidelineFromStart(16.dp)
                val endGuideline = createGuidelineFromEnd(16.dp)

                Backdrop(
                    backdropUrl = Api.getBackdropPath(tv.backdropPath),
                    Modifier
                        .constrainAs(backdrop) {})

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

                Title(
                    tv.name,
                    tv.originalName,
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
                    } else {
                        context.openChromeCustomTab(
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
                }

                Poster(posterUrl = Api.getPosterPath(tv.posterPath), modifier = Modifier
                    .width(120.dp)
                    .padding(top = 120.dp)
                    .constrainAs(poster) {
                        centerAround(backdrop.bottom)
                        linkTo(startGuideline, endGuideline)
                    })

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
                                isMovie = false,
                                genre = it.id
                            )
                        )
                    )
                }

                TvFields(
                    tv,
                    uiState.detail,
                    modifier = Modifier.constrainAs(specs) {
                        top.linkTo(genres.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                )

                Text(
                    text = tv.overview ?: "",
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
                            viewModel.translate(tv.overview ?: "") { mutableUiState, source ->
                                mutableUiState.update {
                                    it.copy(
                                        tv = it.tv?.copy(
                                            overview = source
                                        )
                                    )
                                }
                            }
                        }
                )

                ActionChips(modifier = Modifier.constrainAs(actions) {
                    top.linkTo(overview.bottom, 16.dp)
                    linkTo(startGuideline, endGuideline)
                }, actionState = uiState.actionState) {
                    viewModel.actionDetail(it)
                }

                SectionView(
                    items = uiState.detail?.seasons ?: emptyList(),
                    headerResId = R.string.seasons,
                    itemContent = { item, idx ->
                        TvSeason(season = item) {
                            viewModel.translate(item.overview ?: "") { mutableUiState, source ->
                                mutableUiState.update {
                                    val current =
                                        it.detail?.seasons?.toMutableList() ?: return@translate
                                    current[idx] = item.copy(overview = source)
                                    it.copy(
                                        detail = it.detail.copy(
                                            seasons = current
                                        )
                                    )
                                }
                            }
                        }
                    },
                    modifier = Modifier.constrainAs(seasons) {
                        top.linkTo(actions.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                )

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
                        top.linkTo(seasons.bottom, 16.dp)
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
                                        isMovie = false,
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
                                isMovie = false,
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
class TvDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    tvRepository: TvRepository,
    private val favoriteRepository: FavoriteRepository,
    private val configureRepository: ConfigureRepository
) : ViewModel() {


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

    fun translate(from: String, action: (MutableStateFlow<UIStateTvDetail>, String) -> Unit) {
        viewModelScope.launch {
            translateToVi(from)?.let { action.invoke(_uiState, it) }
        }
    }

    private fun translateAI(isGPT: Boolean) {
        viewModelScope.launch {
            uiState.value.tv?.overview?.let { overview ->
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
                            tv = it.tv?.copy(
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
            uiState.value.tv?.let { tv ->
                val ext = StringBuilder()
                val language = configureRepository.getLanguages()
                    .firstOrNull { it.iso6391 == tv.originalLanguage }?.englishName
                if (!language.isNullOrEmpty() && !tv.originalName.isNullOrEmpty()) {
                    ext.append("(")
                    ext.append("$language : ${tv.originalName}")
                }
                if (!tv.firstAirDate.isNullOrEmpty()) {
                    ext.append(if (ext.isEmpty()) "(" else " - ")
                    ext.append(tv.firstAirDate.substring(0..3))
                }
                if (ext.isNotEmpty()) ext.append(")")

                val name = "Tv series ${tv.name} $ext"
                val output =
                    if (isGPT) makeGPTSummary(name) else makeGenerativeModelChatSummary(name)
                _uiState.update {
                    val new = it.actionState.toMutableList()
                    new[if (isGPT) Action.GPT_CHAT.ordinal else Action.GG_CHAT.ordinal] = false
                    it.copy(
                        tv = it.tv?.copy(
                            overview = output ?: it.tv.overview
                        ),
                        actionState = new
                    )
                }
            }
        }
    }

    private fun generativeModel() {
        viewModelScope.launch {
            uiState.value.tv?.let { tv ->
                val ext = StringBuilder()
                val language = configureRepository.getLanguages()
                    .firstOrNull { it.iso6391 == tv.originalLanguage }?.englishName
                if (!language.isNullOrEmpty() && !tv.originalName.isNullOrEmpty()) {
                    ext.append("(")
                    ext.append("$language : ${tv.originalName}")
                }
                if (!tv.firstAirDate.isNullOrEmpty()) {
                    ext.append(if (ext.isEmpty()) "(" else " - ")
                    ext.append(tv.firstAirDate.substring(0..3))
                }
                if (ext.isNotEmpty()) ext.append(")")

                val name = "Tv series ${tv.name} $ext"

                makeGenerativeModelFlow(name)
                    .collect { value ->
                        _uiState.update {
                            val new = it.actionState.toMutableList()
                            new[Action.GenerativeModel.ordinal] = false
                            it.copy(
                                tv = it.tv?.copy(
                                    overview = value
                                ),
                                actionState = new
                            )
                        }
                    }
            }
        }
    }

    fun onFavoriteChange() {
        viewModelScope.launch {
            uiState.value.tv?.let { tv ->
                val isFavorite = uiState.value.isFavorite
                if (isFavorite) favoriteRepository.deleteFavoriteTv(tv.id ?: return@launch)
                else favoriteRepository.addFavoriteTv(
                    FavoriteTv(
                        adult = tv.adult,
                        backdropPath = tv.backdropPath,
                        genreIds = tv.genreIds,
                        id = tv.id,
                        originalLanguage = tv.originalLanguage,
                        originalName = tv.originalName,
                        overview = tv.overview,
                        popularity = tv.popularity,
                        posterPath = tv.posterPath,
                        firstAirDate = tv.firstAirDate,
                        voteAverage = tv.voteAverage,
                        voteCount = tv.voteCount,
                        name = tv.name,
                        originCountry = tv.originCountry
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
            uiState.value.tv?.let { tv ->
                val isWatched = uiState.value.isWatched
                if (isWatched) favoriteRepository.deleteWatchedTv(tv.id ?: return@launch)
                else favoriteRepository.addWatchedTv(
                    WatchedTv(
                        adult = tv.adult,
                        backdropPath = tv.backdropPath,
                        genreIds = tv.genreIds,
                        id = tv.id,
                        originalLanguage = tv.originalLanguage,
                        originalName = tv.originalName,
                        overview = tv.overview,
                        popularity = tv.popularity,
                        posterPath = tv.posterPath,
                        firstAirDate = tv.firstAirDate,
                        voteAverage = tv.voteAverage,
                        voteCount = tv.voteCount,
                        name = tv.name,
                        originCountry = tv.originCountry
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

    private val _uiState = MutableStateFlow(UIStateTvDetail())
    val uiState: StateFlow<UIStateTvDetail> = _uiState

    init {
        viewModelScope.launch {
            savedStateHandle.get<Tv>(NavScreen.TvDetailScreen.tvDetail)?.let { tv ->
                _uiState.update {
                    it.copy(
                        tv = tv
                    )
                }
                tv.id?.let { id ->
                    val detail = async { tvRepository.getTvDetail(id) }
                    val credit = async { tvRepository.getTvCredit(id) }
                    val isFavorite = async { favoriteRepository.isFavoriteTv(id) }
                    val isWatched = async { favoriteRepository.isWatchedTv(id) }
                    val images = async { tvRepository.getTvImages(id) }
                    val keywords = async { tvRepository.getTvKeywords(id) }
                    val videos = async { tvRepository.getTvVideos(id) }
                    val ids = async { tvRepository.getIds(id) }
                    _uiState.update {
                        it.copy(
                            cast = credit.await()?.cast ?: emptyList(),
                            crew = credit.await()?.crew ?: emptyList(),
                            isFavorite = isFavorite.await(),
                            isWatched = isWatched.await(),
                            images = images.await(),
                            keywords = keywords.await(),
                            videos = videos.await(),
                            detail = detail.await(),
                            genres = detail.await()?.genres ?: emptyList(),
                            companies = detail.await()?.productionCompanies ?: emptyList(),
                            ids = ids.await()
                        )
                    }
                }
            }
        }
    }
}

data class UIStateTvDetail(
    val tv: Tv? = null,
    val detail: TvDetail? = null,
    val isFavorite: Boolean = false,
    val isWatched: Boolean = false,
    val genres: List<GenreItemResponse> = emptyList(),
    val cast: List<Cast> = emptyList(),
    val crew: List<Crew> = emptyList(),
    val companies: List<ProductionCompany> = emptyList(),
    val images: List<ImageResponse> = emptyList(),
    val keywords: List<Keyword> = emptyList(),
    val videos: List<Video> = emptyList(),
    val ids: List<SocialData> = emptyList(),
    val actionState: List<Boolean> = Action.entries.map { false }
)

@Composable
private fun TvFields(tv: Tv, detail: TvDetail?, modifier: Modifier) {
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






