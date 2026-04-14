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

    init { loadPrayerTimesAndQibla() }

    fun loadPrayerTimesAndQibla() {
        viewModelScope.launch {
            try {
                _prayerTimes.value = ScanPangClient.api.halalQuery(HalalRequest(category = "prayer_time")).prayer_times
                _qibla.value = ScanPangClient.api.halalQuery(HalalRequest(category = "qibla")).qibla
            } catch (e: Exception) {
                Log.e("HalalVM", "기도시간/키블라 로드 실패: ${e.message}")
            }
        }
    }

    fun loadRestaurants(halalType: String = "") {
        viewModelScope.launch {
            _loading.value = true
            try {
                _restaurants.value = ScanPangClient.api.halalQuery(
                    HalalRequest(category = "restaurant", halal_type = halalType)
                ).restaurants
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
                _prayerRooms.value = ScanPangClient.api.halalQuery(
                    HalalRequest(category = "prayer_room")
                ).prayer_rooms
            } catch (e: Exception) {
                Log.e("HalalVM", "기도실 로드 실패: ${e.message}")
            }
            _loading.value = false
        }
    }
}
