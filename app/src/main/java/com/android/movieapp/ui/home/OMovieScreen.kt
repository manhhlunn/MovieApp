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
import androidx.compose.foundation.layout.height
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
import androidx.compose.ui.text.style.TextOverflow
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
import com.android.movieapp.models.network.MyMovie
import com.android.movieapp.models.network.OMovie
import com.android.movieapp.repository.OMovieRepository
import com.android.movieapp.ui.configure.SearchBar
import com.android.movieapp.ui.ext.ProgressiveGlowingImage
import com.android.movieapp.ui.ext.getColumnCount
import com.android.movieapp.ui.ext.ifNull
import com.android.movieapp.ui.media.FilterCategory
import com.android.movieapp.ui.media.FilterCountry
import com.android.movieapp.ui.media.MediaType
import com.android.movieapp.ui.media.SortTime
import dagger.hilt.android.lifecycle.HiltViewModel
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
import java.util.Calendar
import javax.inject.Inject

@Composable
fun OMovieScreen(navController: NavController) {
    val navControllerOMovie = rememberNavController()

    Scaffold(bottomBar = {
        BottomNavigationView(
            navController = navControllerOMovie,
            items = BottomNavigationScreen.entries.filter { it.type == TypeScreen.MEDIA }
        )
    }) {
        NavHost(
            modifier = Modifier
                .padding(
                    top = 12.dp,
                    bottom = it.calculateBottomPadding() - 32.dp
                ),
            navController = navControllerOMovie,
            startDestination = BottomNavigationScreen.NewMovieMediaScreen.route
        ) {

            composable(route = BottomNavigationScreen.NewMovieMediaScreen.route) {
                BaseOMovieScreen(
                    navController = navController,
                    hiltViewModel<OMovieViewModel>()
                )
            }

            composable(route = BottomNavigationScreen.MovieMediaSearchScreen.route) {
                BaseOMovieScreen(
                    navController = navController,
                    hiltViewModel<SearchOMovieViewModel>()
                )
            }

            composable(route = BottomNavigationScreen.MyMovieScreen.route) {
                MyMovieScreen(
                    navController = navController,
                    hiltViewModel<MyMovieViewModel>()
                )
            }
        }
    }
}

@HiltViewModel
class OMovieViewModel @Inject constructor(private val oMovieRepository: OMovieRepository) :
    BaseOMovieViewModel() {

    override val invoke: (OMovieFilter) -> Flow<PagingData<OMovie>> = {
        oMovieRepository.getMovies(
            it.mediaType,
            it.sortTime,
            it.filterCategory,
            it.filterCountry,
            it.year
        )
    }


    init {
        fetch(OMovieFilter())
    }
}


@HiltViewModel
class SearchOMovieViewModel @Inject constructor(private val oMovieRepository: OMovieRepository) :
    BaseOMovieViewModel() {

    override val invoke: (OMovieFilter) -> Flow<PagingData<OMovie>> = {
        oMovieRepository.searchMovie(
            it.query,
            it.sortTime,
            it.filterCategory,
            it.filterCountry,
            it.year
        )
    }


    init {
        fetch(OMovieFilter())
    }
}

@HiltViewModel
class MyMovieViewModel @Inject constructor(oMovieRepository: OMovieRepository) : ViewModel() {

    val items = oMovieRepository.getMyMovies().cachedIn(viewModelScope)
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun BaseOMovieScreen(navController: NavController, viewModel: BaseOMovieViewModel) {

    val values = viewModel.values.collectAsLazyPagingItems()
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = values.loadState.refresh is LoadState.Loading,
            onRefresh = {
                values.refresh()
            })

