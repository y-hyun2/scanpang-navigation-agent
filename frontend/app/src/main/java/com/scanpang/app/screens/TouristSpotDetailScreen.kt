@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.scanpang.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.scanpang.app.data.DummyData
import com.scanpang.app.data.remote.ScanPangViewModel
import com.scanpang.app.data.toPlace
import com.scanpang.app.data.SavedPlaceNavTarget
import com.scanpang.app.data.galleryModels
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangSpacing

@Composable
fun TouristSpotDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: ScanPangViewModel = viewModel()
    val convenienceResult by viewModel.convenienceResult.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) { viewModel.searchConvenience(category = "tourist_info") }

    val place = remember(convenienceResult) {
        if (convenienceResult?.category == "tourist_info") {
            convenienceResult?.facilities?.firstOrNull()?.toPlace("관광지")
        } else null
    } ?: DummyData.touristPlaces.first()
    val imageModels = remember(place.id) { place.galleryModels(defaultPlaceDetailGallery()) }
    val pagerState = rememberPagerState(pageCount = { imageModels.size })
    var fullscreenOpen by remember { mutableStateOf(false) }
    val visitCards = remember(place.id) {
        listOf(
            DetailVisitCardUi(
                if (place.isOpen) "지금 방문 가능" else "운영 종료",
                place.openHours,
                if (place.isOpen) DetailVisitCardTone.Open else DetailVisitCardTone.Closed,
            ),
            DetailVisitCardUi("야간 전망", "일몰 이후 조명·야경 코스", DetailVisitCardTone.Neutral),
            DetailVisitCardUi("우천 시", "실내 전망 위주 동선 추천", DetailVisitCardTone.Neutral),
        )
    }
    val bookmark = rememberDetailBookmark(
        placeId = place.id,
        placeName = place.name,
        category = place.category,
        distanceLine = "${place.category} · ${place.distance}",
        tags = place.tags,
        target = SavedPlaceNavTarget.TouristSpot,
    )

    if (fullscreenOpen) {
        DetailImageFullscreenDialog(
            gallery = imageModels,
            pagerState = pagerState,
            onDismiss = { fullscreenOpen = false },
        )
    }

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
            onFullscreenClick = { fullscreenOpen = true },
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
            )
            DetailNavigateWideButton(
                onClick = {
                    navController.navigate(AppRoutes.ArNavMap) { launchSingleTop = true }
                },
            )
            DetailVisitCardsHorizontalPager(cards = visitCards)
            DetailScreenDivider()
            DetailSectionHeader(title = "소개")
            DetailIntroBody(text = place.description)
            DetailSectionHeader(title = "상세 정보")
            DetailInfoLine(Icons.Rounded.Place, "주소", place.address)
            DetailInfoLine(Icons.Rounded.AccessTime, "운영시간", place.openHours)
            DetailInfoLine(Icons.Rounded.AccessTime, "입장료", place.subCategory.ifBlank { "현장 요금제" })
            DetailInfoLine(Icons.Rounded.Phone, "전화번호", place.phone)
            DetailContentBottomSpacer()
        }
    }
}
