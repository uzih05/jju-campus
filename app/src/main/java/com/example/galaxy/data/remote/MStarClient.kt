package com.example.galaxy.data.remote

import android.content.Context
import com.example.galaxy.BuildConfig
import com.example.galaxy.data.remote.api.MStarApi
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import java.util.concurrent.TimeUnit

object MStarClient {

    private const val BASE_URL = "https://mnode.jj.ac.kr"

    private val json = Json { ignoreUnknownKeys = true }

    private var tokenStore: TokenStore? = null

    fun init(context: Context) {
        tokenStore = TokenStore(context)
    }

    val api: MStarApi by lazy {
        val logging = HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY
                    else HttpLoggingInterceptor.Level.NONE
        }

        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val token = tokenStore?.let {
                    kotlinx.coroutines.runBlocking { it.getAccessToken() }
                }
                val request = if (token != null) {
                    chain.request().newBuilder()
                        .addHeader("Authorization", "Bearer $token")
                        .build()
                } else chain.request()
                chain.proceed(request)
            }
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(MStarApi::class.java)
    }
}
