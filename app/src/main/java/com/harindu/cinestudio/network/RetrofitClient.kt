package com.harindu.cinestudio.network

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import com.harindu.cinestudio.data.api.TmdbApiService

/**
 * Singleton object to provide a configured Retrofit client for API calls.
 */
object RetrofitClient {

    private const val BASE_URL = "https://api.themoviedb.org/3/"

    // Create a logging interceptor for HTTP request and response logging
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY // Log request and response bodies
    }

    // Configure OkHttpClient with the logging interceptor
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(30, TimeUnit.SECONDS) // Connection timeout
        .readTimeout(30, TimeUnit.SECONDS)    // Read timeout
        .writeTimeout(30, TimeUnit.SECONDS)   // Write timeout
        .build()

    // Build the Retrofit instance using OkHttpClient and GsonConverterFactory
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create()) // Use Gson for JSON serialization/deserialization
            .build()
    }

    /**
     * Provides the TmdbApiService instance for making API calls.
     */
    val tmdbApiService: TmdbApiService by lazy {
        retrofit.create(TmdbApiService::class.java)
    }
}