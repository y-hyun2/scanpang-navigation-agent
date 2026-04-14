package com.scanpang.app.screens.detail

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Phone
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.navigation.NavController
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.ScanPangFigmaAssets
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

/**
 * Figma: 식당 상세 (`290:1325`)
 */
@Composable
fun RestaurantDetailScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(ScanPangColors.Surface)
            .verticalScroll(rememberScrollState())
            .navigationBarsPadding(),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(ScanPangDimens.detailPhotoHeroHeight),
        ) {
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(ScanPangFigmaAssets.RestaurantDetailHero)
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .statusBarsPadding()
                    .padding(ScanPangSpacing.sm),
            ) {
                Surface(
                    shape = CircleShape,
                    color = ScanPangColors.ArOverlayWhite93,
                    shadowElevation = ScanPangDimens.arPoiCardShadowElevation,
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                        contentDescription = "뒤로",
                        modifier = Modifier.padding(ScanPangSpacing.sm),
                        tint = ScanPangColors.OnSurfaceStrong,
                    )
                }
            }
            Row(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = ScanPangSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.xs),
            ) {
                repeat(3) { index ->
                    Box(
                        modifier = Modifier
                            .size(ScanPangDimens.stackGap6)
                            .clip(CircleShape)
                            .background(
                                if (index == 0) ScanPangColors.Primary
                                else ScanPangColors.OutlineSubtle,
                            ),
                    )
                }
            }
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(ScanPangSpacing.lg),
                shape = ScanPangShapes.badge6,
                color = ScanPangColors.DetailImageCountScrim,
            ) {
                Text(
                    text = "1/4",
                    modifier = Modifier.padding(
                        horizontal = ScanPangSpacing.sm,
                        vertical = ScanPangDimens.badgePadVertical,
                    ),
                    style = ScanPangType.detailImageCount9,
                    color = Color.White,
                )
            }
        }
        Column(
            modifier = Modifier.padding(horizontal = ScanPangDimens.screenHorizontal),
            verticalArrangement = Arrangement.spacedBy(ScanPangDimens.detailSectionSpacing),
        ) {
            Spacer(modifier = Modifier.height(ScanPangSpacing.md))
            Text(
                text = "할랄가든 명동점",
                style = ScanPangType.detailRestaurantTitle24,
                color = ScanPangColors.OnSurfaceStrong,
            )
            Text(
                text = "한식 · 도보 2분",
                style = ScanPangType.detailMetaSubtitle13,
                color = ScanPangColors.OnSurfaceMuted,
            )
            Row(
                horizontalArrangement = Arrangement.spacedBy(ScanPangDimens.stackGap6),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                RestaurantTrustChip(text = "할랄 인증", icon = Icons.Rounded.Verified)
                RestaurantTrustChip(text = "방문자 추천", icon = Icons.Rounded.Star)
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Button(
                    onClick = {
                        navController.navigate(AppRoutes.ArNavMap) { launchSingleTop = true }
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(ScanPangDimens.detailCtaHeight),
                    shape = ScanPangShapes.radius12,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = ScanPangColors.Primary,
                        contentColor = Color.White,
                    ),
                ) {
                    Text(
                        text = "길안내 시작",
                        style = ScanPangType.body15Medium,
                    )
                }
                OutlinedButton(
                    onClick = { },
                    modifier = Modifier.size(ScanPangDimens.detailCtaSide),
                    shape = ScanPangShapes.radius12,
                    border = BorderStroke(
                        ScanPangDimens.borderHairline,
                        ScanPangColors.OutlineSubtle,
                    ),
                    contentPadding = PaddingValues(),
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Phone,
                        contentDescription = "전화",
                        tint = ScanPangColors.OnSurfaceStrong,
                    )
                }
            }
            Surface(
                shape = ScanPangShapes.detailVisitCard,
                color = ScanPangColors.DetailVisitOpenSurface,
                border = BorderStroke(
                    ScanPangDimens.borderHairline,
                    ScanPangColors.DetailVisitOpenBorder,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(ScanPangSpacing.md),
                    horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CheckCircle,
                        contentDescription = null,
                        tint = ScanPangColors.StatusOpen,
                        modifier = Modifier.size(ScanPangDimens.icon18),
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(ScanPangDimens.icon5)) {
                        Text(
                            text = "지금 방문 가능",
                            style = ScanPangType.title14,
                            color = ScanPangColors.OnSurfaceStrong,
                        )
                        Text(
                            text = "영업 중 · 월–일 11:00–22:00",
                            style = ScanPangType.caption12Medium,
                            color = ScanPangColors.OnSurfaceMuted,
                        )
                    }
                }
            }
            HorizontalDivider(color = ScanPangColors.OutlineSubtle)
            Text(
                text = "소개",
                style = ScanPangType.detailSectionTitle15,
                color = ScanPangColors.OnSurfaceStrong,
            )
            Text(
                text = "명동 한복판에서 한우와 전통 한식을 할랄 기준으로 즐길 수 있는 공간입니다. 가족 단위 방문에 적합합니다.",
                style = ScanPangType.detailIntro13,
                color = ScanPangColors.OnSurfaceMuted,
            )
            Text(
                text = "대표 메뉴",
                style = ScanPangType.detailSectionTitle15,
                color = ScanPangColors.OnSurfaceStrong,
            )
            DetailMenuLine(name = "한우 불고기 정식", price = "15,000원")
            DetailMenuLine(name = "된장찌개 세트", price = "9,000원")
            Text(
                text = "상세 정보",
                style = ScanPangType.detailSectionTitle15,
                color = ScanPangColors.OnSurfaceStrong,
            )
            DetailInfoLine(
                icon = Icons.Rounded.Place,
                label = "주소",
                value = "서울특별시 중구 명동길 26",
            )
            DetailInfoLine(
                icon = Icons.Rounded.Phone,
                label = "전화",
                value = "02-1234-5678",
            )
            DetailInfoLine(
                icon = Icons.Rounded.AccessTime,
                label = "영업시간",
                value = "11:00 – 22:00 (연중무휴)",
            )
            Spacer(modifier = Modifier.height(ScanPangDimens.detailContentBottomPad))
        }
    }
}

