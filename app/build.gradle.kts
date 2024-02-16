plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.dagger.hilt.android")
    id("kotlin-parcelize")
    id("com.google.devtools.ksp")
    id("com.google.gms.google-services")
}

android {
    signingConfigs {
        create("release") {
            storeFile =
                file("C:\\Users\\LEGION\\AndroidStudioProjects\\MovieApp\\movie_app_keystore.jks")
            storePassword = "31072001"
            keyAlias = "movieapp31"
            keyPassword = "31072001"
        }
    }
    namespace = "com.android.movieapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.android.movieapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        ksp {
            arg("room.schemaLocation", "$projectDir/schemas")
        }
        signingConfig = signingConfigs.getByName("debug")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = JavaVersion.VERSION_17.toString()
    }
    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

val navVersion = "2.7.7"
val hiltNavVersion = "1.1.0"
val hiltVersion = "2.49"
val pagingVersion = "3.2.1"
val roomVersion = "2.6.1"
val retrofitVersion = "2.9.0"
val okHttpLoggingVersion = "4.12.0"
val gsonVersion = "2.10.1"
val coilVersion = "2.4.0"
val datastoreVersion = "1.0.0"
val media3Version = "1.2.1"

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0")
    implementation ("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
    implementation("androidx.activity:activity-compose:1.8.2")
    implementation("androidx.compose.material:material:1.6.1")
    implementation(platform("androidx.compose:compose-bom:2023.03.00"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3-android:1.2.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.0.1")
    implementation("androidx.browser:browser:1.7.0")
    implementation("com.google.firebase:firebase-config:21.6.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2023.03.00"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    implementation("com.google.dagger:hilt-android:$hiltVersion")
    ksp ("com.google.dagger:hilt-android-compiler:$hiltVersion")
    implementation("androidx.navigation:navigation-compose:$navVersion")
    implementation("androidx.hilt:hilt-navigation-compose:$hiltNavVersion")

    implementation("androidx.paging:paging-runtime-ktx:$pagingVersion")
    implementation("androidx.paging:paging-compose:$pagingVersion")

    implementation ("androidx.room:room-runtime:$roomVersion")
    implementation ("androidx.room:room-ktx:$roomVersion")
    implementation ("androidx.room:room-paging:$roomVersion")
    ksp ("androidx.room:room-compiler:$roomVersion")

    implementation ("com.squareup.retrofit2:retrofit:$retrofitVersion")
    implementation ("com.squareup.retrofit2:converter-gson:$retrofitVersion")
    implementation ("com.squareup.okhttp3:logging-interceptor:$okHttpLoggingVersion")

    implementation ("com.google.code.gson:gson:$gsonVersion")
    implementation ("io.coil-kt:coil-compose:$coilVersion")

    implementation("androidx.datastore:datastore-preferences:$datastoreVersion")
    implementation("androidx.datastore:datastore:$datastoreVersion")

    implementation ("androidx.media3:media3-exoplayer-hls:$media3Version")
    implementation ("androidx.media3:media3-exoplayer-dash:$media3Version")
    implementation ("androidx.media3:media3-exoplayer:$media3Version")
    implementation ("androidx.media3:media3-ui:$media3Version")

    implementation ("com.github.iammannan:TranslateAPI:1.1")
    implementation ("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
    implementation("com.google.ai.client.generativeai:generativeai:0.2.0")
}