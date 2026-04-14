package com.scanpang.app.components.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.scanpang.app.components.ar.ArCameraBackdrop
import com.scanpang.app.components.ar.ArNavSideVolumeCamera
import com.scanpang.app.components.ar.ArNavStandaloneChatBlock
import com.scanpang.app.ui.ScanPangFigmaAssets
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

@Composable
fun DetailArStoreOverlayScaffold(
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    onVolumeClick: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(modifier = modifier.fillMaxSize()) {
        ArCameraBackdrop(showFreezeTint = false, modifier = Modifier.fillMaxSize())
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            ArNavStandaloneChatBlock(
                userMessage = "할랄 메뉴 추천해줘",
                agentMessage = "대표 메뉴는 한우 불고기 정식이에요. 매장 내 모든 육류는 할랄 인증을 받았습니다.",
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
                    .height(ScanPangDimens.detailArStorePanelHeight),
                shape = ScanPangShapes.detailArStoreCard,
                color = ScanPangColors.DetailArStoreCardSurface,
                shadowElevation = ScanPangDimens.arPoiCardShadowElevation,
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(ScanPangDimens.detailArStoreHeroHeight)
                            .clip(ScanPangShapes.detailArStoreImageTop),
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
                    }
                    Column(
                        modifier = Modifier.padding(ScanPangSpacing.lg),
                        verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(ScanPangDimens.stackGap6),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StoreHalalBadge()
                            Surface(
                                shape = ScanPangShapes.badge6,
                                color = ScanPangColors.PrimarySoft,
                            ) {
                                Text(
                                    text = "한식",
                                    modifier = Modifier.padding(
                                        horizontal = ScanPangSpacing.sm,
                                        vertical = ScanPangDimens.badgePadVertical,
                                    ),
                                    style = ScanPangType.badge9SemiBold,
                                    color = ScanPangColors.Primary,
                                )
                            }
                            Text(
                                text = "80m",
                                style = ScanPangType.meta11Medium,
                                color = ScanPangColors.OnSurfaceMuted,
                            )
                            Text(
                                text = "영업 중",
                                style = ScanPangType.meta11SemiBold,
                                color = ScanPangColors.StatusOpen,
                            )
                        }
                        Text(
                            text = "할랄가든 명동점",
                            style = ScanPangType.detailPlaceTitle18,
                            color = ScanPangColors.OnSurfaceStrong,
                        )
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(ScanPangDimens.stackGap6),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            StoreTrustChip(text = "할랄 인증", icon = Icons.Rounded.Verified)
                            StoreTrustChip(text = "방문자 추천", icon = Icons.Rounded.Star)
                        }
                        StoreMenuRow(name = "한우 불고기 정식", price = "15,000원")
                        StoreMenuRow(name = "된장찌개 세트", price = "9,000원")
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(
                                text = "리뷰 128개 보기",
                                style = ScanPangType.caption12Medium,
                                color = ScanPangColors.Primary,
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                                contentDescription = null,
                                modifier = Modifier.size(ScanPangDimens.icon18),
                                tint = ScanPangColors.Primary,
                            )
                        }
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
private fun StoreHalalBadge() {
    Row(
        modifier = Modifier
            .clip(ScanPangShapes.badge6)
            .background(ScanPangColors.HalalMeatBadgeBackground)
            .padding(
                horizontal = ScanPangSpacing.sm,
                vertical = ScanPangDimens.badgePadVertical,
            ),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.xs),
    ) {
        Box(
            modifier = Modifier
                .size(ScanPangDimens.icon5)
                .clip(CircleShape)
                .background(ScanPangColors.HalalMeatBadgeText),
        )
        Text(
            text = "HALAL MEAT",
            style = ScanPangType.badge9Bold,
            color = ScanPangColors.HalalMeatBadgeText,
        )
    }
}

@Composable
private fun StoreTrustChip(
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
private fun StoreMenuRow(
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
