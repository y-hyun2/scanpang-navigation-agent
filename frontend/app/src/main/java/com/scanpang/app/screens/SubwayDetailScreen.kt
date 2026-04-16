@file:OptIn(androidx.compose.foundation.layout.ExperimentalLayoutApi::class)

package com.scanpang.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Alignment
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

private fun lineTags(place: Place): List<String> =
    place.tags.filter { it.contains("호선") }

private fun facilityTags(place: Place): List<String> =
    place.tags.filter { !it.contains("호선") }

private fun exitBulletsFromDescription(place: Place): List<String> {
    val parts = place.description.split(".")
        .map { it.trim() }
        .filter { it.isNotBlank() }
    val exits = parts.filter { Regex("""\d+번""").containsMatchIn(it) }
    return if (exits.isNotEmpty()) exits else listOf(place.description)
}

@Composable
fun SubwayDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val viewModel: ScanPangViewModel = viewModel()
    val convenienceResult by viewModel.convenienceResult.collectAsState()
    val isLoading by viewModel.loading.collectAsState()

    LaunchedEffect(Unit) { viewModel.searchConvenience(category = "subway") }

    val place = remember(convenienceResult) {
        if (convenienceResult?.category == "subway") {
            convenienceResult?.facilities?.firstOrNull()?.toPlace("지하철역")
        } else null
    } ?: DummyData.subwayPlaces.first()
    val bookmark = rememberDetailBookmark(
        placeId = place.id,
        placeName = place.name,
        category = place.category,
        distanceLine = "${lineTags(place).joinToString()} · ${place.distance}",
        tags = place.tags,
        target = SavedPlaceNavTarget.Subway,
    )
    val exits = remember(place.id) { exitBulletsFromDescription(place) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
            ) {
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
                    verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
                ) {
                    lineTags(place).forEach { line ->
                        Surface(
                            shape = ScanPangShapes.badge6,
                            color = ScanPangColors.PrimarySoft,
                        ) {
                            Text(
                                text = line,
                                modifier = Modifier.padding(
                                    horizontal = ScanPangSpacing.sm,
                                    vertical = ScanPangDimens.chipPadVertical,
                                ),
                                style = ScanPangType.chip13SemiBold,
                                color = ScanPangColors.Primary,
                            )
                        }
                    }
                }
                Text(
                    text = place.distance,
                    style = ScanPangType.detailMetaSubtitle13,
                    color = ScanPangColors.OnSurfaceMuted,
                )
            }
            DetailNavigateWideButton(
                onClick = {
                    navController.navigate(AppRoutes.ArNavMap) { launchSingleTop = true }
                },
            )
            DetailScreenDivider()
            DetailSectionHeader(title = "출구 정보")
            Column(verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm)) {
                exits.forEachIndexed { index, line ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
                    ) {
                        Text(
                            text = "${index + 1}.",
                            style = ScanPangType.title14,
                            color = ScanPangColors.Primary,
                        )
                        Text(
                            text = line,
                            style = ScanPangType.detailIntro13,
                            color = ScanPangColors.OnSurfaceMuted,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
            DetailSectionHeader(title = "운행 시간")
            DetailIntroBody(text = "첫차·막차: ${place.openHours} (역사 안내 기준)")
            DetailSectionHeader(title = "편의시설")
            DetailFacilityTagRow(tags = facilityTags(place))
            DetailSectionHeader(title = "상세 정보")
            DetailInfoLine(Icons.Rounded.Place, "주소", place.address)
            DetailInfoLine(Icons.Rounded.Phone, "전화번호", place.phone)
            DetailContentBottomSpacer()
        }
    }
}
