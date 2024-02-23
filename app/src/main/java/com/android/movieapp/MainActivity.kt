package com.android.movieapp

import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Person
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.network.ImageResponse
import com.android.movieapp.models.network.SearchResultItem
import com.android.movieapp.network.Api
import com.android.movieapp.ui.detail.MovieDetailScreen
import com.android.movieapp.ui.detail.OMovieDetailScreen
import com.android.movieapp.ui.detail.PersonDetailScreen
import com.android.movieapp.ui.detail.SuperStreamDetailScreen
import com.android.movieapp.ui.detail.TvDetailScreen
import com.android.movieapp.ui.ext.ZoomableImage
import com.android.movieapp.ui.home.HomeScreen
import com.android.movieapp.ui.home.KeyDetailScreen
import com.android.movieapp.ui.home.LocalDarkTheme
import com.android.movieapp.ui.theme.MovieAppTheme
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import java.io.Serializable

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val wakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            return@lazy newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock.release()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        wakeLock.acquire(2 * 60 * 60 * 1000L)
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
            view.onApplyWindowInsets(windowInsets)
        }
        enableEdgeToEdge()
        setContent {
            val systemTheme = isSystemInDarkTheme()
            val isDarkTheme = remember { mutableStateOf(systemTheme) }
            MovieAppTheme(darkTheme = isDarkTheme.value) {
                CompositionLocalProvider(
                    LocalDarkTheme provides isDarkTheme,
                ) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background,
                    ) {
                        val navController = rememberNavController()
                        NavHost(
                            navController = navController,
                            startDestination = NavScreen.HomeScreen.route
                        ) {

                            composable(route = NavScreen.HomeScreen.route) {
                                HomeScreen(navController = navController)
                            }

                            composable(
                                route = NavScreen.MovieDetailScreen.routeWithArgument,
                                arguments = listOf(
                                    navArgument(NavScreen.MovieDetailScreen.movieDetail) {
                                        type = NavScreen.MovieDetailScreen.MovieDetailType()
                                    })
                            ) {
                                MovieDetailScreen(
                                    navController = navController
                                )
                            }

                            composable(
                                route = NavScreen.OMovieDetailScreen.routeWithArgument,
                                arguments = listOf(
                                    navArgument(NavScreen.OMovieDetailScreen.slug) {
                                        type = NavType.StringType
                                    })
                            ) {

                                OMovieDetailScreen(
                                    navController = navController,
                                    viewModel = hiltViewModel()
                                )
                            }

                            composable(
                                route = NavScreen.SuperStreamMovieDetailScreen.routeWithArgument,
                                arguments = listOf(
                                    navArgument(NavScreen.SuperStreamMovieDetailScreen.superStreamMovie) {
                                        type =
                                            NavScreen.SuperStreamMovieDetailScreen.MyMovieDetailType()
                                    })
                            ) {
                                SuperStreamDetailScreen(
                                    navController = navController,
                                    viewModel = hiltViewModel()
                                )
                            }

                            composable(
                                route = NavScreen.TvDetailScreen.routeWithArgument,
                                arguments = listOf(
                                    navArgument(NavScreen.TvDetailScreen.tvDetail) {
                                        type = NavScreen.TvDetailScreen.TvDetailType()
                                    })
                            ) {
                                TvDetailScreen(
                                    navController = navController
                                )
                            }

                            composable(
                                route = NavScreen.PersonDetailScreen.routeWithArgument,
                                arguments = listOf(
                                    navArgument(NavScreen.PersonDetailScreen.personDetail) {
                                        type = NavScreen.PersonDetailScreen.PersonDetailType()
                                    })
                            ) {
                                PersonDetailScreen(
                                    navController = navController
                                )
                            }

                            composable(
                                route = NavScreen.KeyDetailScreen.routeWithArgument,
                                arguments = listOf(
                                    navArgument(NavScreen.KeyDetailScreen.keyDetail) {
                                        type = NavScreen.KeyDetailScreen.KeyDetailType()
                                    })
                            ) {
                                KeyDetailScreen(
                                    navController = navController
                                )
                            }

                            dialog(
                                dialogProperties = DialogProperties(
                                    usePlatformDefaultWidth = false // experimental
                                ),
                                route = NavScreen.PreviewImageDialog.routeWithArgument,
                                arguments = listOf(
                                    navArgument(NavScreen.PreviewImageDialog.imageUrl) {
                                        type = NavScreen.PreviewImageDialog.ImageType()
                                    })
                            ) {
                                val image =
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                        it.arguments?.getSerializable(
                                            NavScreen.PreviewImageDialog.imageUrl,
                                            ImageResponse::class.java
                                        )
                                    } else {
                                        @Suppress("DEPRECATION")
                                        it.arguments?.getSerializable(NavScreen.PreviewImageDialog.imageUrl) as? ImageResponse
                                    }

                                image?.filePath?.let { path ->
                                    ZoomableImage(
                                        imageUrl = Api.getOriginalPath(path),
                                        navController
                                    )
                                } ?: navController.popBackStack()
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class NavScreen(val route: String) {

    data object HomeScreen : NavScreen("home_screen")

    data object PreviewImageDialog : NavScreen("preview_image_dialog") {

        const val imageUrl: String = "imageUrl"

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$imageUrl}")
            }

        fun navigateWithArgument(image: ImageResponse) = buildString {
            val json = Uri.encode(Gson().toJson(image))
            append(route)
            append("/$json")
        }

        class ImageType : NavType<ImageResponse>(isNullableAllowed = false) {

            @Suppress("DEPRECATION")
            override fun get(bundle: Bundle, key: String): ImageResponse? {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable(key, ImageResponse::class.java)
                } else {
                    bundle.getSerializable(key) as? ImageResponse
                }
            }

            override fun parseValue(value: String): ImageResponse {
                return Gson().fromJson(value, ImageResponse::class.java)
            }

            override fun put(bundle: Bundle, key: String, value: ImageResponse) {
                bundle.putSerializable(key, value)
            }
        }
    }

    data object KeyDetailScreen : NavScreen("key_detail_screen") {

        const val keyDetail: String = "keyDetail"

        data class KeyDetail(
            val isMovie: Boolean = true,
            val name: String?,
            val genre: Int? = null,
            val keyword: Int? = null,
            val company: Int? = null,
        ) : Serializable

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$keyDetail}")
            }

        fun navigateWithArgument(detail: KeyDetail) = buildString {
            val json = Uri.encode(Gson().toJson(detail))
            append(route)
            append("/$json")
        }

        class KeyDetailType : NavType<KeyDetail>(isNullableAllowed = false) {

            @Suppress("DEPRECATION")
            override fun get(bundle: Bundle, key: String): KeyDetail? {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable(key, KeyDetail::class.java)
                } else {
                    bundle.getSerializable(key) as? KeyDetail
                }
            }

            override fun parseValue(value: String): KeyDetail {
                return Gson().fromJson(value, KeyDetail::class.java)
            }

            override fun put(bundle: Bundle, key: String, value: KeyDetail) {
                bundle.putSerializable(key, value)
            }
        }
    }

    data object MovieDetailScreen : NavScreen("movie_detail_screen") {
        const val movieDetail: String = "movieDetail"

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$movieDetail}")
            }

        fun navigateWithArgument(movie: Movie) = buildString {
            val json = Uri.encode(Gson().toJson(movie))
            append(route)
            append("/$json")
        }

        class MovieDetailType : NavType<Movie>(isNullableAllowed = false) {

            @Suppress("DEPRECATION")
            override fun get(bundle: Bundle, key: String): Movie? {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable(key, Movie::class.java)
                } else {
                    bundle.getSerializable(key) as? Movie
                }
            }

            override fun parseValue(value: String): Movie {
                return Gson().fromJson(value, Movie::class.java)
            }

            override fun put(bundle: Bundle, key: String, value: Movie) {
                bundle.putSerializable(key, value)
            }
        }
    }

    data object OMovieDetailScreen : NavScreen("o_movie_detail_screen") {
        const val slug: String = "slug"

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$slug}")
            }

        fun navigateWithArgument(slug: String) = buildString {
            append(route)
            append("/$slug")
        }
    }

    data object SuperStreamMovieDetailScreen : NavScreen("super_stream_movie_detail_screen") {
        const val superStreamMovie: String = "super_stream_movie"
        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$superStreamMovie}")
            }

        fun navigateWithArgument(searchResultItem: SearchResultItem) = buildString {
            val json = Uri.encode(Gson().toJson(searchResultItem))
            append(route)
            append("/$json")
        }

        class MyMovieDetailType : NavType<SearchResultItem>(isNullableAllowed = false) {
            @Suppress("DEPRECATION")
            override fun get(bundle: Bundle, key: String): SearchResultItem? {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getParcelable(key, SearchResultItem::class.java)
                } else {
                    bundle.getParcelable(key) as? SearchResultItem
                }
            }

            override fun parseValue(value: String): SearchResultItem {
                return Gson().fromJson(value, SearchResultItem::class.java)
            }

            override fun put(bundle: Bundle, key: String, value: SearchResultItem) {
                bundle.putParcelable(key, value)
            }
        }
    }

    data object TvDetailScreen : NavScreen("tv_detail_screen") {
        const val tvDetail: String = "tvDetail"

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$tvDetail}")
            }

        fun navigateWithArgument(tv: Tv) = buildString {
            val json = Uri.encode(Gson().toJson(tv))
            append(route)
            append("/$json")
        }

        class TvDetailType : NavType<Tv>(isNullableAllowed = false) {

            @Suppress("DEPRECATION")
            override fun get(bundle: Bundle, key: String): Tv? {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable(key, Tv::class.java)
                } else {
                    bundle.getSerializable(key) as? Tv
                }
            }

            override fun parseValue(value: String): Tv {
                return Gson().fromJson(value, Tv::class.java)
            }

            override fun put(bundle: Bundle, key: String, value: Tv) {
                bundle.putSerializable(key, value)
            }
        }
    }

    data object PersonDetailScreen : NavScreen("person_detail_screen") {
        const val personDetail: String = "personDetail"

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$personDetail}")
            }

        fun navigateWithArgument(person: Person) = buildString {
            val json = Uri.encode(Gson().toJson(person))
            append(route)
            append("/$json")
        }

        class PersonDetailType : NavType<Person>(isNullableAllowed = false) {

            @Suppress("DEPRECATION")
            override fun get(bundle: Bundle, key: String): Person? {
                return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    bundle.getSerializable(key, Person::class.java)
                } else {
                    bundle.getSerializable(key) as? Person
                }
            }

            override fun parseValue(value: String): Person {
                return Gson().fromJson(value, Person::class.java)
            }

            override fun put(bundle: Bundle, key: String, value: Person) {
                bundle.putSerializable(key, value)
            }
        }
    }
}

