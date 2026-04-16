@file:OptIn(androidx.compose.foundation.ExperimentalFoundationApi::class)

package com.scanpang.app.screens

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.Bookmark
import androidx.compose.material.icons.rounded.BookmarkBorder
import androidx.compose.material.icons.rounded.Cancel
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Fullscreen
import androidx.compose.material.icons.rounded.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.scanpang.app.data.Place
import com.scanpang.app.data.SavedPlaceEntry
import com.scanpang.app.data.SavedPlaceNavTarget
import com.scanpang.app.data.SavedPlacesStore
import com.scanpang.app.ui.ScanPangFigmaAssets
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

/** Coil용 더미 갤러리 — API 연동 시 동일 시그니처로 교체 */
fun defaultPlaceDetailGallery(): List<String> = ScanPangFigmaAssets.RestaurantDetailGallery

fun Place.detailVisitCardsFromPlace(): List<DetailVisitCardUi> {
    val statusTitle = if (isOpen) "지금 방문 가능" else "운영 종료"
    val statusTone = if (isOpen) DetailVisitCardTone.Open else DetailVisitCardTone.Closed
    val hint = if (description.length > 56) description.take(56) + "…" else description
    return listOf(
        DetailVisitCardUi(statusTitle, openHours, statusTone),
        DetailVisitCardUi("안내", hint.ifBlank { "상세 정보는 매장에 문의해 주세요." }, DetailVisitCardTone.Neutral),
    )
}

enum class DetailVisitCardTone {
    Open,
    Closed,
    Neutral,
}

data class DetailVisitCardUi(
    val title: String,
    val subtitle: String,
    val tone: DetailVisitCardTone,
)

data class DetailBookmarkController(
    val bookmarked: Boolean,
    val onToggle: () -> Unit,
)

fun Context.openPhoneDialer(rawPhone: String) {
    val digits = rawPhone.filter { it.isDigit() || it == '+' }
    if (digits.isEmpty()) return
    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$digits"))
    startActivity(intent)
}

@Composable
fun rememberDetailBookmark(
    placeId: String,
    placeName: String,
    category: String,
    distanceLine: String,
    tags: List<String>,
    target: SavedPlaceNavTarget,
): DetailBookmarkController {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val store = remember { SavedPlacesStore(context) }
    var bookmarked by remember(placeId) { mutableStateOf(store.isSaved(placeId)) }

    DisposableEffect(lifecycleOwner, placeId) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                bookmarked = store.isSaved(placeId)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val onToggle: () -> Unit = {
        if (bookmarked) {
            store.remove(placeId)
            bookmarked = false
            Toast.makeText(context, "저장이 해제되었습니다", Toast.LENGTH_SHORT).show()
        } else {
            store.save(
                SavedPlaceEntry(
                    id = placeId,
                    name = placeName,
                    category = category,
                    distanceLine = distanceLine,
                    tags = tags,
                    target = target,
                ),
            )
            bookmarked = true
            Toast.makeText(context, "저장되었습니다", Toast.LENGTH_SHORT).show()
        }
    }

    return DetailBookmarkController(bookmarked, onToggle)
}

@Composable
fun DetailImageFullscreenDialog(
    gallery: List<Any>,
    pagerState: PagerState,
    onDismiss: () -> Unit,
) {
    val context = LocalContext.current
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black),
        ) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier.fillMaxSize(),
            ) { page ->
                AsyncImage(
                    model = ImageRequest.Builder(context)
                        .data(gallery[page])
                        .crossfade(true)
                        .build(),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Fit,
                )
            }
            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(ScanPangSpacing.sm),
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "닫기",
                    tint = Color.White,
                )
            }
        }
    }
}

