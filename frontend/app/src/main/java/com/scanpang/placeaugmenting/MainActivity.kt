package com.scanpang.placeaugmenting

import android.Manifest
import android.location.Location
import android.opengl.Matrix
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberMultiplePermissionsState
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.TrackingState
import io.github.sceneview.ar.ARScene
import io.github.sceneview.rememberEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import java.util.concurrent.TimeUnit
import java.util.Locale
import kotlin.math.roundToInt

// ── ScanPang 백엔드 데이터 클래스 ────────────────────────────────────────────

data class PlaceQueryRequest(
    val heading: Double,
    val user_lat: Double,
    val user_lng: Double,
    val user_alt: Double = 0.0,
    val pitch: Double = 0.0,
    val user_message: String = "이 건물에 대해 알려줘",
    val language: String = "ko"
)

data class FloorInfo(val floor: String, val stores: List<String>)

data class ArOverlay(
    val name: String,
    val category: String,
    val floor_info: List<FloorInfo>,
    val open_hours: String,
    val closed_days: String,
    val homepage: String,
    val parking_info: String,
    val admission_fee: String,
    val is_estimated: Boolean = false
)

data class Docent(val speech: String, val follow_up_suggestions: List<String>)

data class PlaceQueryResponse(val ar_overlay: ArOverlay, val docent: Docent)

interface ScanpangApi {
    @POST("place/query")
    suspend fun queryPlace(@Body request: PlaceQueryRequest): PlaceQueryResponse
}

private val scanpangApi: ScanpangApi by lazy {
    val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)  // 레이캐스팅은 즉시, LLM 도슨트 대기 여유
        .writeTimeout(15, TimeUnit.SECONDS)
        .connectionPool(ConnectionPool(5, 4, TimeUnit.SECONDS))  // uvicorn keep-alive(5s)보다 짧게
        .build()
    Retrofit.Builder()
        .baseUrl(BuildConfig.SERVER_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ScanpangApi::class.java)
}

// ── PlaceData ─────────────────────────────────────────────────────────────────

data class PlaceData(
    val id: String,
    val name: String,
    val details: String,
    val latitude: Double,
    val longitude: Double,
    val distance: Float,
    val arOverlay: ArOverlay? = null,
    val docentSpeech: String = ""
)

