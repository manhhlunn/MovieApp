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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material3.Scaffold
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavController
import androidx.paging.LoadState
import androidx.paging.cachedIn
import androidx.paging.compose.collectAsLazyPagingItems
import com.android.movieapp.NavScreen
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.network.Api
import com.android.movieapp.network.service.SortValue
import com.android.movieapp.repository.FilterRepository
import com.android.movieapp.ui.ext.getColumnCount
import com.android.movieapp.ui.ext.roundOffDecimal
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject


@OptIn(ExperimentalMaterialApi::class)
@Composable
fun KeyDetailScreen(navController: NavController, viewModel: KeyDetailViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val movies = viewModel.movies.collectAsLazyPagingItems()
    val tvs = viewModel.tvs.collectAsLazyPagingItems()

    Scaffold(topBar = {
        TopAppBarDetail(uiState?.name, onBackClicked = {
            navController.popBackStack()
        })
    }) { padding ->
        Box(contentAlignment = Alignment.Center) {
            if (uiState != null) {
                val pullRefreshState =
                    rememberPullRefreshState(
                        refreshing = if (uiState?.isMovie == true) (movies.loadState.refresh is LoadState.Loading) else (tvs.loadState.refresh is LoadState.Loading),
                        onRefresh = {
                            if (uiState?.isMovie == true) movies.refresh()
                            else tvs.refresh()
                        })

                Column(modifier = Modifier.padding(padding)) {
                    Spacer(modifier = Modifier.height(12.dp))
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
                            val values = if (uiState?.isMovie == true) movies else tvs
                            items(values.itemCount) { index ->
                                values[index]?.let { item ->
                                    when (item) {
                                        is Movie -> MovieItemView(
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

                                        is Tv -> MovieItemView(
                                            posterUrl = Api.getPosterPath(item.posterPath),
                                            title = item.name.toString(),
                                            bottomRight = item.voteAverage?.roundOffDecimal()
                                        ) {
                                            navController.navigate(
                                                NavScreen.TvDetailScreen.navigateWithArgument(
                                                    item
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        PullRefreshIndicator(
                            refreshing = if (uiState?.isMovie == true) (movies.loadState.refresh is LoadState.Loading) else (tvs.loadState.refresh is LoadState.Loading),
                            state = pullRefreshState,
                            modifier = Modifier.align(Alignment.TopCenter),
                        )
                    }
                }
            } else {
                CircularProgressIndicator()
            }
        }
    }
}

@HiltViewModel
class KeyDetailViewModel @Inject constructor(
    private val filterRepository: FilterRepository,
    savedStateHandle: SavedStateHandle
) :
    ViewModel() {

    private val _uiState = MutableStateFlow(
        savedStateHandle.get<NavScreen.KeyDetailScreen.KeyDetail>(NavScreen.KeyDetailScreen.keyDetail),
    )
    val uiState: StateFlow<NavScreen.KeyDetailScreen.KeyDetail?> = _uiState
    private var currentFilter: NavScreen.KeyDetailScreen.KeyDetail? = null
    private val filterQueryFlow = _uiState.asSharedFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _movies = filterQueryFlow
        .onEach { currentFilter = it }
        .flatMapLatest {
            if (it?.isMovie == true) {
                filterRepository.filterMovies(
                    SortValue.POPULAR_DESC,
                    "",
                    "",
                    it.genre?.let { genre -> listOf(genre) } ?: emptyList(),
                    it.keyword?.let { keyword -> listOf(keyword) } ?: emptyList(),
                    emptyList(),
                    it.company?.let { company -> listOf(company) } ?: emptyList(),
                    isIncludeFavoritePeople = false,
                    isIncludeWatched = false
                )
            } else emptyFlow()
        }

    val movies = _movies.cachedIn(viewModelScope)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _tvs = filterQueryFlow
        .onEach { currentFilter = it }
        .flatMapLatest {
            if (it?.isMovie == false) {
                filterRepository.filterTvs(
                    SortValue.POPULAR_DESC,
                    "",
                    "",
                    it.genre?.let { genre -> listOf(genre) } ?: emptyList(),
                    it.keyword?.let { keyword -> listOf(keyword) } ?: emptyList(),
                    emptyList(),
                    it.company?.let { company -> listOf(company) } ?: emptyList(),
                    isIncludeEnded = false,
                    isIncludeWatched = false
                )
            } else emptyFlow()
        }

    val tvs = _tvs.cachedIn(viewModelScope)

}





