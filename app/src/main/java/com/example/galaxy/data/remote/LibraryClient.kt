package com.example.galaxy.data.remote

import com.example.galaxy.data.remote.api.LibraryApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object LibraryClient {

    private const val BASE_URL = "https://lib.jj.ac.kr/"

    private val json = Json { ignoreUnknownKeys = true }

    val api: LibraryApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .readTimeout(10, TimeUnit.SECONDS)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(LibraryApi::class.java)
    }
}
