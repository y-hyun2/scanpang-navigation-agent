package com.scanpang.app.components.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Apartment
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.CalendarMonth
import androidx.compose.material.icons.rounded.Elevator
import androidx.compose.material.icons.rounded.ExpandMore
import androidx.compose.material.icons.rounded.Lightbulb
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.material.icons.rounded.SmartToy
import androidx.compose.material.icons.rounded.Stairs
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import com.scanpang.app.components.ar.ArCameraBackdrop
import com.scanpang.app.components.ar.ArNavSideVolumeCamera
import com.scanpang.app.components.ar.ArNavStandaloneChatBlock
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

enum class PlaceDetailArTab {
    Building,
    Floors,
    AiGuide,
}

@Composable
fun DetailArPlaceOverlayScaffold(
    selectedTab: PlaceDetailArTab,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onTabBuilding: () -> Unit,
    onTabFloors: () -> Unit,
    onTabAi: () -> Unit,
    onVolumeClick: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        ArCameraBackdrop(showFreezeTint = false, modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            ArNavStandaloneChatBlock(
                userMessage = "여기 역사가 궁금해",
                agentMessage = "이 건물은 1920년대 초기 바우하우스 양식을 반영한 서울의 대표적인 근대 건축물입니다.",
                inputPlaceholder = "무엇이든 물어보세요",
                modifier = Modifier.fillMaxWidth(),
            )
        }
        Box(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth()
                .padding(horizontal = ScanPangDimens.arTopBarHorizontal)
                .padding(top = ScanPangDimens.detailArPanelTop),
        ) {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(ScanPangDimens.detailArPanelHeight),
                shape = ScanPangShapes.radius16,
                color = ScanPangColors.DetailArPanelSurface,
                shadowElevation = ScanPangDimens.arPoiCardShadowElevation,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = ScanPangSpacing.lg)
                        .padding(top = ScanPangSpacing.md, bottom = ScanPangSpacing.md),
                ) {
                    PlaceDetailPanelHeader()
                    Spacer(modifier = Modifier.height(ScanPangSpacing.md))
                    DetailPlaceThreeTabRow(
                        selected = selectedTab,
                        onBuilding = onTabBuilding,
                        onFloors = onTabFloors,
                        onAi = onTabAi,
                    )
                    Spacer(modifier = Modifier.height(ScanPangSpacing.md))
                    HorizontalDivider(color = ScanPangColors.OutlineSubtle)
                    Spacer(modifier = Modifier.height(ScanPangSpacing.md))
                    when (selectedTab) {
                        PlaceDetailArTab.Building -> BuildingTabBody()
                        PlaceDetailArTab.Floors -> FloorsTabBody()
                        PlaceDetailArTab.AiGuide -> AiGuideTabBody()
                    }
                }
            }
        }
        DetailArExploringTopHud(
            modifier = Modifier.align(Alignment.TopStart),
            onHomeClick = onHomeClick,
            onSearchClick = onSearchClick,
        )
        ArNavSideVolumeCamera(
            onVolumeClick = onVolumeClick,
            onCameraClick = onCameraClick,
        )
    }
}

@Composable
private fun PlaceDetailPanelHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Top,
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
        ) {
            Text(
                text = "서울시립미술관",
                style = ScanPangType.detailPlaceTitle18,
                color = ScanPangColors.OnSurfaceStrong,
            )
            Text(
                text = "Seoul Museum of Art",
                style = ScanPangType.detailSubtitleEn9,
                color = ScanPangColors.OnSurfacePlaceholder,
            )
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Rounded.LocationOn,
                    contentDescription = null,
                    modifier = Modifier.size(ScanPangDimens.detailInfoIcon15),
                    tint = ScanPangColors.OnSurfaceMuted,
                )
                Text(
                    text = "중구 덕수궁길 61",
                    style = ScanPangType.caption12Medium,
                    color = ScanPangColors.OnSurfaceMuted,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "·",
                    style = ScanPangType.caption12Medium,
                    color = ScanPangColors.OnSurfacePlaceholder,
                )
                Text(
                    text = "120m",
                    style = ScanPangType.meta11Medium,
                    color = ScanPangColors.OnSurfaceMuted,
                )
            }
        }
        Surface(
            modifier = Modifier
                .size(ScanPangDimens.detailBookmarkBtn)
                .clip(CircleShape)
                .clickable(onClick = { }),
            shape = CircleShape,
            color = ScanPangColors.Surface,
            shadowElevation = ScanPangDimens.arPoiCardShadowElevation,
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Rounded.BookmarkBorder,
                    contentDescription = "저장",
                    modifier = Modifier.size(ScanPangDimens.detailBookmarkIcon),
                    tint = ScanPangColors.OnSurfaceStrong,
                )
            }
        }
    }
}

