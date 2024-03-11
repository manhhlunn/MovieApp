package com.android.movieapp

import android.app.PictureInPictureParams
import android.content.Context
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.util.Rational
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.RequiresApi
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.dialog
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.android.movieapp.models.entities.Movie
import com.android.movieapp.models.entities.Person
import com.android.movieapp.models.entities.Tv
import com.android.movieapp.models.network.SearchResultItem
import com.android.movieapp.network.Api
import com.android.movieapp.ui.detail.MovieDetailScreen
import com.android.movieapp.ui.detail.PersonDetailScreen
import com.android.movieapp.ui.detail.TvDetailScreen
import com.android.movieapp.ui.ext.ZoomableImage
import com.android.movieapp.ui.home.HomeScreen
import com.android.movieapp.ui.home.KeyDetailScreen
import com.android.movieapp.ui.home.widget.LocalDarkTheme
import com.android.movieapp.ui.player.OMovieDetailScreen
import com.android.movieapp.ui.player.SuperStreamDetailScreen
import com.android.movieapp.ui.player.sendPauseBroadcast
import com.android.movieapp.ui.theme.MovieAppTheme
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.Serializable
import javax.inject.Inject

val LocalIsInPipMode = compositionLocalOf<Boolean> {
    error("LocalIsInPipMode is not provided")
}

