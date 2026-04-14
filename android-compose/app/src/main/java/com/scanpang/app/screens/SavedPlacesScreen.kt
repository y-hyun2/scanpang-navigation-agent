package com.scanpang.app.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowDown
import androidx.compose.material.icons.rounded.SwapVert
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.navigation.NavController
import com.scanpang.app.components.SavedPlaceCard
import com.scanpang.app.components.SavedPlaceTag
import com.scanpang.app.components.SavedPlaceTagStyle
import com.scanpang.app.components.ScanPangBottomBar
import com.scanpang.app.components.ScanPangFilterChip
import com.scanpang.app.components.ScanPangMainTab
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

private val filterLabels = listOf(
    "전체", "식당", "카페", "편의점", "쇼핑", "환전소", "은행", "ATM",
    "병원", "약국", "지하철역", "화장실", "물품보관함",
)

@Composable
fun SavedPlacesScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val selected = remember { mutableIntStateOf(0) }
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = ScanPangColors.Background,
        bottomBar = {
            ScanPangBottomBar(
                selectedTab = ScanPangMainTab.Saved,
                onHomeClick = { navController.navigate(AppRoutes.Home) { launchSingleTop = true } },
                onSearchClick = { navController.navigate(AppRoutes.Search) { launchSingleTop = true } },
                onSavedClick = { },
                onProfileClick = { navController.navigate(AppRoutes.Profile) { launchSingleTop = true } },
                onExploreClick = {
                    navController.navigate(AppRoutes.ArDefault) { launchSingleTop = true }
                },
            )
        },
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(ScanPangColors.Background)
                .statusBarsPadding()
                .padding(horizontal = ScanPangDimens.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.lg),
        ) {
            item {
                Text(
                    text = "저장한 장소",
                    style = ScanPangType.homeGreeting,
                    color = ScanPangColors.OnSurfaceStrong,
                    modifier = Modifier.padding(top = ScanPangSpacing.sm),
                )
            }
            item {
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
                ) {
                    itemsIndexed(filterLabels) { index, label ->
                        ScanPangFilterChip(
                            label = label,
                            selected = selected.intValue == index,
                            onClick = { selected.intValue = index },
                        )
                    }
                }
            }
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = "4개의 장소",
                        style = ScanPangType.link13,
                        color = ScanPangColors.OnSurfaceMuted,
                    )
                    Row(
                        modifier = Modifier
                            .clip(ScanPangShapes.sortButton)
                            .background(ScanPangColors.Background)
                            .clickable(onClick = { })
                            .padding(
                                start = ScanPangDimens.sortButtonPaddingStart,
                                end = ScanPangDimens.sortButtonPaddingEnd,
                                top = ScanPangDimens.sortButtonPaddingVertical,
                                bottom = ScanPangDimens.sortButtonPaddingVertical,
                            ),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.xs),
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.SwapVert,
                            contentDescription = null,
                            modifier = Modifier
                                .padding(start = ScanPangSpacing.xs)
                                .size(ScanPangDimens.icon14),
                            tint = ScanPangColors.OnSurfaceStrong,
                        )
                        Text(
                            text = "가까운 순",
                            style = ScanPangType.sort12SemiBold,
                            color = ScanPangColors.OnSurfaceStrong,
                        )
                        Icon(
                            imageVector = Icons.Rounded.KeyboardArrowDown,
                            contentDescription = null,
                            modifier = Modifier.size(ScanPangDimens.icon14),
                            tint = ScanPangColors.OnSurfaceStrong,
                        )
                    }
                }
            }
            item {
                SavedPlaceCard(
                    title = "모로코 할랄 키친",
                    categoryLabel = "할랄 식당",
                    distanceLine = "명동 · 350m",
                    tags = listOf(
                        SavedPlaceTag("인증완료", SavedPlaceTagStyle.Success),
                        SavedPlaceTag("인기", SavedPlaceTagStyle.Warning),
                    ),
                    onClick = { },
                )
            }
            item {
                SavedPlaceCard(
                    title = "이태원 중앙성원",
                    categoryLabel = "기도실",
                    distanceLine = "이태원 · 1.2km",
                    tags = listOf(SavedPlaceTag("대성원", SavedPlaceTagStyle.Neutral)),
                    onClick = { },
                )
            }
            item {
                SavedPlaceCard(
                    title = "경복궁",
                    categoryLabel = "관광지",
                    distanceLine = "종로구 · 4.5km",
                    tags = listOf(
                        SavedPlaceTag("유네스코", SavedPlaceTagStyle.Neutral),
                        SavedPlaceTag("필수방문", SavedPlaceTagStyle.Warning),
                    ),
                    onClick = { },
                )
            }
            item {
                SavedPlaceCard(
                    title = "올리브영 명동 플래그십",
                    categoryLabel = "쇼핑",
                    distanceLine = "명동 · 200m",
                    tags = listOf(
                        SavedPlaceTag("뷰티", SavedPlaceTagStyle.Neutral),
                        SavedPlaceTag("무슬림 친화", SavedPlaceTagStyle.Success),
                    ),
                    onClick = { },
                )
            }
        }
    }
}
