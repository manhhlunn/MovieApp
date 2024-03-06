package com.android.movieapp.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
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
import com.android.movieapp.MovieApp
import com.android.movieapp.NavScreen
import com.android.movieapp.R
import com.android.movieapp.models.network.HomePageData
import com.android.movieapp.models.network.OMovie
import com.android.movieapp.models.network.SearchResultItem
import com.android.movieapp.repository.MediaRepository
import com.android.movieapp.ui.ext.SearchBar
import com.android.movieapp.ui.ext.getColumnCount
import com.android.movieapp.ui.ext.ifNull
import com.android.movieapp.ui.home.widget.BottomNavigationScreen
import com.android.movieapp.ui.home.widget.BottomNavigationView
import com.android.movieapp.ui.home.widget.FilterLine
import com.android.movieapp.ui.home.widget.HomeDrawerNavigation
import com.android.movieapp.ui.home.widget.MovieItemView
import com.android.movieapp.ui.home.widget.TypeScreen
import com.android.movieapp.ui.media.FilterCategory
import com.android.movieapp.ui.media.FilterCountry
import com.android.movieapp.ui.media.MediaType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

@Composable
fun MediaScreen(navController: NavController) {
    val navControllerMedia = rememberNavController()

    Scaffold(bottomBar = {
        BottomNavigationView(
            navController = navControllerMedia,
            items = BottomNavigationScreen.entries.filter { it.type == TypeScreen.MEDIA }
        )
    }) {
        NavHost(
            route = HomeDrawerNavigation.MediaScreen.route,
            modifier = Modifier.padding(it),
            navController = navControllerMedia,
            startDestination = BottomNavigationScreen.OMovieMediaScreen.route
        ) {
            composable(route = BottomNavigationScreen.OMovieMediaScreen.route) {
                OMovieScreen(navController = navController, viewModel = hiltViewModel())
            }

            composable(route = BottomNavigationScreen.SuperStreamMovieMediaScreen.route) {
                SuperStreamMovieScreen(navController = navController, viewModel = hiltViewModel())
            }
        }
    }
}

@HiltViewModel
class MediaViewModel @Inject constructor(private val mediaRepository: MediaRepository) :
    BaseMediaViewModel<OMovie>() {

    override val invoke: (MediaFilter) -> Flow<PagingData<OMovie>> = {
        if (it.query.isEmpty()) {
            mediaRepository.getOMovies(
                it.mediaType,
                it.filterCategory,
                it.filterCountry,
                it.year
            )
        } else {
            mediaRepository.searchOMovies(
                it.query,
                it.filterCategory,
                it.filterCountry,
                it.year
            )
        }
    }

    init {
        viewModelScope.launch {
            fetch(MediaFilter())
        }
    }
}

