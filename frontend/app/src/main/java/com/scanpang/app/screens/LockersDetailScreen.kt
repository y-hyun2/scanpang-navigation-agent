package com.scanpang.app.screens

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.scanpang.app.data.DummyData
import com.scanpang.app.data.LockerTier
import com.scanpang.app.data.Place
import com.scanpang.app.data.SavedPlaceNavTarget
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

private fun paymentTagsFromPlace(place: Place): List<String> {
    val keys = listOf("현금", "카드", "QR")
    val fromTags = place.tags.filter { tag -> keys.any { tag.contains(it, ignoreCase = true) } }
    return fromTags.ifEmpty { listOf("카드결제") }
}

@Composable
fun LockersDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val place = remember { DummyData.lockerPlaces.first() }
    val tiers = remember(place.id) { DummyData.lockerTiers[place.id].orEmpty() }
    val bookmark = rememberDetailBookmark(
        placeId = place.id,
        placeName = place.name,
        category = place.category,
        distanceLine = "${place.category} · ${place.distance}",
        tags = place.tags,
        target = SavedPlaceNavTarget.Lockers,
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScanPangColors.Surface)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        DetailScrollTopBackRow(onBack = { navController.popBackStack() })
        Column(
            modifier = Modifier.padding(horizontal = ScanPangDimens.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(ScanPangDimens.detailSectionSpacing),
        ) {
            Spacer(modifier = Modifier.height(ScanPangSpacing.sm))
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
            DetailScreenDivider()
            DetailSectionHeader(title = "보관함 정보")
            if (tiers.isEmpty()) {
                DetailFacilityTagRow(tags = place.tags)
            } else {
                Column(verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm)) {
                    tiers.forEach { row ->
                        LockerTierCard(row = row)
                    }
                }
            }
            DetailScreenDivider()
            DetailSectionHeader(title = "운영 시간")
            DetailIntroBody(text = place.openHours)
            DetailSectionHeader(title = "결제 방법")
            DetailFacilityTagRow(tags = paymentTagsFromPlace(place))
            DetailSectionHeader(title = "상세 정보")
            DetailInfoLine(Icons.Rounded.Place, "주소", place.address)
            DetailInfoLine(Icons.Rounded.AccessTime, "운영시간", place.openHours)
            DetailContentBottomSpacer()
        }
    }
}

@Composable
private fun LockerTierCard(row: LockerTier) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = ScanPangShapes.detailVisitCard,
        color = ScanPangColors.DetailMenuRowBackground,
        border = BorderStroke(
            ScanPangDimens.borderHairline,
            ScanPangColors.OutlineSubtle,
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(ScanPangSpacing.md),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ScanPangDimens.icon5)) {
                Text(
                    text = row.label,
                    style = ScanPangType.title14,
                    color = ScanPangColors.OnSurfaceStrong,
                )
                Text(
                    text = row.price,
                    style = ScanPangType.detailIntro13,
                    color = ScanPangColors.OnSurfaceMuted,
                )
            }
            Text(
                text = if (row.available) "가용" else "만석",
                style = ScanPangType.chip13SemiBold,
                color = if (row.available) ScanPangColors.StatusOpen else ScanPangColors.Error,
            )
        }
    }
}