@Composable
fun DetailHeroPhotoPager(
    gallery: List<Any>,
    pagerState: PagerState,
    onBack: () -> Unit,
    onFullscreenClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(ScanPangDimens.detailPhotoHeroHeight),
    ) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
        ) { page ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(gallery[page])
                    .crossfade(true)
                    .build(),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop,
            )
        }
        IconButton(
            onClick = onBack,
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
        if (onFullscreenClick != null) {
            IconButton(
                onClick = onFullscreenClick,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .statusBarsPadding()
                    .padding(ScanPangSpacing.sm),
            ) {
                Surface(
                    shape = CircleShape,
                    color = ScanPangColors.ArOverlayWhite93,
                    shadowElevation = ScanPangDimens.arPoiCardShadowElevation,
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Fullscreen,
                        contentDescription = "전체 화면",
                        modifier = Modifier.padding(ScanPangSpacing.sm),
                        tint = ScanPangColors.OnSurfaceStrong,
                    )
                }
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
                text = "${pagerState.currentPage + 1}/${gallery.size}",
                modifier = Modifier.padding(
                    horizontal = ScanPangSpacing.sm,
                    vertical = ScanPangDimens.badgePadVertical,
                ),
                style = ScanPangType.detailImageCount9,
                color = Color.White,
            )
        }
    }
}

@Composable
fun DetailScrollTopBackRow(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(ScanPangSpacing.sm),
        horizontalArrangement = Arrangement.Start,
    ) {
        IconButton(onClick = onBack) {
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
    }
}

@Composable
fun DetailTitleBookmarkRow(
    title: String,
    bookmarked: Boolean,
    onBookmarkClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
    ) {
        Text(
            text = title,
            style = ScanPangType.detailRestaurantTitle24,
            color = ScanPangColors.OnSurfaceStrong,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onBookmarkClick) {
            Icon(
                imageVector = if (bookmarked) Icons.Rounded.Bookmark else Icons.Rounded.BookmarkBorder,
                contentDescription = if (bookmarked) "저장됨" else "저장",
                tint = if (bookmarked) {
                    ScanPangColors.Primary
                } else {
                    ScanPangColors.OnSurfacePlaceholder
                },
            )
        }
    }
}

@Composable
fun DetailCategoryDistanceLine(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = ScanPangType.detailMetaSubtitle13,
        color = ScanPangColors.OnSurfaceMuted,
    )
}

@Composable
fun DetailCategoryTagDistanceRow(
    categoryLabel: String,
    distanceText: String,
    modifier: Modifier = Modifier,
    trailing: (@Composable () -> Unit)? = null,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
    ) {
        Surface(
            shape = ScanPangShapes.badge6,
            color = ScanPangColors.PrimarySoft,
        ) {
            Text(
                text = categoryLabel,
                modifier = Modifier.padding(
                    horizontal = ScanPangSpacing.sm,
                    vertical = ScanPangDimens.chipPadVertical,
                ),
                style = ScanPangType.category11SemiBold,
                color = ScanPangColors.Primary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        Text(
            text = distanceText,
            style = ScanPangType.detailMetaSubtitle13,
            color = ScanPangColors.OnSurfaceMuted,
            maxLines = 1,
            modifier = Modifier.weight(1f, fill = false),
        )
        trailing?.invoke()
    }
}

@Composable
fun DetailNavigateWideButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    label: String = "길안내 시작",
) {
    Button(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .height(ScanPangDimens.detailCtaHeight),
        shape = ScanPangShapes.radius12,
        colors = ButtonDefaults.buttonColors(
            containerColor = ScanPangColors.Primary,
            contentColor = Color.White,
        ),
    ) {
        Text(text = label, style = ScanPangType.body15Medium)
    }
}

@Composable
fun DetailNavigateAndSideIconRow(
    onNavigate: () -> Unit,
    sideIcon: ImageVector,
    sideContentDescription: String,
    onSideClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Button(
            onClick = onNavigate,
            modifier = Modifier
                .weight(1f)
                .height(ScanPangDimens.detailCtaHeight),
            shape = ScanPangShapes.radius12,
            colors = ButtonDefaults.buttonColors(
                containerColor = ScanPangColors.Primary,
                contentColor = Color.White,
            ),
        ) {
            Text(text = "길안내 시작", style = ScanPangType.body15Medium)
        }
        OutlinedButton(
            onClick = onSideClick,
            modifier = Modifier.size(ScanPangDimens.detailCtaSide),
            shape = ScanPangShapes.radius12,
            border = BorderStroke(
                ScanPangDimens.borderHairline,
                ScanPangColors.OutlineSubtle,
            ),
            contentPadding = PaddingValues(),
        ) {
            Icon(
                imageVector = sideIcon,
                contentDescription = sideContentDescription,
                tint = ScanPangColors.OnSurfaceStrong,
            )
        }
    }
}

