package com.scanpang.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.Place
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.scanpang.app.data.DummyData
import com.scanpang.app.data.remote.ScanPangViewModel
import com.scanpang.app.data.toPlace
import com.scanpang.app.data.SavedPlaceNavTarget
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangSpacing

@Composable
fun RestroomDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: ScanPangViewModel = viewModel()
    val convenienceResult by viewModel.convenienceResult.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) { viewModel.searchConvenience(category = "restroom") }

    val place = remember(convenienceResult) {
        if (convenienceResult?.category == "restroom") {
            convenienceResult?.facilities?.firstOrNull()?.toPlace("화장실")
        } else null
    } ?: DummyData.restroomPlaces.first()
    val bookmark = rememberDetailBookmark(
        placeId = place.id,
        placeName = place.name,
        category = place.category,
        distanceLine = "${place.category} · ${place.distance}",
        tags = place.tags,
        target = SavedPlaceNavTarget.Restroom,
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
            DetailSectionHeader(title = "편의시설")
            DetailFacilityTagRow(tags = place.tags)
            DetailScreenDivider()
            DetailSectionHeader(title = "운영 시간")
            DetailIntroBody(text = place.openHours)
            DetailSectionHeader(title = "상세 정보")
            DetailInfoLine(Icons.Rounded.Place, "주소", place.address)
            DetailInfoLine(Icons.Rounded.AccessTime, "운영시간", place.openHours)
            DetailContentBottomSpacer()
        }
    }
}
