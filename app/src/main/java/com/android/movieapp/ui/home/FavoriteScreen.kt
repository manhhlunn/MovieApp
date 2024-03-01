package com.android.movieapp.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.movieapp.NavScreen
import com.android.movieapp.models.entities.FavoriteMovie
import com.android.movieapp.models.entities.FavoritePerson
import com.android.movieapp.models.entities.FavoriteTv
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Person
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.entities.WatchedMovie
import com.android.movieapp.models.entities.WatchedTv
import com.android.movieapp.network.Api
import com.android.movieapp.ui.ext.getColumnCount
import com.android.movieapp.ui.ext.roundOffDecimal


@Composable
fun FavoriteScreen(navController: NavController) {
    val navControllerFav = rememberNavController()

    Scaffold(bottomBar = {
        BottomNavigationView(
            navController = navControllerFav,
            items = BottomNavigationScreen.entries.filter { it.type == TypeScreen.FAVORITE }
        )
    }) {
        NavHost(
            modifier = Modifier
                .padding(it),
            navController = navControllerFav,
            startDestination = BottomNavigationScreen.TvFavoriteScreen.route
        ) {
            composable(route = BottomNavigationScreen.MovieFavoriteScreen.route) {
                BaseFavoriteScreen(
                    navController = navController,
                    hiltViewModel<FavoriteMovieViewModel>()
                )
            }

            composable(route = BottomNavigationScreen.TvFavoriteScreen.route) {
                BaseFavoriteScreen(
                    navController = navController,
                    hiltViewModel<FavoriteTvViewModel>()
                )
            }

            composable(route = BottomNavigationScreen.PersonFavoriteScreen.route) {
                BaseFavoriteScreen(
                    navController = navController,
                    hiltViewModel<FavoritePersonViewModel>()
                )
            }
        }
    }
}

