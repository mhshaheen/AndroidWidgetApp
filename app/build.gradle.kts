plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("org.jetbrains.kotlin.plugin.compose") version "2.0.0"
    id("kotlin-kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.example.androidwidgetapp"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.example.androidwidgetapp"
        minSdk = 24
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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

    buildFeatures {
        viewBinding = true
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    kapt {
        correctErrorTypes = true
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")

    /*---------------------------------------Dagger-Hilt-------------------------------------*/
    implementation("com.google.dagger:hilt-android:2.48")
    implementation("androidx.compose.material3:material3-android:1.3.2")
    implementation("androidx.media3:media3-common-ktx:1.7.1")
    implementation("androidx.activity:activity:1.10.1")
    kapt("com.google.dagger:hilt-android-compiler:2.48")

    /*----------------------------activity/fragment-viewModels--------------------------------*/
    implementation("androidx.fragment:fragment-ktx:1.6.2")
    implementation("androidx.lifecycle:lifecycle-extensions:2.2.0")

    /*-------------------------------------lifecycleScope-------------------------------------*/
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.lifecycle:lifecycle-livedata-ktx:2.8.7")

    /*-------------------------------------Room-Database-------------------------------------*/
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    kapt("androidx.room:room-compiler:2.6.1")

    /*-----------------------------------------Glide-----------------------------------------*/
    implementation("com.github.bumptech.glide:glide:4.16.0")

    /*-----------------------------------------Retrofit-----------------------------------------*/
    implementation("com.squareup.retrofit2:retrofit:2.11.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    /*-----------------------------------------Gson-----------------------------------------*/
    implementation("com.google.code.gson:gson:2.10.1")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")

    /*----------------------------------navigation nave host-------------------------------------*/
    implementation("androidx.navigation:navigation-fragment-ktx:2.8.3")
    implementation("androidx.navigation:navigation-ui-ktx:2.8.3")

    /*----------------------------------billing google-------------------------------------*/
    implementation("com.android.billingclient:billing-ktx:7.1.1")
    implementation("com.google.android.gms:play-services-ads:23.5.0")

    /*----------------------------------firebase crashlytics-------------------------------------*/
    implementation("com.google.firebase:firebase-crashlytics-buildtools:3.0.2")
    implementation(kotlin("reflect"))

    /*----------------------------------Paging-------------------------------------*/
    implementation("androidx.paging:paging-runtime-ktx:3.3.2")

    /*-----------------------------------Media3-------------------------------------*/
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-exoplayer-dash:1.8.0")
    implementation("androidx.media3:media3-exoplayer-hls:1.8.0")
    implementation("androidx.media3:media3-common:1.8.0")
    implementation("androidx.media3:media3-session:1.8.0")
    implementation("androidx.media3:media3-datasource:1.8.0")
    implementation("androidx.media3:media3-datasource-okhttp:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")
    implementation("androidx.media3:media3-ui-compose:1.8.0")

    implementation("androidx.activity:activity-compose:1.9.0")
    implementation("androidx.compose.ui:ui:1.5.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.5.0")
    implementation("androidx.compose.foundation:foundation:1.5.0")
//
//    // Coroutines
//    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    //AndroidStories
    //implementation(project(":stories"))

    implementation(project(":ketch"))
}