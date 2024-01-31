package com.android.movieapp.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import com.android.movieapp.db.MovieDao
import com.android.movieapp.db.PersonDao
import com.android.movieapp.db.TvDao
import com.android.movieapp.models.entities.FavoriteMovie
import com.android.movieapp.models.entities.FavoritePerson
import com.android.movieapp.models.entities.FavoriteTv
import com.android.movieapp.models.entities.WatchedMovie
import com.android.movieapp.models.entities.WatchedTv
import com.android.movieapp.network.Api


class FavoriteRepository(
    private val movieDao: MovieDao,
    private val tvDao: TvDao,
    private val personDao: PersonDao
) {

    fun favoriteMovies() = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            movieDao.getFavorites()
        }
    ).flow

    fun watchedMovies() = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            movieDao.getWatched()
        }
    ).flow

    fun favoriteTvs() = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            tvDao.getFavorites()
        }
    ).flow

    fun watchedTvs() = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            tvDao.getWatched()
        }
    ).flow

    fun favoritePersons() = Pager(
        config = PagingConfig(
            pageSize = Api.PAGING_SIZE,
            prefetchDistance = Api.PAGING_SIZE,
            initialLoadSize = Api.PAGING_SIZE,
        ),
        pagingSourceFactory = {
            personDao.getFavorites()
        }
    ).flow

    suspend fun isFavoriteMovie(id: Int): Boolean {
        return movieDao.isFavorite(id)
    }

    suspend fun isFavoriteTv(id: Int): Boolean {
        return tvDao.isFavorite(id)
    }

    suspend fun isFavoritePerson(id: Int): Boolean {
        return personDao.isExist(id)
    }

    suspend fun isWatchedMovie(id: Int): Boolean {
        return movieDao.isWatched(id)
    }

    suspend fun isWatchedTv(id: Int): Boolean {
        return tvDao.isWatched(id)
    }

    suspend fun addFavoriteMovie(fav: FavoriteMovie) {
        movieDao.insertFavorite(fav)
    }

    suspend fun deleteFavoriteMovie(id: Int) {
        movieDao.deleteFavorite(id)
    }

    suspend fun addWatchedMovie(watched: WatchedMovie) {
        movieDao.insertWatched(watched)
    }

    suspend fun deleteWatchedMovie(id: Int) {
        movieDao.deleteWatched(id)
    }

    suspend fun addWatchedTv(watched: WatchedTv) {
        tvDao.insertWatched(watched)
    }

    suspend fun deleteWatchedTv(id: Int) {
        tvDao.deleteWatched(id)
    }

    suspend fun addFavoriteTv(fav: FavoriteTv) {
        tvDao.insertFavorite(fav)
    }

    suspend fun deleteFavoriteTv(id: Int) {
        tvDao.deleteFavorite(id)
    }

    suspend fun addFavoritePerson(fav: FavoritePerson) {
        personDao.insertFavorite(fav)
    }

    suspend fun deleteFavoritePerson(id: Int) {
        personDao.deleteFavorite(id)
    }
}