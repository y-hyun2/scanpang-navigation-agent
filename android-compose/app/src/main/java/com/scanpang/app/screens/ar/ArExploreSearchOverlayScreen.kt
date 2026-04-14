package com.scanpang.app.screens.ar

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.History
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.scanpang.app.components.ar.ArCameraBackdrop
import com.scanpang.app.components.ar.ArChatBottomSection
import com.scanpang.app.components.ar.ArPoiPinsLayer
import com.scanpang.app.components.ar.ArSideButtonsLayer
import com.scanpang.app.components.ar.ArStatusPillNeutral
import com.scanpang.app.components.ar.ArTopGradientBar
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

@Composable
fun ArExploreSearchOverlayScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val recents = remember { listOf("이태원 할랄 맛집", "명동 쇼핑몰") }
    val suggestions = remember { listOf("할랄", "카페", "기도실", "환전소") }
    var selectedTag by remember { mutableStateOf<String?>("할랄") }

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
                onSearchClick = { },
                centerContent = {
                    ArStatusPillNeutral(text = "탐색 중")
                },
            )

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = ScanPangDimens.arFilterPanelHorizontal)
                    .padding(top = ScanPangSpacing.sm)
                    .border(
                        ScanPangDimens.borderHairline,
                        ScanPangColors.ArSearchPanelStroke,
                        ScanPangShapes.arSearchPanel,
                    ),
                shape = ScanPangShapes.arSearchPanel,
                color = ScanPangColors.Surface,
                shadowElevation = ScanPangDimens.arPoiCardShadowElevation,
            ) {
                Column(
                    modifier = Modifier.padding(ScanPangDimens.arTopBarHorizontal),
                    verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
                            modifier = Modifier
                                .weight(1f)
                                .clickable { navController.navigate(AppRoutes.ArChatKeyboard) },
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Search,
                                contentDescription = null,
                                tint = ScanPangColors.OnSurfaceMuted,
                                modifier = Modifier.size(ScanPangDimens.icon20),
                            )
                            Text(
                                text = "장소·메뉴 검색",
                                style = ScanPangType.searchPlaceholderRegular,
                                color = ScanPangColors.OnSurfacePlaceholder,
                            )
                        }
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = "닫기",
                                tint = ScanPangColors.OnSurfaceStrong,
                            )
                        }
                    }

                    Text(
                        text = "최근 검색",
                        style = ScanPangType.sectionTitle16,
                        color = ScanPangColors.OnSurfaceStrong,
                    )
                    recents.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { navController.navigate(AppRoutes.ArChatKeyboard) }
                                .padding(vertical = ScanPangSpacing.sm),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.History,
                                contentDescription = null,
                                tint = ScanPangColors.OnSurfaceMuted,
                                modifier = Modifier.size(ScanPangDimens.icon18),
                            )
                            Text(
                                text = item,
                                style = ScanPangType.body14Regular,
                                color = ScanPangColors.OnSurfaceStrong,
                            )
                        }
                    }

                    Text(
                        text = "추천 검색어",
                        style = ScanPangType.sectionTitle16,
                        color = ScanPangColors.OnSurfaceStrong,
                    )
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
                    ) {
                        suggestions.forEach { tag ->
                            ArSearchTagChip(
                                label = "# $tag",
                                selected = selectedTag == tag,
                                onClick = {
                                    selectedTag = tag
                                    navController.navigate(AppRoutes.ArChatKeyboard)
                                },
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(ScanPangSpacing.sm))
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
private fun ArSearchTagChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) ScanPangColors.ArRecommendTagHalalBackground else ScanPangColors.Surface
    val fg = if (selected) ScanPangColors.Primary else ScanPangColors.OnSurfaceStrong
    val borderColor = if (selected) ScanPangColors.Primary else ScanPangColors.OutlineSubtle
    Surface(
        modifier = Modifier
            .clickable(onClick = onClick)
            .then(
                if (!selected) {
                    Modifier.border(
                        ScanPangDimens.borderHairline,
                        borderColor,
                        ScanPangShapes.profileTag,
                    )
                } else {
                    Modifier
                },
            ),
        shape = ScanPangShapes.profileTag,
        color = bg,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(
                horizontal = ScanPangDimens.arSearchTagHorizontalPad,
                vertical = ScanPangDimens.arSearchTagVerticalPad,
            ),
            style = ScanPangType.tag11Medium,
            color = fg,
        )
    }
}
