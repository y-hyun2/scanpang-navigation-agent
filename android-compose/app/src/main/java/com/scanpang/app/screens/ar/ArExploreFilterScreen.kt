package com.scanpang.app.screens.ar

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.scanpang.app.components.ar.ArCameraBackdrop
import com.scanpang.app.components.ar.ArChatBottomSection
import com.scanpang.app.components.ar.ArFilterChipRow
import com.scanpang.app.components.ar.ArPoiPinsLayer
import com.scanpang.app.components.ar.ArSideButtonsLayer
import com.scanpang.app.components.ar.ArStatusPillPrimary
import com.scanpang.app.components.ar.ArTopGradientBar
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType
import androidx.compose.material3.Surface

@Composable
fun ArExploreFilterScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    var category by remember { mutableStateOf<String?>("카페") }
    var sort by remember { mutableStateOf<String?>("거리순") }
    val categories = remember {
        listOf("카페", "음식점", "쇼핑", "관광", "기도실", "환전")
    }
    val sorts = remember { listOf("거리순", "인기순", "할랄 인증") }

    Box(modifier = modifier.fillMaxSize()) {
        ArCameraBackdrop(showFreezeTint = false, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(),
        ) {
            ArTopGradientBar(
                modifier = Modifier.fillMaxWidth(),
                onHomeClick = { navController.popBackStack() },
                onSearchClick = { navController.navigate(AppRoutes.ArSearch) },
                centerContent = {
                    ArStatusPillPrimary(
                        text = "카페 탐색 중",
                        icon = Icons.Rounded.FilterList,
                    )
                },
            )

            Spacer(modifier = Modifier.height(ScanPangSpacing.sm))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScanPangDimens.arFilterPanelHorizontal),
                shape = ScanPangShapes.arFilterPanelTop,
                color = ScanPangColors.Surface,
                shadowElevation = ScanPangDimens.arPoiCardShadowElevation,
            ) {
                Column(
                    modifier = Modifier.padding(ScanPangDimens.arTopBarHorizontal),
                    verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
                ) {
                    RowFilterHeader(
                        label = "카테고리",
                        value = category ?: "",
                    )
                    ArFilterChipRow(
                        labels = categories,
                        selected = category,
                        onSelect = { category = it },
                    )
                    Text(
                        text = "정렬",
                        style = ScanPangType.arFilterTitle16,
                        color = ScanPangColors.OnSurfaceStrong,
                    )
                    ArFilterChipRow(
                        labels = sorts,
                        selected = sort,
                        onSelect = { sort = it },
                    )
                    Spacer(modifier = Modifier.height(ScanPangDimens.arFilterSectionTitleTop))
                    Button(
                        onClick = { navController.popBackStack() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ScanPangDimens.searchBarHeightDefault),
                        shape = ScanPangShapes.radius12,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = ScanPangColors.Primary,
                            contentColor = Color.White,
                        ),
                    ) {
                        Text("필터 적용", style = ScanPangType.body15Medium)
                    }
                    Spacer(modifier = Modifier.height(ScanPangDimens.arFilterApplyBottom))
                }
            }
        }

        Box(Modifier.fillMaxSize()) {
            ArPoiPinsLayer()
            ArSideButtonsLayer(
                onVolumeClick = { },
                onCameraClick = { },
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            ArChatBottomSection(
                userMessage = "손님, 오늘은 어떤 장소를 찾고 계신가요?",
                agentMessage = "안녕하세요! 스캔팡 AI입니다. 주변 장소를 탐색해 드릴게요.",
                inputPlaceholder = "무엇이든 물어보삼",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}

@Composable
private fun RowFilterHeader(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            style = ScanPangType.arFilterTitle16,
            color = ScanPangColors.OnSurfaceStrong,
        )
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = value,
                style = ScanPangType.body15Medium,
                color = ScanPangColors.OnSurfaceMuted,
            )
            Icon(
                imageVector = Icons.Rounded.ArrowDropDown,
                contentDescription = null,
                tint = ScanPangColors.OnSurfaceMuted,
            )
        }
    }
}
