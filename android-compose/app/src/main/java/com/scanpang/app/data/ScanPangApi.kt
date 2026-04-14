package com.scanpang.app.data

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

interface ScanPangApiService {
    @POST("halal/query")
    suspend fun halalQuery(@Body request: HalalRequest): HalalResponse
}

object ScanPangClient {
    // 에뮬레이터: 10.0.2.2 = 호스트 머신의 localhost
    // 실기기 USB: adb reverse tcp:8000 tcp:8000 → localhost
    private const val BASE_URL = "http://10.0.2.2:8000/"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: ScanPangApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ScanPangApiService::class.java)
    }
}

data class HalalRequest(
    val category: String = "",
    val message: String = "",
    val lat: Double = 37.5636,
    val lng: Double = 126.9822,
    val language: String = "ko",
    val halal_type: String = "",
    val radius: Int = 0,
)

data class HalalResponse(
    val speech: String,
    val category: String,
    val language: String,
    val prayer_times: PrayerTimeData?,
    val qibla: QiblaData?,
    val restaurants: List<HalalRestaurant>,
    val prayer_rooms: List<PrayerRoomDetail>,
)

data class PrayerTimeData(
    val fajr: String, val dhuhr: String, val asr: String,
    val maghrib: String, val isha: String,
    val hijri_date: String, val gregorian_date: String,
)

data class QiblaData(val direction: Double, val lat: Double, val lng: Double)

data class HalalRestaurant(
    val restaurant_id: String, val name_ko: String, val name_en: String,
    val halal_type: String, val muslim_cooks_available: Boolean?,
    val no_alcohol_sales: Boolean?, val cuisine_type: List<String>,
    val menu_examples: List<String>, val distance_m: Double,
    val lat: Double, val lng: Double, val address: String,
    val phone: String, val opening_hours: String,
    val break_time: String, val last_order: String,
)

data class PrayerRoomDetail(
    val name: String, val name_en: String, val distance_m: Double,
    val lat: Double, val lng: Double, val address: String,
    val floor: String, val open_hours: String,
    val facilities: Map<String, Boolean>, val availability_status: String,
)
