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
import androidx.compose.material.icons.rounded.AccountBalance
import androidx.compose.material.icons.rounded.AttachMoney
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import com.scanpang.app.data.Place
import com.scanpang.app.data.SavedPlaceNavTarget
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

private fun isAtm24h(place: Place): Boolean =
    place.openHours.contains("24") || place.tags.any { it.contains("24") }

private fun cardBrandTags(place: Place): List<String> =
    place.tags.filter { !it.contains("24") }

@Composable
fun AtmDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: ScanPangViewModel = viewModel()
    val convenienceResult by viewModel.convenienceResult.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) { viewModel.searchConvenience(category = "atm") }

    val place = remember(convenienceResult) {
        if (convenienceResult?.category == "atm") {
            convenienceResult?.facilities?.firstOrNull()?.toPlace("ATM")
        } else null
    } ?: DummyData.atmPlaces.first()
    val bookmark = rememberDetailBookmark(
        placeId = place.id,
        placeName = place.name,
        category = place.category,
        distanceLine = "${place.category} · ${place.distance}",
        tags = place.tags,
        target = SavedPlaceNavTarget.Atm,
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
                trailing = {
                    Surface(
                        shape = ScanPangShapes.badge6,
                        color = if (isAtm24h(place)) {
                            ScanPangColors.DetailVisitOpenSurface
                        } else {
                            ScanPangColors.DetailFacilityTagBackground
                        },
                    ) {
                        Text(
                            text = if (isAtm24h(place)) "24시간" else "시간제",
                            modifier = Modifier.padding(
                                horizontal = ScanPangSpacing.sm,
                                vertical = ScanPangDimens.chipPadVertical,
                            ),
                            style = ScanPangType.category11SemiBold,
                            color = if (isAtm24h(place)) {
                                ScanPangColors.TrustPillText
                            } else {
                                ScanPangColors.OnSurfaceMuted
                            },
                        )
                    }
                },
            )
            DetailNavigateWideButton(
                onClick = {
                    navController.navigate(AppRoutes.ArNavMap) { launchSingleTop = true }
                },
            )
            DetailScreenDivider()
            DetailSectionHeader(title = "지원 카드사")
            DetailFacilityTagRow(tags = cardBrandTags(place))
            DetailScreenDivider()
            DetailSectionHeader(title = "운영 시간")
            DetailIntroBody(text = place.openHours)
            DetailSectionHeader(title = "상세 정보")
            DetailInfoLine(Icons.Rounded.Place, "주소", place.address)
            DetailInfoLine(Icons.Rounded.AccountBalance, "은행명", place.name.substringBefore(" ATM"))
            DetailInfoLine(Icons.Rounded.AttachMoney, "수수료 안내", place.description)
            DetailContentBottomSpacer()
        }
    }
}
