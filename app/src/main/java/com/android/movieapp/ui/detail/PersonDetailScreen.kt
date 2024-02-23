package com.android.movieapp.ui.detail

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
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
import com.android.movieapp.models.entities.FavoritePerson
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Person
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.network.ImageResponse
import com.android.movieapp.models.network.PersonDetail
import com.android.movieapp.network.Api
import com.android.movieapp.repository.FavoriteRepository
import com.android.movieapp.repository.PersonRepository
import com.android.movieapp.ui.ext.CircleGlowingImage
import com.android.movieapp.ui.ext.ifNull
import com.android.movieapp.ui.ext.ifNullOrEmpty
import com.android.movieapp.ui.ext.makeWikiRequest
import com.android.movieapp.ui.ext.openChromeCustomTab
import com.android.movieapp.ui.ext.roundOffDecimal
import com.android.movieapp.ui.ext.springAnimation
import com.android.movieapp.ui.ext.translateToVi
import com.android.movieapp.ui.home.MovieItemView
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@Composable
fun PersonDetailScreen(
    navController: NavController,
    viewModel: PersonDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    Box(contentAlignment = Alignment.Center) {
        val person = uiState.person
        if (person != null) {
            ConstraintLayout(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState()),
            ) {
                val (backIcon, openIcon, backdrop, profile, ids, specs, title, bio) = createRefs()
                val (movies, tvs, imagesSection) = createRefs()
                val startGuideline = createGuidelineFromStart(16.dp)
                val endGuideline = createGuidelineFromEnd(16.dp)

                Backdrop(
                    backdropUrl = Api.getDefaultBackDropPath(),
                    Modifier
                        .constrainAs(backdrop) {})

                IconButton(onClick = {
                    navController.popBackStack()
                }, modifier = Modifier
                    .constrainAs(backIcon) {
                        start.linkTo(parent.start)
                        top.linkTo(backdrop.bottom)
                        end.linkTo(profile.start)
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
                    .constrainAs(openIcon) {
                        start.linkTo(profile.end)
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

                Title(
                    person.name,
                    person.knownForDepartment,
                    modifier = Modifier
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                        .constrainAs(title) {
                            top.linkTo(profile.bottom)
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
                    } else
                        context.openChromeCustomTab(
                            when (social.type) {
                                SocialType.Facebook -> "https://www.facebook.com/${social.id}"
                                SocialType.IMDb -> "https://www.imdb.com/name/${social.id}"
                                SocialType.Instagram -> "https://www.instagram.com/${social.id}"
                                SocialType.Twitter -> "https://twitter.com/${social.id}"
                                SocialType.Tiktok -> "https://www.tiktok.com/@${social.id}"
                                else -> return@IdChips
                            }
                        )
                }

                PersonProfile(url = Api.getPosterPath(person.profilePath), modifier = Modifier
                    .padding(bottom = 100.dp)
                    .width(180.dp)
                    .constrainAs(profile) {
                        bottom.linkTo(backdrop.bottom)
                        linkTo(startGuideline, endGuideline)
                    })

                PersonFields(
                    person = person,
                    modifier = Modifier.constrainAs(specs) {
                        top.linkTo(ids.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                )

                Text(
                    text = person.biography ?: "",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        lineHeight = 24.sp
                    ),
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .constrainAs(bio) {
                            top.linkTo(
                                specs.bottom,
                                if (person.biography.isNullOrEmpty()) 0.dp else 16.dp
                            )
                            linkTo(startGuideline, endGuideline)
                        }
                        .clickable {
                            viewModel.translate(person.biography ?: "")
                        },
                )

                SectionView(
                    items = uiState.movies,
                    headerResId = R.string.movie_title,
                    itemContent = { item, _ ->
                        MovieItemView(
                            modifier = Modifier.width(140.dp),
                            posterUrl = Api.getPosterPath(item.posterPath),
                            title = item.title.toString(),
                            bottomRight = item.voteAverage?.roundOffDecimal()
                        ) {
                            navController.navigate(
                                NavScreen.MovieDetailScreen.navigateWithArgument(
                                    item
                                )
                            )
                        }
                    },
                    modifier = Modifier.constrainAs(movies) {
                        top.linkTo(bio.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                )

                SectionView(
                    items = uiState.tvs,
                    headerResId = R.string.tv_title,
                    itemContent = { item, _ ->
                        MovieItemView(
                            modifier = Modifier.width(140.dp),
                            posterUrl = Api.getPosterPath(item.posterPath),
                            title = item.name.toString(),
                            bottomRight = item.voteAverage?.roundOffDecimal()
                        ) {
                            navController.navigate(NavScreen.TvDetailScreen.navigateWithArgument(item))
                        }
                    },
                    modifier = Modifier.constrainAs(tvs) {
                        top.linkTo(movies.bottom, 16.dp)
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
                        top.linkTo(tvs.bottom, 16.dp)
                        linkTo(startGuideline, endGuideline)
                    },
                )
            }
        } else {
            CircularProgressIndicator()
        }
    }
}


@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val personRepository: PersonRepository,
    private val favoriteRepository: FavoriteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(UIStatePersonDetail())
    val uiState: StateFlow<UIStatePersonDetail> = _uiState

    fun translate(text: String) {
        viewModelScope.launch {
            val translate = translateToVi(text)
            if (translate != null) {
                _uiState.update {
                    it.copy(
                        person = it.person?.copy(
                            biography = translate
                        )
                    )
                }
            }
        }
    }

    init {
        viewModelScope.launch {
            savedStateHandle.get<Person>(NavScreen.PersonDetailScreen.personDetail)
                ?.let { person ->
                    person.id?.let { id ->
                        val detail = async { personRepository.getPersonDetail(id) }
                        val images = async { personRepository.getPersonImages(id) }
                        val movies = async { personRepository.getPersonMovies(id) }
                        val tvs = async { personRepository.getPersonTvs(id) }
                        val isFavorite = async { favoriteRepository.isFavoritePerson(id) }
                        val ids = async { personRepository.getIds(id) }
                        _uiState.update {
                            it.copy(
                                person = detail.await() ?: PersonDetail(
                                    adult = person.adult,
                                    alsoKnownAs = null,
                                    biography = null,
                                    birthday = null,
                                    deathday = null,
                                    gender = person.gender,
                                    homepage = null,
                                    id = person.id,
                                    imdbId = null,
                                    knownForDepartment = person.knownForDepartment,
                                    name = person.name,
                                    placeOfBirth = null,
                                    popularity = person.popularity,
                                    profilePath = person.profilePath
                                ),
                                movies = movies.await(),
                                tvs = tvs.await(),
                                images = images.await(),
                                isFavorite = isFavorite.await(),
                                ids = ids.await()
                            )
                        }
                    }
                }
        }
    }

    fun onFavoriteChange() {
        viewModelScope.launch {
            uiState.value.person?.let { person ->
                val isFavorite = uiState.value.isFavorite
                if (isFavorite) favoriteRepository.deleteFavoritePerson(person.id ?: return@launch)
                else favoriteRepository.addFavoritePerson(
                    FavoritePerson(
                        adult = person.adult,
                        gender = person.gender,
                        id = person.id,
                        knownForDepartment = person.knownForDepartment,
                        name = person.name,
                        popularity = person.popularity,
                        profilePath = person.profilePath
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
}


data class UIStatePersonDetail(
    val person: PersonDetail? = null,
    val isFavorite: Boolean = false,
    val movies: List<Movie> = emptyList(),
    val tvs: List<Tv> = emptyList(),
    val images: List<ImageResponse> = emptyList(),
    val ids: List<SocialData> = emptyList()
)

@Composable
private fun PersonFields(person: PersonDetail, modifier: Modifier) {
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






