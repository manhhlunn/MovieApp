package com.android.movieapp.db.converters

import androidx.room.TypeConverter
import com.android.movieapp.models.network.SearchResultItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

open class MovieDataConverter {

    @TypeConverter
    fun fromString(value: String): SearchResultItem? {
        val listType = object : TypeToken<SearchResultItem>() {}.type
        return Gson().fromJson<SearchResultItem>(value, listType)
    }

    @TypeConverter
    fun toString(searchResultItem: SearchResultItem): String {
        val gson = Gson()
        return gson.toJson(searchResultItem)
    }

}
