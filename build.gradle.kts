// Top-level build file where you can add configuration options common to all sub-projects/modules.
buildscript {

    repositories {
        google()
    }
    dependencies {
        // Keep this for navigation safe args
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.9.0")
    }
}

plugins {
    // Make sure the Android Application plugin version matches your libs.versions.toml
    id("com.android.application") version "8.8.2" apply false
    id("com.google.gms.google-services") version "4.4.2" apply false
    id("org.jetbrains.kotlin.android") version "2.0.0" apply false // This should be good for Kotlin 2.0.0
}