@Composable
private fun DetailPlaceThreeTabRow(
    selected: PlaceDetailArTab,
    onBuilding: () -> Unit,
    onFloors: () -> Unit,
    onAi: () -> Unit,
) {
    Surface(
        shape = ScanPangShapes.sortButton,
        color = ScanPangColors.Background,
        border = BorderStroke(ScanPangDimens.borderHairline, ScanPangColors.OutlineSubtle),
    ) {
        Row(
            modifier = Modifier
                .padding(ScanPangDimens.detailThreeTabTrackPad)
                .height(ScanPangDimens.detailThreeTabHeight)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            PlaceDetailTabSegment(
                label = "건물 정보",
                selected = selected == PlaceDetailArTab.Building,
                onClick = onBuilding,
                modifier = Modifier.weight(1f),
            )
            PlaceDetailTabSegment(
                label = "층별 정보",
                selected = selected == PlaceDetailArTab.Floors,
                onClick = onFloors,
                modifier = Modifier.weight(1f),
            )
            PlaceDetailTabSegment(
                label = "AI 가이드",
                selected = selected == PlaceDetailArTab.AiGuide,
                onClick = onAi,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun PlaceDetailTabSegment(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = RoundedCornerShape(ScanPangDimens.detailThreeTabInnerRadius)
    if (selected) {
        Surface(
            modifier = modifier
                .fillMaxHeight()
                .clip(shape)
                .clickable(onClick = onClick),
            shape = shape,
            color = ScanPangColors.Surface,
            shadowElevation = ScanPangDimens.arPoiCardShadowElevation,
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text(
                    text = label,
                    style = ScanPangType.detailThreeTab11,
                    color = ScanPangColors.Primary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    } else {
        Box(
            modifier = modifier
                .fillMaxHeight()
                .clip(shape)
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = label,
                style = ScanPangType.detailThreeTab11Inactive,
                color = ScanPangColors.OnSurfacePlaceholder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun BuildingTabBody() {
    val carousel = rememberScrollState()
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(carousel),
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
    ) {
        repeat(3) {
            Box(
                modifier = Modifier
                    .width(ScanPangDimens.detailArCarouselItemWidth)
                    .height(ScanPangDimens.detailArCarouselSmallHeight)
                    .clip(ScanPangShapes.radius12)
                    .background(ScanPangColors.DetailCarouselPlaceholder),
            )
        }
    }
    Spacer(modifier = Modifier.height(ScanPangSpacing.md))
    Text(
        text = "덕수궁 내에 위치한 근대 미술관으로, 한국 근현대 미술 작품을 중심으로 기획 전시를 선보입니다. 역사적 건축물과 조화를 이루는 전시 공간이 특징입니다.",
        style = ScanPangType.detailBody12Loose,
        color = ScanPangColors.OnSurfaceMuted,
    )
    Spacer(modifier = Modifier.height(ScanPangSpacing.lg))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
    ) {
        InfoGridCell(
            icon = Icons.Rounded.Apartment,
            label = "층수",
            value = "지상 3층",
            modifier = Modifier.weight(1f),
        )
        InfoGridCell(
            icon = Icons.Rounded.CalendarMonth,
            label = "건축년도",
            value = "1933년",
            modifier = Modifier.weight(1f),
        )
    }
    Spacer(modifier = Modifier.height(ScanPangSpacing.md))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
    ) {
        InfoGridCell(
            icon = Icons.Rounded.Elevator,
            label = "편의시설",
            value = "엘리베이터",
            modifier = Modifier.weight(1f),
        )
        InfoGridCell(
            icon = Icons.Rounded.Stairs,
            label = "계단",
            value = "비상구",
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun InfoGridCell(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(ScanPangDimens.arNavActionIconInner)
                .clip(CircleShape)
                .background(ScanPangColors.PrimarySoft),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ScanPangDimens.detailGridIcon12),
                tint = ScanPangColors.Primary,
            )
        }
        Column(verticalArrangement = Arrangement.spacedBy(ScanPangDimens.icon5)) {
            Text(
                text = label,
                style = ScanPangType.detailGrid10,
                color = ScanPangColors.OnSurfacePlaceholder,
            )
            Text(
                text = value,
                style = ScanPangType.meta13,
                color = ScanPangColors.OnSurfaceStrong,
            )
        }
    }
}

@Composable
private fun FloorsTabBody() {
    var openB1 by remember { mutableStateOf(false) }
    var open1F by remember { mutableStateOf(true) }
    var open2F by remember { mutableStateOf(false) }
    FloorBlock(
        title = "지하 1층 (B1)",
        subtitle = "주차장",
        expanded = openB1,
        onToggle = { openB1 = !openB1 },
    )
    Spacer(modifier = Modifier.height(ScanPangSpacing.sm))
    FloorBlock(
        title = "1층 (1F)",
        subtitle = "로비 · 매표소",
        expanded = open1F,
        onToggle = { open1F = !open1F },
        facilities = listOf("안내데스크", "카페테리아", "기념품샵"),
    )
    Spacer(modifier = Modifier.height(ScanPangSpacing.sm))
    FloorBlock(
        title = "2층 (2F)",
        subtitle = "전시실 A · B",
        expanded = open2F,
        onToggle = { open2F = !open2F },
    )
}

@Composable
private fun FloorBlock(
    title: String,
    subtitle: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    facilities: List<String> = emptyList(),
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ScanPangShapes.radius12)
            .border(ScanPangDimens.borderHairline, ScanPangColors.OutlineSubtle, ScanPangShapes.radius12)
            .clickable(onClick = onToggle)
            .padding(ScanPangSpacing.md),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(ScanPangDimens.icon5)) {
                Text(
                    text = title,
                    style = ScanPangType.detailFloorTitle14,
                    color = ScanPangColors.OnSurfaceStrong,
                )
                Text(
                    text = subtitle,
                    style = ScanPangType.caption12Medium,
                    color = ScanPangColors.OnSurfaceMuted,
                )
            }
            Icon(
                imageVector = Icons.Rounded.ExpandMore,
                contentDescription = null,
                modifier = Modifier
                    .size(ScanPangDimens.detailFloorChevron)
                    .rotate(if (expanded) 180f else 0f),
                tint = ScanPangColors.OnSurfacePlaceholder,
            )
        }
        if (expanded && facilities.isNotEmpty()) {
            Spacer(modifier = Modifier.height(ScanPangSpacing.sm))
            facilities.forEach { line ->
                Text(
                    text = "· $line",
                    style = ScanPangType.caption12Medium,
                    color = ScanPangColors.OnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
private fun AiGuideTabBody() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
    ) {
        Icon(
            imageVector = Icons.Rounded.SmartToy,
            contentDescription = null,
            modifier = Modifier.size(ScanPangDimens.detailAiRobotIcon),
            tint = ScanPangColors.Primary,
        )
        Text(
            text = "AI 가이드 요약",
            style = ScanPangType.title14,
            color = ScanPangColors.OnSurfaceStrong,
        )
    }
    Spacer(modifier = Modifier.height(ScanPangSpacing.sm))
    Text(
        text = "이곳은 한국 근현대 미술의 흐름을 한눈에 볼 수 있는 공간입니다. 조용한 동선으로 둘러보기 좋습니다.",
        style = ScanPangType.detailBody12Loose,
        color = ScanPangColors.OnSurfaceMuted,
    )
    Spacer(modifier = Modifier.height(ScanPangSpacing.md))
    Text(
        text = "추천 포인트",
        style = ScanPangType.title14,
        color = ScanPangColors.OnSurfaceStrong,
    )
    Spacer(modifier = Modifier.height(ScanPangSpacing.sm))
    listOf(
        "1층 로비의 조형물",
        "2층 통유리 전시실",
        "덕수궁 돌담길과 이어지는 산책로",
    ).forEach { line ->
        Row(
            modifier = Modifier.padding(vertical = ScanPangDimens.icon5),
            horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
            verticalAlignment = Alignment.Top,
        ) {
            Box(
                modifier = Modifier
                    .padding(top = ScanPangDimens.icon5)
                    .size(ScanPangDimens.detailAiPointIcon)
                    .clip(CircleShape)
                    .background(ScanPangColors.PrimarySoft),
            )
            Text(
                text = line,
                style = ScanPangType.detailBody12Loose,
                color = ScanPangColors.OnSurfaceMuted,
            )
        }
    }
    Spacer(modifier = Modifier.height(ScanPangSpacing.md))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(ScanPangShapes.radius12)
            .background(ScanPangColors.DetailTipBackground)
            .padding(ScanPangSpacing.md),
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = Icons.Rounded.Lightbulb,
            contentDescription = null,
            modifier = Modifier.size(ScanPangDimens.detailAiPointIcon),
            tint = ScanPangColors.AccentAmber,
        )
        Column(verticalArrangement = Arrangement.spacedBy(ScanPangDimens.icon5)) {
            Text(
                text = "Tip",
                style = ScanPangType.detailBadge9,
                color = ScanPangColors.DetailTipText,
            )
            Text(
                text = "오후 늦은 시간대에 방문하면 전시실이 한산해 사진 촬영에 유리합니다.",
                style = ScanPangType.detailBody12Loose,
                color = ScanPangColors.DetailTipText,
            )
        }
    }
}
