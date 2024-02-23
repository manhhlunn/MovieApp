package com.android.movieapp.ds

import android.content.Context
import androidx.annotation.WorkerThread
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


private val Context.dataStore by preferencesDataStore("DatastoreDB")

@Singleton
class DataStoreManager @Inject constructor(@ApplicationContext appContext: Context) {

    val dataStore = appContext.dataStore
    var language by myDataStoreValue("en")
    var region by myDataStoreValue("KR")

    inline fun <reified T> get(name: String, default: T) = runBlocking {
        when (default) {
            is String -> dataStore.data.first()[stringPreferencesKey(name)] ?: default
            is Int -> dataStore.data.first()[intPreferencesKey(name)] ?: default
            is Boolean -> dataStore.data.first()[booleanPreferencesKey(name)] ?: default
            is Double -> dataStore.data.first()[doublePreferencesKey(name)] ?: default
            is Float -> dataStore.data.first()[floatPreferencesKey(name)] ?: default
            is Long -> dataStore.data.first()[longPreferencesKey(name)] ?: default
            else -> throw IllegalArgumentException("Not support data type ${T::class.java}")
        } as T
    }

    inline fun <reified T> set(
        name: String,
        value: T,
    ) = runBlocking<Unit> {
        dataStore.edit {
            when (value) {
                is String -> it[stringPreferencesKey(name)] = value
                is Int -> it[intPreferencesKey(name)] = value
                is Boolean -> it[booleanPreferencesKey(name)] = value
                is Double -> it[doublePreferencesKey(name)] = value
                is Float -> it[floatPreferencesKey(name)] = value
                is Long -> it[longPreferencesKey(name)] = value
                else -> throw IllegalArgumentException("Not support data type ${T::class.java}")
            }
        }
    }
}

inline fun <reified T : Any> myDataStoreValue(
    defaultValue: T,
) = object : ReadWriteProperty<DataStoreManager, T> {

    @WorkerThread
    override fun getValue(thisRef: DataStoreManager, property: KProperty<*>): T {
        return thisRef.get(name = property.name, default = defaultValue)
    }


    override fun setValue(thisRef: DataStoreManager, property: KProperty<*>, value: T) {
        thisRef.set(name = property.name, value = value)
    }
}