@HiltViewModel
class SuperStreamMovieViewModel @Inject constructor(private val mediaRepository: MediaRepository) :
    BaseMediaViewModel<SearchResultItem>() {

    override val invoke: (MediaFilter) -> Flow<PagingData<SearchResultItem>> = {
        if (it.query.isEmpty()) {
            mediaRepository.getSuperStreamMovies(type = it.type)
        } else {
            mediaRepository.searchSuperStreamMovies(it.query)
        }
    }

    init {
        viewModelScope.launch {
            val pageTypes = mediaRepository.getSuperStreamHomePageMovies()
            fetch(MediaFilter(type = pageTypes.first), pageTypes.second)
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun OMovieScreen(navController: NavController, viewModel: MediaViewModel) {

    val values = viewModel.values.collectAsLazyPagingItems()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = values.loadState.refresh is LoadState.Loading,
            onRefresh = {
                values.refresh()
            })

    Column {
        SearchBar(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            hint = stringResource(
                R.string.search_movie
            ),
            value = uiState.filter.query
        ) {
            viewModel.onQueryChange(it)
        }

        FilterLine(
            name = "Danh sách",
            values = uiState.mediaTypes
        ) { it, _ ->
            Text(
                text = it.displayName,
                color = if (it == uiState.filter.mediaType && uiState.filter.query.isEmpty()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 0f),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier
                    .background(
                        if (it == uiState.filter.mediaType && uiState.filter.query.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable {
                        viewModel.onMediaTypeChange(it)
                    },
            )
        }

        FilterLine(
            name = "Thể loại",
            values = uiState.categories
        ) { it, _ ->
            Text(
                text = it?.displayName ?: "Toàn bộ",
                color = if (it == uiState.filter.filterCategory) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 0f),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier
                    .background(
                        if (it == uiState.filter.filterCategory) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable {
                        viewModel.onCategoryChange(it)
                    },
            )
        }
        FilterLine(
            name = "Quốc gia",
            values = uiState.countries
        ) { it, _ ->
            Text(
                text = it?.displayName ?: "Toàn bộ",
                color = if (it == uiState.filter.filterCountry) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 0f),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier
                    .background(
                        if (it == uiState.filter.filterCountry) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable {
                        viewModel.onCountryChange(it)
                    },
            )
        }
        FilterLine(name = "Năm", values = uiState.years) { it, _ ->
            Text(
                text = it.ifNull("Toàn bộ"),
                color = if (it == uiState.filter.year) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 0f),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier
                    .background(
                        if (it == uiState.filter.year) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable {
                        viewModel.onYearChange(it)
                    },
            )
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
                        MovieItemView(
                            posterUrl = "${MovieApp.baseImageUrl}${item.thumbUrl}",
                            title = item.name.toString(),
                            bottomRight = item.quality
                        ) {
                            item.slug?.let { slug ->
                                navController.navigate(
                                    NavScreen.OMovieDetailScreen.navigateWithArgument(
                                        slug
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SuperStreamMovieScreen(navController: NavController, viewModel: SuperStreamMovieViewModel) {

    val values = viewModel.values.collectAsLazyPagingItems()
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = values.loadState.refresh is LoadState.Loading,
            onRefresh = {
                values.refresh()
            })

    Column {
        SearchBar(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            hint = stringResource(
                R.string.search_movie
            ),
            value = uiState.filter.query
        ) {
            viewModel.onQueryChange(it)
        }

        FilterLine(
            name = "Danh sách",
            values = uiState.pageTypes
        ) { it, _ ->
            Text(
                text = it.name.toString(),
                color = if (it.type != null && it.type == uiState.filter.type && uiState.filter.query.isEmpty()) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 0f),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier
                    .background(
                        if (it.type != null && it.type == uiState.filter.type && uiState.filter.query.isEmpty()) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable {
                        viewModel.onPageTypeChange(it.type)
                    },
            )
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
                        MovieItemView(
                            posterUrl = item.image.toString(),
                            title = item.title.toString(),
                            bottomRight = item.quality ?: item.imdbRating
                        ) {
                            navController.navigate(
                                NavScreen.SuperStreamMovieDetailScreen.navigateWithArgument(
                                    item
                                )
                            )
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


abstract class BaseMediaViewModel<T : Any> : ViewModel() {

    abstract val invoke: (MediaFilter) -> Flow<PagingData<T>>

    private val _uiState = MutableStateFlow(UIStateOMovieScreen())
    val uiState: StateFlow<UIStateOMovieScreen> = _uiState
    private var currentFilter: MediaFilter? = null
    private val filterQueryFlow = MutableSharedFlow<MediaFilter>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _values = filterQueryFlow
        .onEach { currentFilter = it }
        .flatMapLatest {
            invoke(it)
        }

    val values = _values.cachedIn(viewModelScope)

    private fun updateFilter(filter: MediaFilter) {
        if (filter != currentFilter) {
            filterQueryFlow.tryEmit(filter)
        }
    }

    fun onYearChange(year: Int?) {
        _uiState.update {
            val newFilter = it.filter.copy(
                year = year
            )
            updateFilter(newFilter)
            it.copy(
                filter = newFilter
            )
        }
    }

    fun onCountryChange(filterCountry: FilterCountry?) {
        _uiState.update {
            val newFilter = it.filter.copy(
                filterCountry = filterCountry
            )
            updateFilter(newFilter)
            it.copy(
                filter = newFilter
            )
        }
    }

    fun onCategoryChange(filterCategory: FilterCategory?) {
        _uiState.update {
            val newFilter = it.filter.copy(
                filterCategory = filterCategory
            )
            updateFilter(newFilter)
            it.copy(
                filter = newFilter
            )
        }
    }

    fun onQueryChange(query: String) {
        _uiState.update {
            val newFilter = it.filter.copy(
                query = query
            )
            updateFilter(newFilter)
            it.copy(
                filter = newFilter
            )
        }
    }

    fun onMediaTypeChange(mediaType: MediaType) {
        _uiState.update {
            val newFilter = it.filter.copy(
                mediaType = mediaType,
                query = ""
            )
            updateFilter(newFilter)
            it.copy(
                filter = newFilter
            )
        }
    }

    fun onPageTypeChange(type: String?) {
        if (type != uiState.value.filter.type) {
            _uiState.update {
                val newFilter = it.filter.copy(
                    type = type,
                    query = ""
                )
                updateFilter(newFilter)
                it.copy(
                    filter = newFilter
                )
            }
        }
    }

    suspend fun fetch(filter: MediaFilter, pageTypes: List<HomePageData> = emptyList()) =
        coroutineScope {
            val countries = async { getCountries() }
            val categories = async { getCategories() }
            val years = async { getYears() }
            _uiState.update {
                updateFilter(filter)
                it.copy(
                    filter = filter,
                    countries = countries.await(),
                    categories = categories.await(),
                    years = years.await(),
                    pageTypes = pageTypes
                )
            }
        }

    private fun getYears(): List<Int?> {
        val year = Calendar.getInstance().get(Calendar.YEAR)
        val minYear = 2010
        val years = mutableListOf<Int?>(null)
        repeat(year - minYear + 1) {
            years.add(year - it)
        }
        return years
    }

    private fun getCategories(): List<FilterCategory?> {
        val categories = mutableListOf<FilterCategory?>(null)
        categories.addAll(FilterCategory.entries)
        return categories
    }

    private fun getCountries(): List<FilterCountry?> {
        val countries = mutableListOf<FilterCountry?>(null)
        countries.addAll(FilterCountry.entries)
        return countries
    }

}

data class MediaFilter(
    val mediaType: MediaType = MediaType.PhimBo,
    val query: String = "",
    val filterCategory: FilterCategory? = null,
    val filterCountry: FilterCountry? = null,
    val year: Int? = null,
    val type: String? = null
)

data class UIStateOMovieScreen(
    val filter: MediaFilter = MediaFilter(),
    val mediaTypes: List<MediaType> = MediaType.entries,
    val categories: List<FilterCategory?> = emptyList(),
    val years: List<Int?> = emptyList(),
    val countries: List<FilterCountry?> = emptyList(),
    val pageTypes: List<HomePageData> = emptyList(),
)
