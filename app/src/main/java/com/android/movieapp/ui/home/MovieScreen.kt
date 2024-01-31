package com.android.movieapp.ui.home

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.paging.PagingData
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.network.Api
import com.android.movieapp.repository.FavoriteRepository
import com.android.movieapp.ui.ext.ProgressiveGlowingImage
import com.android.movieapp.ui.theme.AppYellow
import com.android.movieapp.usecase.FilterUseCase
import com.android.movieapp.usecase.PopularUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject


@Composable
fun MovieItemView(
    modifier: Modifier = Modifier,
    movie: Movie,
    onExpandDetails: (Movie) -> Unit
) {
    Column(modifier = modifier
        .padding(4.dp)
        .clickable {
            onExpandDetails.invoke(movie)
        }) {
        val posterUrl = Api.getPosterPath(movie.posterPath)
        Box {
            ProgressiveGlowingImage(
                url = posterUrl,
                glow = true
            )
            Text(
                text = movie.voteAverage.toString(),
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black
                    )
                ),
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = AppYellow,
                modifier = Modifier
                    .padding(6.dp)
                    .align(Alignment.BottomEnd)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = movie.title ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 1f
                )
            ),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@HiltViewModel
class PopularMovieViewModel @Inject constructor(private val popularUseCase: PopularUseCase) :
    BaseSearchViewModel<Movie>() {

    override val invoke: (String) -> Flow<PagingData<Movie>> = {
        popularUseCase.invokeMovie(it)
    }
}

@HiltViewModel
class FavoriteMovieViewModel @Inject constructor(favoriteRepository: FavoriteRepository) :
    ViewModel() {

    val favoriteMovie = favoriteRepository.favoriteMovies()
}

@HiltViewModel
class WatchedMovieViewModel @Inject constructor(favoriteRepository: FavoriteRepository) :
    ViewModel() {

    val watchedMovie = favoriteRepository.watchedMovies()
}

@HiltViewModel
class FilterMovieViewModel @Inject constructor(private val filterUseCase: FilterUseCase) :
    BaseFilterViewModel<Movie>() {

    override val invoke: (FilterValue) -> Flow<PagingData<Movie>> = {
        filterUseCase.invokeMovie(
            it.sortValue,
            it.originCountry.value,
            it.originLanguage.value,
            it.withGenres,
            it.years,
            it.includes.firstOrNull { include -> include.type == Includes.FAVORITE }?.value ?: false,
            it.includes.firstOrNull { include -> include.type == Includes.WATCHED }?.value ?: false,
        )
    }

    override suspend fun invokeGenre() = filterUseCase.getMovieGenres()
    override suspend fun invokeLanguage() = filterUseCase.getLanguages()
    override suspend fun invokeCountry() = filterUseCase.getCountries()
    override suspend fun invokeYear() = filterUseCase.getYears()

    init {
        fetch(FilterValue(includes = Includes.entries.mapNotNull {
            when (it) {
                Includes.FAVORITE -> IncludesData(it, true)
                Includes.WATCHED -> IncludesData(it, false)
                Includes.ENDED -> null
            }
        }))
    }
}