enum class RecognitionState {
    IDLE,
    SEARCHING,
    SUCCESS,
    FAILURE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MainAppScreen()
        }
    }
}


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun MainAppScreen() {
    val arPermissionsState = rememberMultiplePermissionsState(
        permissions = listOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    )

    if (arPermissionsState.allPermissionsGranted) {
        GeospatialARScreen()
    } else {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "AR 내비게이션 구동을 위해\n카메라와 위치 정보가 필요합니다.", textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = { arPermissionsState.launchMultiplePermissionRequest() }) {
                Text("권한 허용하기")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GeospatialARScreen() {
    val engine = rememberEngine()
    val coroutineScope = rememberCoroutineScope()
    val context = androidx.compose.ui.platform.LocalContext.current

    var recognitionStatus by remember { mutableStateOf(RecognitionState.IDLE) }
    var trackingMessage by remember { mutableStateOf("ARCore 초기화 중...") }
    val geospatialAnchors = remember { mutableMapOf<String, Anchor>() }
    var anchorScreenPositions by remember { mutableStateOf<Map<String, Offset>>(emptyMap()) }
    val dynamicPlaces = remember { mutableStateListOf<PlaceData>() }
    var selectedPlace by remember { mutableStateOf<PlaceData?>(null) }
    var triggerHitTest by remember { mutableStateOf(false) }
    var currentHeading by remember { mutableStateOf(0.0) }
    var currentAltitude by remember { mutableStateOf(0.0) }
    var currentPitch by remember { mutableStateOf(0.0) }
    // 한 번이라도 VPS 정밀(<1.5m) 상태에 도달했는지 — 이후 ARCore SLAM이 위치 유지
    var hasAchievedHighAccuracy by remember { mutableStateOf(false) }

    val verifiedCache = remember { mutableStateListOf<PlaceData>() }

    // ── TTS 초기화 ──────────────────────────────────────────────────────────────
    val tts = remember { mutableStateOf<TextToSpeech?>(null) }
    DisposableEffect(Unit) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) tts.value = instance
        }
        onDispose {
            instance?.stop()
            instance?.shutdown()
        }
    }

    fun speakDocent(speech: String, language: String = "en") {
        if (speech.isEmpty()) return
        val locale = when (language) {
            "ko" -> Locale.KOREAN
            "ja" -> Locale.JAPANESE
            "zh" -> Locale.CHINESE
            "ar" -> Locale("ar")
            else -> Locale.ENGLISH
        }
        tts.value?.language = locale
        tts.value?.speak(speech, TextToSpeech.QUEUE_FLUSH, null, "docent")
    }

    Box(modifier = Modifier.fillMaxSize()) {
        val configuration = androidx.compose.ui.platform.LocalConfiguration.current
        val density = androidx.compose.ui.platform.LocalDensity.current
        val screenWidthPx = with(density) { configuration.screenWidthDp.dp.toPx() }.toInt()
        val screenHeightPx = with(density) { configuration.screenHeightDp.dp.toPx() }.toInt()

        ARScene(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            planeRenderer = false,
            sessionConfiguration = { session, config ->
                config.geospatialMode = Config.GeospatialMode.ENABLED
                config.depthMode = Config.DepthMode.AUTOMATIC
            },
            onSessionUpdated = { session, frame ->
                val earth = session.earth ?: return@ARScene
                val camera = frame.camera

                if (earth.earthState == com.google.ar.core.Earth.EarthState.ENABLED) {
                    if (earth.trackingState == TrackingState.TRACKING) {

                        val pose = earth.cameraGeospatialPose
                        val userLat = pose.latitude
                        val userLng = pose.longitude
                        currentHeading = pose.heading
                        currentAltitude = pose.altitude

                        // 쿼터니언 (East-Up-South 좌표계)에서 카메라 pitch 추출
                        //   quat: [x=E, y=U, z=S, w]
                        //   forward 벡터 = (qz*2, -2*(q.y*q.z + q.w*q.x), -(1 - 2*(q.x²+q.y²)))
                        // 간단히 pitch = asin(-forward.y) 로 근사
                        val q = pose.eastUpSouthQuaternion
                        val fx = 2f * (q[0] * q[2] + q[3] * q[1])
                        val fy = 2f * (q[1] * q[2] - q[3] * q[0])
                        val fz = 1f - 2f * (q[0] * q[0] + q[1] * q[1])
                        val horiz = kotlin.math.sqrt(fx * fx + fz * fz)
                        currentPitch = Math.toDegrees(kotlin.math.atan2(-fy.toDouble(), horiz.toDouble()))

                        if (pose.horizontalAccuracy < 1.5) {
                            hasAchievedHighAccuracy = true
                        }

                        if (hasAchievedHighAccuracy) {
                            trackingMessage = "위치 파악 완료 (오차: ${"%.1f".format(pose.horizontalAccuracy)}m)"

                            val results = FloatArray(1)
                            for (i in dynamicPlaces.indices) {
                                Location.distanceBetween(userLat, userLng, dynamicPlaces[i].latitude, dynamicPlaces[i].longitude, results)
                                dynamicPlaces[i] = dynamicPlaces[i].copy(distance = results[0])
                            }

                            if (triggerHitTest) {
                                triggerHitTest = false
                                recognitionStatus = RecognitionState.SEARCHING

                                val hitResults = frame.hitTest(screenWidthPx / 2f, screenHeightPx / 2f)
                                var foundSurface = false

                                for (hitResult in hitResults) {
                                    val trackable = hitResult.trackable
                                    if (trackable is com.google.ar.core.Plane ||
                                        trackable is com.google.ar.core.Point ||
                                        trackable is com.google.ar.core.DepthPoint) {

                                        if (hitResult.distance > 1.5f) {
                                            val hitGeoPose = earth.getGeospatialPose(hitResult.hitPose)
                                            val targetLat = hitGeoPose.latitude
                                            val targetLng = hitGeoPose.longitude

                                            geospatialAnchors.values.forEach { it.detach() }
                                            geospatialAnchors.clear()
                                            dynamicPlaces.clear()

                                            var cachedPlace: PlaceData? = null
                                            for (cache in verifiedCache) {
                                                Location.distanceBetween(targetLat, targetLng, cache.latitude, cache.longitude, results)
                                                if (results[0] < 2.0f) {
                                                    cachedPlace = cache
                                                    break
                                                }
                                            }

                                            val newId = "Target_${System.currentTimeMillis()}"
                                            val newAnchor = earth.createAnchor(targetLat, targetLng, hitGeoPose.altitude, 0f, 0f, 0f, 1f)
                                            geospatialAnchors[newId] = newAnchor

                                            if (cachedPlace != null) {
                                                Location.distanceBetween(userLat, userLng, cachedPlace.latitude, cachedPlace.longitude, results)
                                                dynamicPlaces.add(cachedPlace.copy(id = newId, distance = results[0]))
                                                recognitionStatus = RecognitionState.SUCCESS
                                            } else {
                                                Location.distanceBetween(userLat, userLng, targetLat, targetLng, results)
                                                val placeData = PlaceData(
                                                    id = newId,
                                                    name = "분석 중...",
                                                    details = "서버와 통신 중.",
                                                    latitude = targetLat,
                                                    longitude = targetLng,
                                                    distance = results[0]
                                                )
                                                dynamicPlaces.add(placeData)

                                                coroutineScope.launch {
                                                    var arOverlay: ArOverlay? = null
                                                    var docentSpeech = ""
                                                    android.util.Log.d("SCANPANG", "API 호출: heading=${"%.1f".format(currentHeading)} pitch=${"%.1f".format(currentPitch)}")
                                                    try {
                                                        val response = scanpangApi.queryPlace(
                                                            PlaceQueryRequest(
                                                                heading = currentHeading,
                                                                user_lat = userLat,
                                                                user_lng = userLng,
                                                                user_alt = currentAltitude,
                                                                pitch = currentPitch,
                                                            )
                                                        )
                                                        android.util.Log.d("SCANPANG", "API 응답: name=${response.ar_overlay.name}, floor_info=${response.ar_overlay.floor_info.size}개")
                                                        arOverlay = response.ar_overlay
                                                        docentSpeech = response.docent.speech
                                                    } catch (e: Exception) {
                                                        android.util.Log.e("SCANPANG", "API 오류: ${e.javaClass.simpleName}: ${e.message}")
                                                        e.printStackTrace()
                                                    }

                                                    val index = dynamicPlaces.indexOfFirst { it.id == newId }
                                                    if (index != -1) {
                                                        val finalPlace = dynamicPlaces[index].copy(
                                                            name = arOverlay?.name?.takeIf { it.isNotEmpty() } ?: "알 수 없는 장소",
                                                            details = if (arOverlay != null) "open_hours: ${arOverlay.open_hours}" else "데이터 교차 검증 완료",
                                                            arOverlay = arOverlay,
                                                            docentSpeech = docentSpeech
                                                        )
                                                        dynamicPlaces[index] = finalPlace
                                                        verifiedCache.add(finalPlace)
                                                        recognitionStatus = RecognitionState.SUCCESS
                                                    }
                                                }
                                            }
                                            foundSurface = true
                                            break
                                        }
                                    }
                                }
                                if (!foundSurface && recognitionStatus == RecognitionState.SEARCHING) {
                                    // Hit test 실패 시 현재 GPS 위치로 앵커 직접 생성 (실외 fallback)
                                    val targetLat = userLat
                                    val targetLng = userLng
                                    val altitude = pose.altitude

                                    geospatialAnchors.values.forEach { it.detach() }
                                    geospatialAnchors.clear()
                                    dynamicPlaces.clear()

                                    var cachedPlace: PlaceData? = null
                                    for (cache in verifiedCache) {
                                        Location.distanceBetween(targetLat, targetLng, cache.latitude, cache.longitude, results)
                                        if (results[0] < 2.0f) {
                                            cachedPlace = cache
                                            break
                                        }
                                    }

                                    val newId = "Target_${System.currentTimeMillis()}"
                                    val newAnchor = earth.createAnchor(targetLat, targetLng, altitude, 0f, 0f, 0f, 1f)
                                    geospatialAnchors[newId] = newAnchor

                                    if (cachedPlace != null) {
                                        dynamicPlaces.add(cachedPlace.copy(id = newId, distance = 0f))
                                        recognitionStatus = RecognitionState.SUCCESS
                                    } else {
                                        val placeData = PlaceData(
                                            id = newId,
                                            name = "분석 중...",
                                            details = "서버와 통신 중.",
                                            latitude = targetLat,
                                            longitude = targetLng,
                                            distance = 0f
                                        )
                                        dynamicPlaces.add(placeData)

                                        coroutineScope.launch {
                                            var arOverlay: ArOverlay? = null
                                            var docentSpeech = ""
                                            android.util.Log.d("SCANPANG", "API 호출(fallback): heading=${"%.1f".format(currentHeading)} pitch=${"%.1f".format(currentPitch)}")
                                            try {
                                                val response = scanpangApi.queryPlace(
                                                    PlaceQueryRequest(
                                                        heading = currentHeading,
                                                        user_lat = userLat,
                                                        user_lng = userLng,
                                                        user_alt = currentAltitude,
                                                        pitch = currentPitch,
                                                    )
                                                )
                                                android.util.Log.d("SCANPANG", "API 응답(fallback): name=${response.ar_overlay.name}, floor_info=${response.ar_overlay.floor_info.size}개")
                                                arOverlay = response.ar_overlay
                                                docentSpeech = response.docent.speech
                                            } catch (e: Exception) {
                                                android.util.Log.e("SCANPANG", "API 오류(fallback): ${e.javaClass.simpleName}: ${e.message}")
                                                e.printStackTrace()
                                            }

                                            val index = dynamicPlaces.indexOfFirst { it.id == newId }
                                            if (index != -1) {
                                                val finalPlace = dynamicPlaces[index].copy(
                                                    name = arOverlay?.name?.takeIf { it.isNotEmpty() } ?: "알 수 없는 장소",
                                                    details = if (arOverlay != null) "open_hours: ${arOverlay.open_hours}" else "데이터 교차 검증 완료",
                                                    arOverlay = arOverlay,
                                                    docentSpeech = docentSpeech
                                                )
                                                dynamicPlaces[index] = finalPlace
                                                verifiedCache.add(finalPlace)
                                                recognitionStatus = RecognitionState.SUCCESS
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            trackingMessage = "VPS 정밀 탐색 중... 주변 건물을 비추세요 (현재 오차: ${"%.1f".format(pose.horizontalAccuracy)}m / 1.5m 미만 필요)"
                        }

                        val newPositions = mutableMapOf<String, Offset>()
                        val viewMatrix = FloatArray(16)
                        camera.getViewMatrix(viewMatrix, 0)
                        val projMatrix = FloatArray(16)
                        camera.getProjectionMatrix(projMatrix, 0, 0.1f, 100.0f)

                        geospatialAnchors.forEach { (id, anchor) ->
                            if (anchor.trackingState == TrackingState.TRACKING) {
                                val anchorPose = anchor.pose
                                val anchorTranslation = floatArrayOf(anchorPose.tx(), anchorPose.ty(), anchorPose.tz(), 1f)
                                val viewCoords = FloatArray(4)
                                Matrix.multiplyMV(viewCoords, 0, viewMatrix, 0, anchorTranslation, 0)

                                if (viewCoords[2] <= 0) {
                                    val clipCoords = FloatArray(4)
                                    Matrix.multiplyMV(clipCoords, 0, projMatrix, 0, viewCoords, 0)
                                    if (clipCoords[3] != 0f) {
                                        val x = ((clipCoords[0] / clipCoords[3] + 1.0f) / 2.0f) * screenWidthPx
                                        val y = ((1.0f - clipCoords[1] / clipCoords[3]) / 2.0f) * screenHeightPx
                                        newPositions[id] = Offset(x, y)
                                    }
                                }
                            }
                        }
                        anchorScreenPositions = newPositions

                    } else {
                        trackingMessage = "ARCore 트래킹 대기 중... 스마트폰을 천천히 움직이세요."
                    }
                } else {
                    trackingMessage = "GCP 서버 통신 실패 또는 VPS 미지원 지역입니다."
                }
            }
        )

        Box(modifier = Modifier.align(Alignment.Center).size(12.dp).background(Color.White.copy(alpha = 0.5f), CircleShape))

        anchorScreenPositions.forEach { (id, offset) ->
            val placeInfo = dynamicPlaces.find { it.id == id }
            if (placeInfo != null) {
                Column(
                    modifier = Modifier
                        .offset { IntOffset(offset.x.roundToInt(), offset.y.roundToInt()) }
                        .background(Color.Black.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                        .clickable { selectedPlace = placeInfo }
                        .padding(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val isPending = placeInfo.name.contains("분석 중")
                    Text(
                        text = if (isPending) "⏳ 분석 중..." else "📍 ${placeInfo.name}",
                        color = if (isPending) Color.Yellow else Color.Cyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                    if (!isPending) {
                        Text(text = "거리: ${"%.1f".format(placeInfo.distance)}m", color = Color.White, fontSize = 10.sp)
                    }
                }
            }
        }

        Column(modifier = Modifier.align(Alignment.TopCenter).padding(top = 40.dp).fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            Card(colors = CardDefaults.cardColors(containerColor = Color.Black.copy(alpha = 0.6f))) {
                Text(text = trackingMessage, color = Color.White, modifier = Modifier.padding(12.dp), textAlign = TextAlign.Center, fontSize = 14.sp)
            }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedVisibility(visible = recognitionStatus != RecognitionState.IDLE, enter = fadeIn(), exit = fadeOut()) {
                val statusText = when(recognitionStatus) {
                    RecognitionState.SEARCHING -> "⏳ 거리를 측정하여 연산 중..."
                    RecognitionState.SUCCESS -> "✅ 건물 식별 및 거리 갱신 완료!"
                    RecognitionState.FAILURE -> "❌ 추출 실패: 대상을 찾을 수 없음."
                    else -> ""
                }
                val statusColor = if(recognitionStatus == RecognitionState.SUCCESS) Color.Green else if(recognitionStatus == RecognitionState.FAILURE) Color.Red else Color.Yellow
                Text(text = statusText, color = statusColor, fontWeight = FontWeight.Bold, modifier = Modifier.background(Color.Black.copy(alpha = 0.4f)).padding(8.dp))
            }
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 60.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "💡 정확한 인식을 위해 유리가 아닌 불투명한 벽면을 조준하세요.",
                color = Color.White,
                fontSize = 12.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.background(Color.Black.copy(alpha = 0.5f), RoundedCornerShape(4.dp)).padding(8.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = { triggerHitTest = true },
                modifier = Modifier.height(56.dp).fillMaxWidth(0.7f),
                shape = RoundedCornerShape(28.dp),
                enabled = recognitionStatus != RecognitionState.SEARCHING && hasAchievedHighAccuracy
            ) {
                if (recognitionStatus == RecognitionState.SEARCHING) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White, strokeWidth = 2.dp)
                } else if (!hasAchievedHighAccuracy) {
                    Text("VPS 정밀 탐색 중...", fontWeight = FontWeight.Bold)
                } else {
                    Text("바라보는 지점 정보 가져오기", fontWeight = FontWeight.Bold)
                }
            }
        }

        if (selectedPlace != null) {
            val currentPlace = dynamicPlaces.find { it.id == selectedPlace!!.id } ?: selectedPlace!!
            val overlay = currentPlace.arOverlay

            val sheetBg = Color(0xFF1C1C1E)
            val dividerColor = Color(0xFF3A3A3C)
            val floorColors = listOf(
                Color(0xFFFF6B6B), Color(0xFF4ECDC4), Color(0xFFFFBE0B),
                Color(0xFFFF006E), Color(0xFF8338EC), Color(0xFF3A86FF),
                Color(0xFF06D6A0), Color(0xFFFF9F1C)
            )

            ModalBottomSheet(
                onDismissRequest = { selectedPlace = null },
                containerColor = sheetBg
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 20.dp)
                        .padding(bottom = 48.dp)
                ) {
                    // 건물명 + 거리
                    Text(
                        text = currentPlace.name,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    // 카테고리 뱃지 + 거리
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!overlay?.category.isNullOrEmpty()) {
                            Box(
                                modifier = Modifier
                                    .background(Color(0xFF2C2C2E), RoundedCornerShape(12.dp))
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(text = overlay!!.category, color = Color.LightGray, fontSize = 12.sp)
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "·", color = Color.Gray, fontSize = 13.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "${"%.0f".format(currentPlace.distance)}m",
                            color = Color.LightGray,
                            fontSize = 13.sp
                        )
                    }

                    // AI 추정 정보 경고 뱃지
                    if (overlay?.is_estimated == true) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color(0xFF3B2A00), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("⚠️", fontSize = 14.sp)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "AI가 이미지를 분석한 추정 정보입니다. 정확하지 않을 수 있습니다.",
                                color = Color(0xFFFFBE0B),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))
                    Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                    Spacer(modifier = Modifier.height(20.dp))

                    // 층별 정보
                    if (!overlay?.floor_info.isNullOrEmpty()) {
                        Text(
                            text = "층별 정보",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))

                        overlay!!.floor_info.forEachIndexed { index, floorInfo ->
                            val badgeColor = floorColors[index % floorColors.size]
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 5.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(width = 40.dp, height = 32.dp)
                                        .background(badgeColor, RoundedCornerShape(8.dp)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = floorInfo.floor,
                                        color = Color.White,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = floorInfo.stores.joinToString(", "),
                                    color = Color.LightGray,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1f)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(20.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // 도슨트 재생 버튼 — 관광지/문화시설 카테고리만 표시
                    val touristCategories = listOf("관광", "문화", "공연", "극장", "성당", "교회", "박물관", "미술관", "기념관", "사찰")
                    val isTouristPlace = touristCategories.any { overlay?.category?.contains(it) == true }
                    if (currentPlace.docentSpeech.isNotEmpty() && isTouristPlace) {
                        Spacer(modifier = Modifier.height(4.dp))
                        Button(
                            onClick = { speakDocent(currentPlace.docentSpeech) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3A86FF))
                        ) {
                            Text("🎙️ 도슨트 해설 듣기", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(16.dp))
                        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(dividerColor))
                        Spacer(modifier = Modifier.height(20.dp))
                    }

                    // 이용 정보
                    val hasInfo = listOf(
                        overlay?.open_hours,
                        overlay?.closed_days,
                        overlay?.admission_fee,
                        overlay?.parking_info
                    ).any { !it.isNullOrEmpty() }

                    if (hasInfo) {
                        Text(
                            text = "이용 정보",
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        if (!overlay?.open_hours.isNullOrEmpty())
                            PlaceInfoRow(label = "운영 시간", value = overlay!!.open_hours)
                        if (!overlay?.closed_days.isNullOrEmpty())
                            PlaceInfoRow(label = "휴무일", value = overlay!!.closed_days)
                        if (!overlay?.admission_fee.isNullOrEmpty())
                            PlaceInfoRow(label = "입장료", value = overlay!!.admission_fee)
                        if (!overlay?.parking_info.isNullOrEmpty())
                            PlaceInfoRow(label = "주차", value = overlay!!.parking_info)
                    }
                }
            }
        }
    }
}

@Composable
fun PlaceInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            color = Color.Gray,
            fontSize = 12.sp,
            modifier = Modifier.width(72.dp)
        )
        Text(
            text = value,
            color = Color.LightGray,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )
    }
}