@Composable
fun DetailVisitCardsHorizontalPager(
    cards: List<DetailVisitCardUi>,
    modifier: Modifier = Modifier,
) {
    if (cards.isEmpty()) return
    val pagerState = rememberPagerState(pageCount = { cards.size })
    HorizontalPager(
        state = pagerState,
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(horizontal = ScanPangDimens.screenHorizontal),
        pageSpacing = ScanPangSpacing.md,
    ) { page ->
        val card = cards[page]
        val style = when (card.tone) {
            DetailVisitCardTone.Open -> VisitCardVisual(
                surface = ScanPangColors.DetailVisitOpenSurface,
                border = ScanPangColors.DetailVisitOpenBorder,
                icon = Icons.Rounded.CheckCircle,
                iconTint = ScanPangColors.StatusOpen,
            )
            DetailVisitCardTone.Closed -> VisitCardVisual(
                surface = ScanPangColors.DetailVisitClosedSurface,
                border = ScanPangColors.DetailVisitClosedBorder,
                icon = Icons.Rounded.Cancel,
                iconTint = ScanPangColors.Error,
            )
            DetailVisitCardTone.Neutral -> VisitCardVisual(
                surface = ScanPangColors.DetailVisitNeutralSurface,
                border = ScanPangColors.DetailVisitNeutralBorder,
                icon = Icons.Rounded.Info,
                iconTint = ScanPangColors.Primary,
            )
        }
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(ScanPangDimens.detailVisitPagerCardMinHeight),
            shape = ScanPangShapes.detailVisitCard,
            color = style.surface,
            border = BorderStroke(ScanPangDimens.borderHairline, style.border),
        ) {
            Row(
                modifier = Modifier.padding(ScanPangSpacing.md),
                horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.md),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = style.icon,
                    contentDescription = null,
                    tint = style.iconTint,
                    modifier = Modifier.size(ScanPangDimens.icon18),
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(ScanPangDimens.icon5),
                    modifier = Modifier.weight(1f),
                ) {
                    Text(
                        text = card.title,
                        style = ScanPangType.title14,
                        color = ScanPangColors.OnSurfaceStrong,
                    )
                    Text(
                        text = card.subtitle,
                        style = ScanPangType.caption12Medium,
                        color = ScanPangColors.OnSurfaceMuted,
                    )
                }
            }
        }
    }
}

private data class VisitCardVisual(
    val surface: Color,
    val border: Color,
    val icon: ImageVector,
    val iconTint: Color,
)

@Composable
fun DetailSectionHeader(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = title,
        modifier = modifier,
        style = ScanPangType.detailSectionTitle15,
        color = ScanPangColors.OnSurfaceStrong,
    )
}

@Composable
fun DetailIntroBody(
    text: String,
    modifier: Modifier = Modifier,
) {
    Text(
        text = text,
        modifier = modifier,
        style = ScanPangType.detailIntro13,
        color = ScanPangColors.OnSurfaceMuted,
    )
}

@Composable
fun DetailInfoLine(
    icon: ImageVector,
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DetailFacilityTagRow(
    tags: List<String>,
    modifier: Modifier = Modifier,
) {
    if (tags.isEmpty()) return
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
    ) {
        tags.forEach { tag ->
            DetailFacilityTagChip(text = tag)
        }
    }
}

@Composable
private fun DetailFacilityTagChip(text: String) {
    Surface(
        shape = ScanPangShapes.badge6,
        color = ScanPangColors.DetailFacilityTagBackground,
        border = BorderStroke(ScanPangDimens.borderHairline, ScanPangColors.OutlineSubtle),
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = ScanPangSpacing.sm,
                vertical = ScanPangDimens.chipPadVertical,
            ),
            style = ScanPangType.tag11Medium,
            color = ScanPangColors.OnSurfaceStrong,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun DetailMenuPriceRow(
    name: String,
    price: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
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
fun DetailScreenDivider() {
    HorizontalDivider(color = ScanPangColors.OutlineSubtle)
}

@Composable
fun DetailContentBottomSpacer() {
    Spacer(modifier = Modifier.height(ScanPangDimens.detailContentBottomPad))
}
