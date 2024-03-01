package com.android.movieapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
import com.android.movieapp.ui.ext.FilterRow
import com.android.movieapp.ui.ext.getColumnCount
import com.android.movieapp.ui.ext.roundOffDecimal
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
    val navControllerFav = rememberNavController()

    Scaffold(bottomBar = {
        BottomNavigationView(
            navController = navControllerFav,
            items = BottomNavigationScreen.entries.filter { it.type == TypeScreen.FILTER }
        )
    }) {
        NavHost(
            modifier = Modifier
                .padding(it),
            navController = navControllerFav,
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

@Composable
fun DropDownLine(
    resId: Int,
    dropdownItems: List<DropdownItem>,
    dropdownItem: DropdownItem,
    onSelected: (DropdownItem) -> Unit
) {
    if (dropdownItems.isNotEmpty()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
            verticalArrangement = Arrangement.Center,
        ) {
            FilterRow(
                resId,
                dropdownItems,
                dropdownItem,
                onSelected = {
                    onSelected(it)
                },
            )
        }
    }
}

@Composable
fun SortByLine(
    current: SortValue,
    onSelected: (SortValue) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${stringResource(id = R.string.sort_by)} : ",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                SortValue.entries.forEachIndexed { index, it ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it.display,
                        color = if (it == current) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 0f),
                                blurRadius = 0.5f
                            )
                        ),
                        modifier = Modifier
                            .background(
                                if (it == current) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                onSelected.invoke(it)
                            },
                    )
                    if (index == SortValue.entries.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun IncludeLine(
    current: List<IncludesData>,
    onSelected: (List<IncludesData>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${stringResource(id = R.string.include)} : ",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                current.forEachIndexed { index, it ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = when (it.type) {
                            Includes.FAVORITE -> stringResource(id = R.string.fav_people)
                            Includes.WATCHED -> stringResource(id = R.string.watched_data)
                            Includes.ENDED -> stringResource(id = R.string.ended_series)
                        },
                        color = if (it.value) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 0f),
                                blurRadius = 0.5f
                            )
                        ),
                        modifier = Modifier
                            .background(
                                if (it.value) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                val new = current.toMutableList()
                                new[index] = it.copy(value = !it.value)
                                onSelected.invoke(new)
                            },
                    )
                    if (index == current.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun YearLine(
    current: List<Int>,
    values: List<Int>,
    onSelected: (List<Int>) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${stringResource(id = R.string.year)} : ",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                values.forEachIndexed { index, it ->
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (it == -1) "None" else it.toString(),
                        color = if (if (it == -1 && current.isEmpty()) true else (it in current)) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 0f),
                                blurRadius = 0.5f
                            )
                        ),
                        modifier = Modifier
                            .background(
                                if (if (it == -1 && current.isEmpty()) true else (it in current)) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                val selected = when {
                                    (it == -1) -> emptyList()
                                    current.isEmpty() -> listOf(it)
                                    else -> {
                                        val min = current.min()
                                        val max = current.max()
                                        when {
                                            (it > max) -> min..it
                                            (it < min) -> it..max
                                            else -> it..max
                                        }.toList()
                                    }
                                }
                                onSelected.invoke(selected)
                            },
                    )
                    if (index == values.size - 1) {
                        Spacer(modifier = Modifier.width(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun GenresLine(
    values: List<GenreItemResponse>,
    current: List<Int>,
    onSelected: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, top = 8.dp, bottom = 8.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(
                text = "${stringResource(id = R.string.genre)} : ",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                values.forEach {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = it.name ?: "",
                        color = if (it.id in current) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            shadow = Shadow(
                                color = Color.Black,
                                offset = Offset(0f, 0f),
                                blurRadius = 0.5f
                            )
                        ),
                        modifier = Modifier
                            .background(
                                if (it.id in current) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                                RoundedCornerShape(50)
                            )
                            .padding(horizontal = 12.dp, vertical = 6.dp)
                            .clickable {
                                it.id?.let { id -> onSelected.invoke(id) }
                            },
                    )
                }
            }
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
