@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.scanpang.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.scanpang.app.data.DummyData
import com.scanpang.app.data.ExchangeRate
import com.scanpang.app.data.SavedPlaceNavTarget
import com.scanpang.app.data.galleryModels
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

@Composable
fun ExchangeDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val place = remember { DummyData.exchangePlaces.first() }
    val imageModels = remember(place.id) { place.galleryModels(defaultPlaceDetailGallery()) }
    val pagerState = rememberPagerState(pageCount = { imageModels.size })
    val rates = remember { DummyData.exchangeRates }
    val bookmark = rememberDetailBookmark(
        placeId = place.id,
        placeName = place.name,
        category = place.category,
        distanceLine = "${place.category} · ${place.distance}",
        tags = place.tags,
        target = SavedPlaceNavTarget.Exchange,
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
            DetailSectionHeader(title = "환율 정보")
            Text(
                text = "추후 API 연동 예정 · 아래 환율·이모지 표시는 더미입니다.",
                style = ScanPangType.caption12Medium,
                color = ScanPangColors.OnSurfaceMuted,
            )
            Column(verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm)) {
                rates.forEach { row ->
                    ExchangeRateRowUi(row = row)
                }
            }
            DetailScreenDivider()
            DetailSectionHeader(title = "소개")
            DetailIntroBody(text = place.description)
            DetailSectionHeader(title = "상세 정보")
            DetailInfoLine(Icons.Rounded.Place, "주소", place.address)
            DetailInfoLine(Icons.Rounded.AccessTime, "운영시간", place.openHours)
            DetailInfoLine(Icons.Rounded.AttachMoney, "수수료 안내", place.tags.joinToString(" · "))
            DetailInfoLine(Icons.Rounded.Phone, "전화번호", place.phone)
            DetailContentBottomSpacer()
        }
    }
}

@Composable
private fun ExchangeRateRowUi(row: ExchangeRate) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ScanPangShapes.detailMenuRow)
            .background(ScanPangColors.DetailMenuRowBackground)
            .padding(
                horizontal = ScanPangSpacing.md,
                vertical = ScanPangSpacing.sm,
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "${row.flag} ${row.currency} → KRW",
            style = ScanPangType.caption12Medium,
            color = ScanPangColors.OnSurfaceStrong,
        )
        Text(
            text = row.rate,
            style = ScanPangType.detailMenuPrice14,
            color = ScanPangColors.OnSurfaceStrong,
        )
    }
}
