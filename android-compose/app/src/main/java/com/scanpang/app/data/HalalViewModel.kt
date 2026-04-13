package com.scanpang.app.data

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HalalViewModel : ViewModel() {

    private val _prayerTimes = MutableStateFlow<PrayerTimeData?>(null)
    val prayerTimes: StateFlow<PrayerTimeData?> = _prayerTimes.asStateFlow()

    private val _qibla = MutableStateFlow<QiblaData?>(null)
    val qibla: StateFlow<QiblaData?> = _qibla.asStateFlow()

    private val _restaurants = MutableStateFlow<List<HalalRestaurant>>(emptyList())
    val restaurants: StateFlow<List<HalalRestaurant>> = _restaurants.asStateFlow()

    private val _prayerRooms = MutableStateFlow<List<PrayerRoomDetail>>(emptyList())
    val prayerRooms: StateFlow<List<PrayerRoomDetail>> = _prayerRooms.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading: StateFlow<Boolean> = _loading.asStateFlow()

    init {
        loadPrayerTimesAndQibla()
    }

    fun loadPrayerTimesAndQibla() {
        viewModelScope.launch {
            try {
                val ptResp = ScanPangClient.api.halalQuery(
                    HalalRequest(category = "prayer_time")
                )
                _prayerTimes.value = ptResp.prayer_times

                val qResp = ScanPangClient.api.halalQuery(
                    HalalRequest(category = "qibla")
                )
                _qibla.value = qResp.qibla
            } catch (e: Exception) {
                Log.e("HalalVM", "기도시간/키블라 로드 실패: ${e.message}")
            }
        }
    }

    fun loadRestaurants(halalType: String = "") {
        viewModelScope.launch {
            _loading.value = true
            try {
                val resp = ScanPangClient.api.halalQuery(
                    HalalRequest(category = "restaurant", halal_type = halalType)
                )
                _restaurants.value = resp.restaurants
            } catch (e: Exception) {
                Log.e("HalalVM", "식당 로드 실패: ${e.message}")
            }
            _loading.value = false
        }
    }

    fun loadPrayerRooms() {
        viewModelScope.launch {
            _loading.value = true
            try {
                val resp = ScanPangClient.api.halalQuery(
                    HalalRequest(category = "prayer_room")
                )
                _prayerRooms.value = resp.prayer_rooms
            } catch (e: Exception) {
                Log.e("HalalVM", "기도실 로드 실패: ${e.message}")
            }
            _loading.value = false
        }
    }
}
