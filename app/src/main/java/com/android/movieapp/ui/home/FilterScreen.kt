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
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.network.GenreItemResponse
import com.android.movieapp.network.Api
import com.android.movieapp.network.service.SortValue
import com.android.movieapp.ui.ext.DropdownItem
import com.android.movieapp.ui.ext.getColumnCount
import com.android.movieapp.ui.ext.roundOffDecimal
import com.android.movieapp.ui.home.widget.BottomNavigationScreen
import com.android.movieapp.ui.home.widget.BottomNavigationView
import com.android.movieapp.ui.home.widget.DropDownLine
import com.android.movieapp.ui.home.widget.GenresLine
import com.android.movieapp.ui.home.widget.HomeDrawerNavigation
import com.android.movieapp.ui.home.widget.IncludeLine
import com.android.movieapp.ui.home.widget.MovieItemView
import com.android.movieapp.ui.home.widget.SortByLine
import com.android.movieapp.ui.home.widget.TypeScreen
import com.android.movieapp.ui.home.widget.YearLine
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@Composable
fun FilterScreen(navController: NavController) {
    val navControllerFilter = rememberNavController()

    Scaffold(bottomBar = {
        BottomNavigationView(
            navController = navControllerFilter,
            items = BottomNavigationScreen.entries.filter { it.type == TypeScreen.FILTER }
        )
    }) {
        NavHost(
            route = HomeDrawerNavigation.FilterScreen.route,
            modifier = Modifier.padding(it),
            navController = navControllerFilter,
            startDestination = BottomNavigationScreen.TvFilterScreen.route
        ) {
            composable(route = BottomNavigationScreen.MovieFilterScreen.route) {
                BaseFilterScreen(
                    navController = navController,
                    hiltViewModel<FilterMovieViewModel>()
                )
            }

            composable(route = BottomNavigationScreen.TvFilterScreen.route) {
                BaseFilterScreen(
                    navController = navController,
                    hiltViewModel<FilterTvViewModel>()
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BaseFilterScreen(navController: NavController, viewModel: BaseFilterViewModel<*>) {

    val values = when (viewModel) {
        is FilterMovieViewModel -> {
            viewModel.values.collectAsLazyPagingItems()
        }

        is FilterTvViewModel -> {
            viewModel.values.collectAsLazyPagingItems()
        }

        else -> return
    }

    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = values.loadState.refresh is LoadState.Loading,
            onRefresh = {
                values.refresh()
            })

    Column {
        DropDownLine(
            resId = R.string.country_title,
            dropdownItems = uiState.countries,
            dropdownItem = uiState.filterValue.originCountry,
            onSelected = {
                viewModel.onSelectedCountry(it)
            }
        )
        SortByLine(uiState.filterValue.sortValue) {
            viewModel.onSelectedSort(it)
        }
        IncludeLine(uiState.filterValue.includes) {
            viewModel.onSelectedInclude(it)
        }
        YearLine(uiState.filterValue.years, uiState.years) {
            viewModel.onSelectedYear(it)
        }
        GenresLine(values = uiState.genres, current = uiState.filterValue.withGenres) {
            viewModel.onSelectedGenre(it)
        }
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
                refreshing = values.loadState.refresh is LoadState.Loading,
                state = pullRefreshState,
                modifier = Modifier.align(Alignment.TopCenter),
            )
        }
    }
}


abstract class BaseFilterViewModel<T : Any> : ViewModel() {

    abstract val invoke: (FilterValue) -> Flow<PagingData<T>>
    abstract suspend fun invokeGenre(): List<GenreItemResponse>
    abstract suspend fun invokeYear(): List<Int>
    abstract suspend fun invokeLanguage(): Pair<DropdownItem, List<DropdownItem>>
    abstract suspend fun invokeCountry(): Pair<DropdownItem, List<DropdownItem>>

    private val _uiState = MutableStateFlow(UIStateFilterScreen())
    val uiState: StateFlow<UIStateFilterScreen> = _uiState
    private var currentFilter: FilterValue? = null
    private val filterQueryFlow = MutableSharedFlow<FilterValue>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _values = filterQueryFlow
        .onEach { currentFilter = it }
        .flatMapLatest {
            invoke(it)
        }

    val values = _values.cachedIn(viewModelScope)

    private fun updateFilter(filter: FilterValue) {
        if (filter != currentFilter) {
            filterQueryFlow.tryEmit(filter)
        }
    }

    fun onSelectedYear(years: List<Int>) {
        _uiState.update {
            val newFilter = it.filterValue.copy(
                years = years
            )
            updateFilter(newFilter)
            it.copy(
                filterValue = newFilter
            )
        }
    }

    fun onSelectedCountry(originCountry: DropdownItem) {
        _uiState.update {
            val newFilter = it.filterValue.copy(
                originCountry = originCountry
            )
            updateFilter(newFilter)
            it.copy(
                filterValue = newFilter
            )
        }
    }

    fun onSelectedLanguage(originLanguage: DropdownItem) {
        _uiState.update {
            val newFilter = it.filterValue.copy(
                originLanguage = originLanguage
            )
            updateFilter(newFilter)
            it.copy(
                filterValue = newFilter
            )
        }
    }

    fun onSelectedSort(sortValue: SortValue) {
        _uiState.update {
            val newFilter = it.filterValue.copy(
                sortValue = sortValue
            )
            updateFilter(newFilter)
            it.copy(
                filterValue = newFilter
            )
        }
    }

    fun onSelectedGenre(id: Int) {
        _uiState.update {
            val currentGenre = it.filterValue.withGenres.toMutableList()
            if (id in currentGenre) currentGenre.remove(id)
            else currentGenre.add(id)
            val newFilter = it.filterValue.copy(
                withGenres = currentGenre
            )
            updateFilter(newFilter)
            it.copy(
                filterValue = newFilter
            )
        }
    }

    fun fetch(filter: FilterValue) {
        viewModelScope.launch {
            val countries = async { invokeCountry() }
            val languages = async { invokeLanguage() }
            val genres = async { invokeGenre() }
            val years = async { invokeYear() }
            _uiState.update {
                val newFilter = filter.copy(
                    originCountry = countries.await().first,
                    originLanguage = languages.await().first
                )
                updateFilter(newFilter)
                it.copy(
                    countries = countries.await().second,
                    languages = languages.await().second,
                    genres = genres.await(),
                    years = years.await(),
                    filterValue = newFilter
                )
            }
        }
    }

    fun onSelectedInclude(includes: List<IncludesData>) {
        _uiState.update {
            val newFilter = it.filterValue.copy(includes = includes)
            updateFilter(newFilter)
            it.copy(
                filterValue = newFilter
            )
        }
    }

}

data class FilterValue(
    val years: List<Int> = emptyList(),
    val sortValue: SortValue = SortValue.POPULAR_DESC,
    val originCountry: DropdownItem = DropdownItem(),
    val originLanguage: DropdownItem = DropdownItem(),
    val withGenres: List<Int> = emptyList(),
    val includes: List<IncludesData> = Includes.entries.map { IncludesData(it) }
)

data class IncludesData(val type: Includes, val value: Boolean = false)

enum class Includes {
    FAVORITE,
    WATCHED,
    ENDED
}

data class UIStateFilterScreen(
    val filterValue: FilterValue = FilterValue(),
    val years: List<Int> = emptyList(),
    val countries: List<DropdownItem> = emptyList(),
    val languages: List<DropdownItem> = emptyList(),
    val genres: List<GenreItemResponse> = emptyList()
)
