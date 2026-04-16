@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.scanpang.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Mosque
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.scanpang.app.data.DummyData
import com.scanpang.app.data.Place
import com.scanpang.app.data.SavedPlaceNavTarget
import com.scanpang.app.data.galleryModels
import com.scanpang.app.data.remote.ScanPangViewModel
import com.scanpang.app.data.toPlace
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

@Composable
fun PrayerRoomDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
    placeName: String = "",
) {
    val viewModel: ScanPangViewModel = viewModel()
    val apiPrayerRooms by viewModel.prayerRooms.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) { viewModel.loadPrayerRooms() }

    val apiPlace = remember(apiPrayerRooms, placeName) {
        val fromApi = apiPrayerRooms.map { it.toPlace() }
        if (placeName.isNotBlank()) {
            fromApi.firstOrNull { it.name == placeName }
        } else {
            fromApi.firstOrNull()
        }
    }

    if (apiPlace == null && isLoading) {
        Box(
            modifier = modifier.fillMaxSize().background(ScanPangColors.Surface),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(color = ScanPangColors.Primary)
        }
        return
    }

    val place = apiPlace
        ?: DummyData.prayerRooms.firstOrNull { it.name == placeName }
        ?: DummyData.prayerRooms.first()
    val imageModels = remember(place.id) { place.galleryModels(defaultPlaceDetailGallery()) }
    val pagerState = rememberPagerState(pageCount = { imageModels.size })
    val bookmark = rememberDetailBookmark(
        placeId = place.id,
        placeName = place.name,
        category = place.category,
        distanceLine = "${place.category} · ${place.distance}",
        tags = place.tags,
        target = SavedPlaceNavTarget.PrayerRoom,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScanPangColors.Surface)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        DetailHeroPhotoPager(
            gallery = imageModels,
            pagerState = pagerState,
            onBack = { navController.popBackStack() },
            onFullscreenClick = null,
        )
        Column(
            modifier = Modifier.padding(horizontal = ScanPangDimens.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(ScanPangDimens.detailSectionSpacing),
        ) {
            Spacer(modifier = Modifier.height(ScanPangSpacing.md))
            DetailTitleBookmarkRow(
                title = place.name,
                bookmarked = bookmark.bookmarked,
                onBookmarkClick = bookmark.onToggle,
            )
            DetailCategoryTagDistanceRow(
                categoryLabel = place.category,
                distanceText = place.distance,
                trailing = {
                    AvailabilityDotRow(place = place)
                },
            )
            DetailNavigateAndSideIconRow(
                onNavigate = {
                    navController.navigate(AppRoutes.arNavMapRoute(place.name)) { launchSingleTop = true }
                },
                sideIcon = Icons.Rounded.Mosque,
                sideContentDescription = "키블라 방향",
                onSideClick = {
                    navController.navigate(AppRoutes.Qibla) { launchSingleTop = true }
                },
            )
            DetailVisitCardsHorizontalPager(cards = place.detailVisitCardsFromPlace())
            DetailScreenDivider()
            DetailSectionHeader(title = "편의시설")
            DetailFacilityTagRow(tags = place.tags)
            DetailScreenDivider()
            DetailSectionHeader(title = "소개")
            DetailIntroBody(text = place.description)
            DetailSectionHeader(title = "상세 정보")
            DetailInfoLine(Icons.Rounded.Place, "주소", place.address)
            DetailInfoLine(Icons.Rounded.AccessTime, "운영시간", place.openHours)
            if (place.phone.isNotBlank()) {
                DetailInfoLine(Icons.Rounded.Phone, "전화번호", place.phone)
            }
            DetailContentBottomSpacer()
        }
    }
}

@Composable
private fun AvailabilityDotRow(place: Place) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(ScanPangDimens.icon10)
                .clip(CircleShape)
                .background(
                    if (place.isOpen) ScanPangColors.StatusOpen else ScanPangColors.Error,
                ),
        )
        Text(
            text = if (place.isOpen) "이용 가능" else "이용 제한",
            style = ScanPangType.caption12Medium,
            color = ScanPangColors.OnSurfaceStrong,
        )
    }
}