    Column {
        if (viewModel is SearchOMovieViewModel) {
            SearchBar(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                hint = stringResource(
                    R.string.search_movie
                )
            ) {
                viewModel.onQueryChange(it)
            }
        } else {
            FilterLine(
                name = "Danh sách",
                values = uiState.mediaTypes
            ) {
                Text(
                    text = it.displayName,
                    color = if (it == uiState.filter.mediaType) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        shadow = Shadow(
                            color = Color.Black,
                            offset = Offset(0f, 0f),
                            blurRadius = 0.5f
                        )
                    ),
                    modifier = Modifier
                        .background(
                            if (it == uiState.filter.mediaType) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                            RoundedCornerShape(50)
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .clickable {
                            viewModel.onMediaTypeChange(it)
                        },
                )
            }
        }
        FilterLine(
            name = "Sắp xếp",
            values = uiState.sortTimes
        ) {
            Text(
                text = it.displayName,
                color = if (it == uiState.filter.sortTime) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.bodyMedium.copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(0f, 0f),
                        blurRadius = 0.5f
                    )
                ),
                modifier = Modifier
                    .background(
                        if (it == uiState.filter.sortTime) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSecondary,
                        RoundedCornerShape(50)
                    )
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .clickable {
                        viewModel.onSortChange(it)
                    },
            )
        }
        FilterLine(
            name = "Thể loại",
            values = uiState.categories
        ) {
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
        ) {
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
        FilterLine(name = "Năm", values = uiState.years) {
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
                    .fillMaxSize()
                    .padding(bottom = 32.dp),
                columns = GridCells.Fixed(getColumnCount()),
                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 12.dp)
            ) {
                items(values.itemCount) { index ->
                    values[index]?.let { item ->
                        OMovieItemView(movie = item) {
                            it.slug?.let { slug ->
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
fun MyMovieScreen(navController: NavController, viewModel: MyMovieViewModel) {

    val values = viewModel.items.collectAsLazyPagingItems()

    val pullRefreshState =
        rememberPullRefreshState(
            refreshing = values.loadState.refresh is LoadState.Loading,
            onRefresh = {
                values.refresh()
            })

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
            items(values.itemCount) { index ->
                values[index]?.let { item ->
                    MyMovieItemView(movie = item) {
                        navController.navigate(
                            NavScreen.MyMovieDetailScreen.navigateWithArgument(
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

@Composable
fun OMovieItemView(
    modifier: Modifier = Modifier,
    movie: OMovie,
    onExpandDetails: (OMovie) -> Unit
) {
    Column(modifier = modifier
        .padding(4.dp)
        .clickable {
            onExpandDetails.invoke(movie)
        })
    {
        val posterUrl = "${MovieApp.baseImageUrl}uploads/movies/${movie.thumbUrl}"
        ProgressiveGlowingImage(
            url = posterUrl,
            glow = true
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = movie.name ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 1f
                )
            ),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun MyMovieItemView(
    modifier: Modifier = Modifier,
    movie: MyMovie,
    onExpandDetails: (MyMovie) -> Unit
) {
    Column(modifier = modifier
        .padding(4.dp)
        .clickable {
            onExpandDetails.invoke(movie)
        })
    {

        ProgressiveGlowingImage(
            url = movie.posterUrl ?: "",
            glow = true
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = movie.name ?: "",
            style = MaterialTheme.typography.bodyMedium.copy(
                shadow = Shadow(
                    color = Color.Black,
                    offset = Offset(0f, 0f),
                    blurRadius = 1f
                )
            ),
            color = MaterialTheme.colorScheme.primary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}


@Composable
fun <T> FilterLine(
    name: String,
    values: List<T>,
    itemContent: @Composable (T) -> Unit,
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
                text = "$name : ",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.Bold
            )
            Row(modifier = Modifier.horizontalScroll(rememberScrollState())) {
                values.forEach {
                    Spacer(modifier = Modifier.width(8.dp))
                    itemContent.invoke(it)
                }
            }
        }
    }
}


abstract class BaseOMovieViewModel : ViewModel() {

    abstract val invoke: (OMovieFilter) -> Flow<PagingData<OMovie>>

    private val _uiState = MutableStateFlow(UIStateOMovieScreen())
    val uiState: StateFlow<UIStateOMovieScreen> = _uiState
    private var currentFilter: OMovieFilter? = null
    private val filterQueryFlow = MutableSharedFlow<OMovieFilter>(replay = 1)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _values = filterQueryFlow
        .onEach { currentFilter = it }
        .flatMapLatest {
            invoke(it)
        }

    val values = _values.cachedIn(viewModelScope)

    private fun updateFilter(filter: OMovieFilter) {
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

    fun onSortChange(sortTime: SortTime) {
        _uiState.update {
            val newFilter = it.filter.copy(
                sortTime = sortTime
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
                mediaType = mediaType
            )
            updateFilter(newFilter)
            it.copy(
                filter = newFilter
            )
        }
    }

    fun fetch(filter: OMovieFilter) {
        viewModelScope.launch {
            val countries = async { getCountries() }
            val categories = async { getCategories() }
            val years = async { getYears() }
            _uiState.update {
                updateFilter(filter)
                it.copy(
                    filter = filter,
                    countries = countries.await(),
                    categories = categories.await(),
                    years = years.await()
                )
            }
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

data class OMovieFilter(
    val mediaType: MediaType = MediaType.PhimBo,
    val query: String = "",
    val sortTime: SortTime = SortTime.ThoiGianCapNhat,
    val filterCategory: FilterCategory? = null,
    val filterCountry: FilterCountry? = null,
    val year: Int? = null,
)

data class UIStateOMovieScreen(
    val filter: OMovieFilter = OMovieFilter(),
    val mediaTypes: List<MediaType> = MediaType.entries,
    val sortTimes: List<SortTime> = SortTime.entries,
    val categories: List<FilterCategory?> = emptyList(),
    val years: List<Int?> = emptyList(),
    val countries: List<FilterCountry?> = emptyList(),
)