@Composable
fun WatchedScreen(navController: NavController) {
    val navControllerWatched = rememberNavController()

    Scaffold(bottomBar = {
        BottomNavigationView(
            navController = navControllerWatched,
            items = BottomNavigationScreen.entries.filter { it.type == TypeScreen.WATCHED }
        )
    }) {
        NavHost(
            modifier = Modifier
                .padding(it),
            navController = navControllerWatched,
            startDestination = BottomNavigationScreen.TvWatchedScreen.route
        ) {
            composable(route = BottomNavigationScreen.MovieWatchedScreen.route) {
                BaseFavoriteScreen(
                    navController = navController,
                    hiltViewModel<WatchedMovieViewModel>()
                )
            }

            composable(route = BottomNavigationScreen.TvWatchedScreen.route) {
                BaseFavoriteScreen(
                    navController = navController,
                    hiltViewModel<WatchedTvViewModel>()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BaseFavoriteScreen(navController: NavController, viewModel: ViewModel) {

    val values = when (viewModel) {
        is FavoriteMovieViewModel -> {
            viewModel.favoriteMovie.collectAsLazyPagingItems()
        }

        is FavoriteTvViewModel -> {
            viewModel.favoriteTv.collectAsLazyPagingItems()
        }

        is FavoritePersonViewModel -> {
            viewModel.favoritePerson.collectAsLazyPagingItems()
        }

        is WatchedMovieViewModel -> {
            viewModel.watchedMovie.collectAsLazyPagingItems()
        }

        is WatchedTvViewModel -> {
            viewModel.watchedTv.collectAsLazyPagingItems()
        }

        else -> return
    }

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = values.loadState.refresh is LoadState.Loading,
            onRefresh = {
                values.refresh()
            })

    Column {
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
        ) {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize(),
                columns = GridCells.Fixed(getColumnCount()),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 16.dp)
            ) {
                items(values.itemCount) { index ->
                    values[index]?.let { item ->
                        when (item) {
                            is FavoriteMovie -> {
                                val movie = Movie(
                                    id = item.id,
                                    title = item.title,
                                    overview = item.overview,
                                    posterPath = item.posterPath,
                                    releaseDate = item.releaseDate,
                                    voteAverage = item.voteAverage,
                                    voteCount = item.voteCount,
                                    adult = item.adult,
                                    backdropPath = item.backdropPath,
                                    genreIds = item.genreIds,
                                    originalLanguage = item.originalLanguage,
                                    originalTitle = item.originalTitle,
                                    popularity = item.popularity,
                                    video = item.video,
                                    page = null
                                )
                                MovieItemView(
                                    posterUrl = Api.getPosterPath(item.posterPath),
                                    title = item.title.toString(),
                                    bottomRight = item.voteAverage?.roundOffDecimal()
                                ) {
                                    navController.navigate(
                                        NavScreen.MovieDetailScreen.navigateWithArgument(
                                            movie
                                        )
                                    )
                                }
                            }

                            is WatchedMovie -> {
                                val movie = Movie(
                                    id = item.id,
                                    title = item.title,
                                    overview = item.overview,
                                    posterPath = item.posterPath,
                                    releaseDate = item.releaseDate,
                                    voteAverage = item.voteAverage,
                                    voteCount = item.voteCount,
                                    adult = item.adult,
                                    backdropPath = item.backdropPath,
                                    genreIds = item.genreIds,
                                    originalLanguage = item.originalLanguage,
                                    originalTitle = item.originalTitle,
                                    popularity = item.popularity,
                                    video = item.video,
                                    page = null
                                )
                                MovieItemView(
                                    posterUrl = Api.getPosterPath(item.posterPath),
                                    title = item.title.toString(),
                                    bottomRight = item.voteAverage?.roundOffDecimal()
                                ) {
                                    navController.navigate(
                                        NavScreen.MovieDetailScreen.navigateWithArgument(
                                            movie
                                        )
                                    )
                                }
                            }

                            is FavoriteTv -> {
                                val tv = Tv(
                                    id = item.id,
                                    name = item.name,
                                    firstAirDate = item.firstAirDate,
                                    genreIds = item.genreIds,
                                    originalLanguage = item.originalLanguage,
                                    originalName = item.originalName,
                                    overview = item.overview,
                                    popularity = item.popularity,
                                    posterPath = item.posterPath,
                                    voteAverage = item.voteAverage,
                                    voteCount = item.voteCount,
                                    adult = item.adult,
                                    backdropPath = item.backdropPath,
                                    originCountry = item.originCountry,
                                    page = null
                                )
                                MovieItemView(
                                    posterUrl = Api.getPosterPath(item.posterPath),
                                    title = item.name.toString(),
                                    bottomRight = item.voteAverage?.roundOffDecimal()
                                ) {
                                    navController.navigate(
                                        NavScreen.TvDetailScreen.navigateWithArgument(
                                            tv
                                        )
                                    )
                                }
                            }

                            is WatchedTv -> {
                                val tv = Tv(
                                    id = item.id,
                                    name = item.name,
                                    firstAirDate = item.firstAirDate,
                                    genreIds = item.genreIds,
                                    originalLanguage = item.originalLanguage,
                                    originalName = item.originalName,
                                    overview = item.overview,
                                    popularity = item.popularity,
                                    posterPath = item.posterPath,
                                    voteAverage = item.voteAverage,
                                    voteCount = item.voteCount,
                                    adult = item.adult,
                                    backdropPath = item.backdropPath,
                                    originCountry = item.originCountry,
                                    page = null
                                )
                                MovieItemView(
                                    posterUrl = Api.getPosterPath(item.posterPath),
                                    title = item.name.toString(),
                                    bottomRight = item.voteAverage?.roundOffDecimal()
                                ) {
                                    navController.navigate(
                                        NavScreen.TvDetailScreen.navigateWithArgument(
                                            tv
                                        )
                                    )
                                }
                            }

                            is FavoritePerson -> {
                                val person = Person(
                                    id = item.id,
                                    name = item.name,
                                    profilePath = item.profilePath,
                                    adult = item.adult,
                                    knownForDepartment = item.knownForDepartment,
                                    popularity = item.popularity,
                                    gender = item.gender,
                                    page = null
                                )
                                PersonItemView(person = person) {
                                    navController.navigate(
                                        NavScreen.PersonDetailScreen.navigateWithArgument(
                                            it
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }
            PullRefreshIndicator(
                refreshing = values.loadState.refresh is LoadState.Loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}
