package com.scanpang.app.data.remote

import android.util.Log
import com.scanpang.app.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private val BASE_URL: String = BuildConfig.SERVER_URL

    init {
        Log.d("RetrofitClient", "BASE_URL = $BASE_URL")
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(
            HttpLoggingInterceptor { message ->
                Log.d("OkHttp", message)
            }.apply {
                level = HttpLoggingInterceptor.Level.BODY
            },
        )
        .build()

    val api: ScanPangApi by lazy {
        Log.d("RetrofitClient", "Creating ScanPangApi with baseUrl=$BASE_URL")
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScanPangApi::class.java)
    }
}
