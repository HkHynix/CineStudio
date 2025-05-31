// app/build.gradle.kts

plugins {
    alias(libs.plugins.android.application)
    // Applying the Kotlin Android plugin to this module.
    // Removed duplicate 'id("org.jetbrains.kotlin.android")' if it was present below alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.android)
    id("androidx.navigation.safeargs.kotlin")
    id("kotlin-parcelize")
    // If you are NOT using Firebase or other Google services that require google-services.json,
    // COMMENT OUT or REMOVE the line below:
    // id("com.google.gms.google-services") // Google Services plugin (for Firebase/Google APIs)
}

android {
    namespace = "com.harindu.cinestudio"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.harindu.cinestudio"
        minSdk = 25
        targetSdk = 35
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
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true // Keep dataBinding=true as you are using <data> in some XMLs
    }
}

dependencies {

    // Core Android Libraries
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material) // Material Design components
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0") // Keep for pull-to-refresh functionality

    // Navigation Component
    implementation("androidx.navigation:navigation-ui-ktx:2.7.6")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.6")

    // Retrofit for API calls
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    implementation("com.squareup.okhttp3:logging-interceptor:4.11.0") // Keep for network logging during development

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.7.0") // For ViewModelScope
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.7.0") // For lifecycle-aware coroutine scope

    // Image Loading - Glide
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0") // REQUIRED for Glide to work correctly

    // UI Components
    // implementation("com.google.android.material:material:1.11.0") // Redundant, already added via libs.material
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("androidx.recyclerview:recyclerview:1.3.2")
    implementation("com.facebook.shimmer:shimmer:0.5.0") // Keep for shimmer loading effects

    // Splash Screen (Keep if you intend to have a custom splash screen)
    implementation ("androidx.core:core-splashscreen:1.0.1")

    // Test Dependencies
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}