@Composable
private fun RestaurantTrustChip(
    text: String,
    icon: ImageVector,
) {
    Row(
        modifier = Modifier
            .clip(ScanPangShapes.badge6)
            .background(ScanPangColors.TrustPillBackground)
            .padding(
                horizontal = ScanPangDimens.trustChipHorizontal,
                vertical = ScanPangDimens.trustChipVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanPangDimens.trustIconGap),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ScanPangDimens.icon10),
            tint = ScanPangColors.TrustPillText,
        )
        Text(
            text = text,
            style = ScanPangType.badge9SemiBold,
            color = ScanPangColors.TrustPillText,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun DetailMenuLine(
    name: String,
    price: String,
) {
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
            text = name,
            style = ScanPangType.caption12Medium,
            color = ScanPangColors.OnSurfaceStrong,
        )
        Text(
            text = price,
            style = ScanPangType.detailMenuPrice14,
            color = ScanPangColors.OnSurfaceStrong,
        )
    }
}

@Composable
private fun DetailInfoLine(
    icon: ImageVector,
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
        verticalAlignment = Alignment.Top,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(ScanPangDimens.icon18),
            tint = ScanPangColors.OnSurfaceMuted,
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(ScanPangDimens.icon5),
        ) {
            Text(
                text = label,
                style = ScanPangType.meta11Medium,
                color = ScanPangColors.OnSurfacePlaceholder,
            )
            Text(
                text = value,
                style = ScanPangType.detailIntro13,
                color = ScanPangColors.OnSurfaceStrong,
            )
        }
    }
}
