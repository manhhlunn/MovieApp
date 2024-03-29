package com.android.movieapp.ui.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.movieapp.NavScreen
import com.android.movieapp.R
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Person
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.ui.configure.SearchBar
import com.android.movieapp.ui.ext.getColumnCount
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach

@Composable
fun PopularScreen(navController: NavController) {
    val navControllerHome = rememberNavController()
    Scaffold(bottomBar = {
        BottomNavigationView(
            navController = navControllerHome,
            items = BottomNavigationScreen.entries.filter { it.type == TypeScreen.POPULAR }
        )
    }) {
        NavHost(
            modifier = Modifier
                .padding(
                    top = 12.dp,
                    bottom = it.calculateBottomPadding() - 32.dp
                ),
            navController = navControllerHome,
            startDestination = BottomNavigationScreen.TvScreen.route
        ) {
            composable(route = BottomNavigationScreen.MovieScreen.route) {
                BaseHomeScreen(
                    navController = navController,
                    hiltViewModel<PopularMovieViewModel>()
                )
            }

            composable(route = BottomNavigationScreen.TvScreen.route) {
                BaseHomeScreen(
                    navController = navController,
                    hiltViewModel<PopularTvViewModel>()
                )
            }

            composable(route = BottomNavigationScreen.PersonScreen.route) {
                BaseHomeScreen(
                    navController = navController,
                    hiltViewModel<PersonScreenViewModel>()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BaseHomeScreen(navController: NavController, viewModel: BaseSearchViewModel<*>) {

    val values = when (viewModel) {
        is PopularMovieViewModel -> {
            viewModel.values.collectAsLazyPagingItems() to stringResource(
                R.string.search_movie
            )
        }

        is PopularTvViewModel -> {
            viewModel.values.collectAsLazyPagingItems() to stringResource(
                R.string.search_tv
            )
        }

        is PersonScreenViewModel -> {
            viewModel.values.collectAsLazyPagingItems() to stringResource(
                R.string.search_person
            )
        }

        else -> return
    }

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = values.first.loadState.refresh is LoadState.Loading,
            onRefresh = {
                values.first.refresh()
            })

    Column {
        SearchBar(
            modifier = Modifier.padding(horizontal = 12.dp), hint = values.second
        ) {
            viewModel.updateSearchQuery(it)
        }
        Box(
            modifier = Modifier
                .pullRefresh(pullRefreshState)
        ) {
            LazyVerticalGrid(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                columns = GridCells.Fixed(getColumnCount()),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
            ) {
                items(values.first.itemCount) { index ->
                    values.first[index]?.let { item ->
                        when (item) {
                            is Movie -> MovieItemView(movie = item) {
                                navController.navigate(
                                    NavScreen.MovieDetailScreen.navigateWithArgument(
                                        it
                                    )
                                )
                            }

                            is Tv -> TvItemView(tv = item) {
                                navController.navigate(
                                    NavScreen.TvDetailScreen.navigateWithArgument(
                                        it
                                    )
                                )
                            }

                            is Person -> PersonItemView(person = item) {
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
            PullRefreshIndicator(
                refreshing = values.first.loadState.refresh is LoadState.Loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}

abstract class BaseSearchViewModel<T : Any> : ViewModel() {

    abstract val invoke: (String) -> Flow<PagingData<T>>
    private var query: String? = null
    private val searchQueryFlow = MutableSharedFlow<String>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _values = searchQueryFlow
        .onEach { query = it }
        .flatMapLatest {
            invoke(it)
        }

    val values = _values.cachedIn(viewModelScope)

    fun updateSearchQuery(newQuery: String) {
        if (newQuery != query) {
            searchQueryFlow.tryEmit(newQuery)
        }
    }

    init {
        updateSearchQuery("")
    }
}