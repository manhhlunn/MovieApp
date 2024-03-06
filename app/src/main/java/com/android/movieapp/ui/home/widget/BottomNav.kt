package com.android.movieapp.ui.home.widget

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.interaction.Interaction
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.android.movieapp.R
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

@Composable
fun BottomNavigationView(
    navController: NavController,
    modifier: Modifier = Modifier,
    items: List<BottomNavigationScreen>
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    NavigationBar(
        modifier = modifier.shadow(5.dp, RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)),
        containerColor = MaterialTheme.colorScheme.onPrimary
    ) {
        items.forEach { item ->
            val scale = animateFloatAsState(
                targetValue = if (currentRoute == item.route) 1.1f else 0.8f,
                animationSpec = tween(500), label = ""
            )
            NavigationBarItem(
                modifier = Modifier.scale(scale.value),
                icon = {
                    Box(contentAlignment = Alignment.Center, modifier = Modifier.size(24.dp)) {
                        Icon(
                            painter = painterResource(id = item.icon),
                            contentDescription = item.name,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                },
                alwaysShowLabel = false,
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = MaterialTheme.colorScheme.primary,
                    unselectedIconColor = MaterialTheme.colorScheme.secondary,
                    indicatorColor = MaterialTheme.colorScheme.onPrimary,
                ),
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        popUpTo(navController.graph.findStartDestination().id) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                interactionSource = NoRippleInteractionSource()
            )
        }
    }
}

enum class BottomNavigationScreen(
    val route: String,
    @DrawableRes val icon: Int,
    @StringRes val title: Int,
    val type: TypeScreen
) {
    TvScreen(
        "tv",
        R.drawable.tv_ic,
        R.string.tv_title,
        TypeScreen.POPULAR
    ),

    PersonScreen("person", R.drawable.person_ic, R.string.person_title, TypeScreen.POPULAR),

    MovieScreen("movie", R.drawable.movie_ic, R.string.movie_title, TypeScreen.POPULAR),

    TvFavoriteScreen(
        "fav_tv",
        R.drawable.tv_ic,
        R.string.tv_title,
        TypeScreen.FAVORITE
    ),

    MovieFavoriteScreen(
        "fav_movie",
        R.drawable.movie_ic,
        R.string.movie_title,
        TypeScreen.FAVORITE
    ),

    PersonFavoriteScreen(
        "fav_person",
        R.drawable.person_ic,
        R.string.person_title,
        TypeScreen.FAVORITE
    ),

    TvWatchedScreen(
        "watched_tv",
        R.drawable.tv_ic,
        R.string.tv_title,
        TypeScreen.WATCHED
    ),

    MovieWatchedScreen(
        "watched_movie", R.drawable.movie_ic, R.string.movie_title,
        TypeScreen.WATCHED
    ),

    TvFilterScreen(
        "filter_tv",
        R.drawable.tv_ic,
        R.string.tv_title,
        TypeScreen.FILTER
    ),

    MovieFilterScreen(
        "filter_movie", R.drawable.movie_ic, R.string.movie_title,
        TypeScreen.FILTER
    ),

    OMovieMediaScreen(
        "o_movie", R.drawable.ic_home, R.string.o_movie,
        TypeScreen.MEDIA
    ),

    SuperStreamMovieMediaScreen(
        "super_stream_movie", R.drawable.movie_ic, R.string.super_stream_movie,
        TypeScreen.MEDIA
    )
}

enum class TypeScreen {
    POPULAR,
    FAVORITE,
    FILTER,
    WATCHED,
    MEDIA
}


class NoRippleInteractionSource : MutableInteractionSource {

    override val interactions: Flow<Interaction> = emptyFlow()

    override suspend fun emit(interaction: Interaction) {}

    override fun tryEmit(interaction: Interaction) = true
}
