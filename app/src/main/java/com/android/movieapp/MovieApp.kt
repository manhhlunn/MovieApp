package com.android.movieapp

import android.app.Application
import com.google.firebase.ktx.Firebase
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfig
import com.google.firebase.remoteconfig.ktx.remoteConfigSettings
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MovieApp : Application() {

    companion object {
        lateinit var baseURL: String
        lateinit var baseImageUrl: String
    }


    override fun onCreate() {
        super.onCreate()
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings { minimumFetchIntervalInSeconds = 0 }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(R.xml.remote_config_defaults)
        baseURL = remoteConfig.getString("baseUrl")
        baseImageUrl = remoteConfig.getString("baseImageUrl")
        remoteConfig.fetchAndActivate().addOnCompleteListener {
            if (it.isSuccessful) {
                baseURL = FirebaseRemoteConfig.getInstance().getString("baseUrl")
                baseImageUrl = FirebaseRemoteConfig.getInstance().getString("baseImageUrl")
            }
        }
    }
}





