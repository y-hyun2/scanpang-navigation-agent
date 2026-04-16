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
import androidx.compose.material.icons.rounded.LocalPharmacy
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.scanpang.app.data.DummyData
import com.scanpang.app.data.Place
import com.scanpang.app.data.SavedPlaceNavTarget
import com.scanpang.app.data.galleryModels
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangSpacing

private fun pharmacy24hLine(place: Place): String =
    when {
        place.openHours.contains("24") -> "24시간 또는 연장 운영"
        else -> "일반 영업시간 (자정 전 종료)"
    }

@Composable
fun PharmacyDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val place = remember { DummyData.pharmacyPlaces.first() }
    val imageModels = remember(place.id) { place.galleryModels(defaultPlaceDetailGallery()) }
    val pagerState = rememberPagerState(pageCount = { imageModels.size })
    val bookmark = rememberDetailBookmark(
        placeId = place.id,
        placeName = place.name,
        category = place.category,
        distanceLine = "${place.category} · ${place.distance}",
        tags = place.tags,
        target = SavedPlaceNavTarget.Pharmacy,
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
            )
            DetailNavigateAndSideIconRow(
                onNavigate = {
                    navController.navigate(AppRoutes.ArNavMap) { launchSingleTop = true }
                },
                sideIcon = Icons.Rounded.Phone,
                sideContentDescription = "전화",
                onSideClick = { context.openPhoneDialer(place.phone) },
            )
            DetailVisitCardsHorizontalPager(cards = place.detailVisitCardsFromPlace())
            DetailScreenDivider()
            DetailSectionHeader(title = "소개")
            DetailIntroBody(text = place.description)
            DetailSectionHeader(title = "상세 정보")
            DetailInfoLine(Icons.Rounded.Place, "주소", place.address)
            DetailInfoLine(Icons.Rounded.AccessTime, "운영시간", place.openHours)
            DetailInfoLine(Icons.Rounded.LocalPharmacy, "24시간 여부", pharmacy24hLine(place))
            DetailInfoLine(Icons.Rounded.Phone, "전화번호", place.phone)
            DetailContentBottomSpacer()
        }
    }
}