@Composable
fun rememberPipMode() = rememberUpdatedState(LocalIsInPipMode.current)

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val viewModel: MainActivityViewModel by viewModels()

    private val wakeLock by lazy {
        (getSystemService(Context.POWER_SERVICE) as PowerManager).run {
            return@lazy newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyApp::MyWakelockTag")
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onPictureInPictureModeChanged(
        isInPictureInPictureMode: Boolean,
        newConfig: Configuration
    ) {
        viewModel.isInPipMode = isInPictureInPictureMode
        super.onPictureInPictureModeChanged(isInPictureInPictureMode, newConfig)
    }

    override fun onStop() {
        super.onStop()
        sendPauseBroadcast()
    }

    override fun onUserLeaveHint() {
        super.onUserLeaveHint()
        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
            && viewModel.isInPlayer.value
            && packageManager.hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)
        ) {
            enterPictureInPictureMode(
                with(PictureInPictureParams.Builder()) {
                    val width = 16
                    val height = 9
                    setAspectRatio(Rational(width, height))
                    build()
                }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        wakeLock.release()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        hideSystemBars()
    }

    private fun hideSystemBars() {
        val windowInsetsController =
            WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
        window.decorView.setOnApplyWindowInsetsListener { view, windowInsets ->
            view.onApplyWindowInsets(windowInsets)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        wakeLock.acquire(2 * 60 * 60 * 1000L)
        hideSystemBars()
        enableEdgeToEdge()
        setContent {
            val systemTheme = isSystemInDarkTheme()
            val isDarkTheme = remember { mutableStateOf(systemTheme) }
            val navController = rememberNavController()
            navController.addOnDestinationChangedListener { _, destination, _ ->
                viewModel.setPlayerModeState(
                    destination.route == NavScreen.SuperStreamMovieDetailScreen.routeWithArgument
                            || destination.route == NavScreen.OMovieDetailScreen.routeWithArgument
                )
            }

            CompositionLocalProvider(LocalDarkTheme provides isDarkTheme) {
                CompositionLocalProvider(LocalIsInPipMode provides viewModel.isInPipMode) {
                    MovieAppTheme(darkTheme = isDarkTheme.value) {
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background,
                        ) {
                            NavHost(
                                route = NavScreen.Root.route,
                                navController = navController,
                                startDestination = NavScreen.HomeScreen.route
                            ) {

                                composable(route = NavScreen.HomeScreen.route) {
                                    HomeScreen(navController = navController)
                                }

                                composable(
                                    route = NavScreen.MovieDetailScreen.routeWithArgument,
                                    arguments = listOf(
                                        navArgument(NavScreen.MovieDetailScreen.MOVIE_DETAIL) {
                                            type = NavType.StringType
                                        })
                                ) {
                                    MovieDetailScreen(
                                        navController = navController
                                    )
                                }

                                composable(
                                    route = NavScreen.TvDetailScreen.routeWithArgument,
                                    arguments = listOf(
                                        navArgument(NavScreen.TvDetailScreen.TV_DETAIL) {
                                            type = NavType.StringType
                                        })
                                ) {
                                    TvDetailScreen(
                                        navController = navController
                                    )
                                }

                                composable(
                                    route = NavScreen.PersonDetailScreen.routeWithArgument,
                                    arguments = listOf(
                                        navArgument(NavScreen.PersonDetailScreen.PERSON_DETAIL) {
                                            type = NavType.StringType
                                        })
                                ) {
                                    PersonDetailScreen(
                                        navController = navController
                                    )
                                }

                                composable(
                                    route = NavScreen.KeyDetailScreen.routeWithArgument,
                                    arguments = listOf(
                                        navArgument(NavScreen.KeyDetailScreen.KEY_DETAIL) {
                                            type = NavType.StringType
                                        })
                                ) {
                                    KeyDetailScreen(
                                        navController = navController
                                    )
                                }

                                composable(
                                    route = NavScreen.OMovieDetailScreen.routeWithArgument,
                                    arguments = listOf(
                                        navArgument(NavScreen.OMovieDetailScreen.O_MOVIE) {
                                            type = NavType.StringType
                                        })
                                ) {
                                    OMovieDetailScreen(
                                        navController = navController
                                    )
                                }

                                composable(
                                    route = NavScreen.SuperStreamMovieDetailScreen.routeWithArgument,
                                    arguments = listOf(
                                        navArgument(NavScreen.SuperStreamMovieDetailScreen.SS_MOVIE) {
                                            type = NavType.StringType
                                        })
                                ) {
                                    SuperStreamDetailScreen(
                                        navController = navController
                                    )
                                }


                                dialog(
                                    dialogProperties = DialogProperties(
                                        usePlatformDefaultWidth = false // experimental
                                    ),
                                    route = NavScreen.PreviewImageDialog.routeWithArgument,
                                    arguments = listOf(
                                        navArgument(NavScreen.PreviewImageDialog.IMAGE_URL) {
                                            type = NavType.StringType
                                        })
                                ) {
                                    val image =
                                        it.arguments?.getString(NavScreen.PreviewImageDialog.IMAGE_URL)

                                    image?.let { path ->
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
}


@HiltViewModel
class MainActivityViewModel @Inject constructor() : ViewModel() {

    var isInPipMode by mutableStateOf(false)

    private val _isInPlayer = MutableStateFlow(false)
    val isInPlayer = _isInPlayer.asStateFlow()

    fun setPlayerModeState(isInPlayer: Boolean) {
        _isInPlayer.value = isInPlayer
    }
}

sealed class NavScreen(val route: String) {

    data object Root : NavScreen("root")

    data object HomeScreen : NavScreen("home_screen")

    data object PreviewImageDialog : NavScreen("preview_image_dialog") {

        const val IMAGE_URL: String = "imageUrl"

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$IMAGE_URL}")
            }

        fun navigateWithArgument(image: String) = buildString {
            val json = Uri.encode(image)
            append(route)
            append("/$json")
        }
    }

    data object KeyDetailScreen : NavScreen("key_detail_screen") {

        const val KEY_DETAIL: String = "keyDetail"

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
                append("/{$KEY_DETAIL}")
            }

        fun navigateWithArgument(detail: KeyDetail) = buildString {
            val json = Uri.encode(Gson().toJson(detail))
            append(route)
            append("/$json")
        }

    }

    data object TvDetailScreen : NavScreen("tv_detail_screen") {

        const val TV_DETAIL: String = "tvDetail"

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$TV_DETAIL}")
            }

        fun navigateWithArgument(tv: Tv) = buildString {
            val json = Uri.encode(Gson().toJson(tv))
            append(route)
            append("/$json")
        }
    }

    data object PersonDetailScreen : NavScreen("person_detail_screen") {

        const val PERSON_DETAIL: String = "personDetail"

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$PERSON_DETAIL}")
            }

        fun navigateWithArgument(person: Person) = buildString {
            val json = Uri.encode(Gson().toJson(person))
            append(route)
            append("/$json")
        }
    }

    data object MovieDetailScreen : NavScreen("movie_detail_screen") {
        const val MOVIE_DETAIL: String = "movieDetail"

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$MOVIE_DETAIL}")
            }

        fun navigateWithArgument(movie: Movie) = buildString {
            val json = Uri.encode(Gson().toJson(movie))
            append(route)
            append("/$json")
        }
    }

    data object OMovieDetailScreen : NavScreen("o_movie_detail_screen") {
        const val O_MOVIE: String = "o_movie"

        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{${O_MOVIE}}")
            }

        fun navigateWithArgument(searchResultItem: SearchResultItem) = buildString {
            val json = Uri.encode(Gson().toJson(searchResultItem))
            append(route)
            append("/$json")
        }
    }

    data object SuperStreamMovieDetailScreen : NavScreen("super_stream_movie_detail_screen") {
        const val SS_MOVIE: String = "super_stream_movie"
        val routeWithArgument: String
            get() = buildString {
                append(route)
                append("/{$SS_MOVIE}")
            }

        fun navigateWithArgument(searchResultItem: SearchResultItem) = buildString {
            val json = Uri.encode(Gson().toJson(searchResultItem))
            append(route)
            append("/$json")
        }
    }


}

