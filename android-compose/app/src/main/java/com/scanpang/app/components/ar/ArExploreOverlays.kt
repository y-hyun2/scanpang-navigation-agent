package com.scanpang.app.components.ar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.CropFree
import androidx.compose.material.icons.rounded.FilterList
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

@Composable
fun ArTopGradientBar(
    modifier: Modifier = Modifier,
    onHomeClick: () -> Unit,
    onSearchClick: () -> Unit,
    centerContent: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        ScanPangColors.ArTopGradientStart,
                        ScanPangColors.ArTopGradientEnd,
                    ),
                ),
            )
            .statusBarsPadding()
            .padding(horizontal = ScanPangDimens.arTopBarHorizontal)
            .padding(bottom = ScanPangDimens.arTopBarBottomPadding),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(
                    maxOf(
                        ScanPangDimens.arCircleBtn36,
                        ScanPangDimens.arStatusPillHeight,
                    ),
                ),
        ) {
            ArCircleIconButton(
                icon = Icons.Rounded.Home,
                contentDescription = "홈",
                onClick = onHomeClick,
                modifier = Modifier.align(Alignment.CenterStart),
            )
            Box(
                modifier = Modifier.align(Alignment.Center),
                contentAlignment = Alignment.Center,
            ) {
                centerContent()
            }
            ArCircleIconButton(
                icon = Icons.Rounded.Search,
                contentDescription = "검색",
                onClick = onSearchClick,
                modifier = Modifier.align(Alignment.CenterEnd),
            )
        }
    }
}

@Composable
fun ArCircleIconButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .size(ScanPangDimens.arCircleBtn36)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = ScanPangColors.ArOverlayWhite80,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(ScanPangDimens.icon20),
                tint = ScanPangColors.OnSurfaceStrong,
            )
        }
    }
}

@Composable
fun ArStatusPillNeutral(
    text: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val mod = if (onClick != null) {
        modifier.clickable(onClick = onClick)
    } else {
        modifier
    }
    Surface(
        modifier = mod.height(ScanPangDimens.arStatusPillHeight),
        shape = CircleShape,
        color = ScanPangColors.ArOverlayWhite80,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ScanPangDimens.arStatusPillHorizontalPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Rounded.CropFree,
                contentDescription = null,
                modifier = Modifier.size(ScanPangDimens.icon18),
                tint = ScanPangColors.OnSurfaceStrong,
            )
            Text(
                text = text,
                style = ScanPangType.arStatusPill15,
                color = ScanPangColors.OnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ArStatusPillPrimary(
    text: String,
    icon: ImageVector,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val mod = if (onClick != null) modifier.clickable(onClick = onClick) else modifier
    Surface(
        modifier = mod.height(ScanPangDimens.arStatusPillHeight),
        shape = CircleShape,
        color = ScanPangColors.Primary,
    ) {
        Row(
            modifier = Modifier.padding(horizontal = ScanPangDimens.arStatusPillHorizontalPad),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(ScanPangDimens.icon18),
                tint = Color.White,
            )
            Text(
                text = text,
                style = ScanPangType.arStatusPill15,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
fun ArSideActionColumn(
    onVolumeClick: () -> Unit,
    onCameraClick: () -> Unit,
    modifier: Modifier = Modifier,
    cameraSurfaceColor: Color = ScanPangColors.ArOverlayWhite93,
    cameraIconTint: Color = ScanPangColors.OnSurfaceStrong,
) {
    Column(
        modifier = modifier.width(ScanPangDimens.arSideColumnWidth),
        verticalArrangement = Arrangement.spacedBy(ScanPangDimens.arSideIconGap),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        ArSideFab(
            icon = Icons.AutoMirrored.Rounded.VolumeUp,
            contentDescription = "볼륨",
            onClick = onVolumeClick,
            surfaceColor = ScanPangColors.ArOverlayWhite85,
        )
        ArSideFab(
            icon = Icons.Rounded.CameraAlt,
            contentDescription = "촬영",
            onClick = onCameraClick,
            surfaceColor = cameraSurfaceColor,
            iconTint = cameraIconTint,
        )
    }
}

@Composable
private fun ArSideFab(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    surfaceColor: Color,
    iconTint: Color = ScanPangColors.OnSurfaceStrong,
) {
    Surface(
        modifier = Modifier
            .size(ScanPangDimens.arSideFab44)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        shape = CircleShape,
        color = surfaceColor,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Icon(
                imageVector = icon,
                contentDescription = contentDescription,
                modifier = Modifier.size(ScanPangDimens.icon20),
                tint = iconTint,
            )
        }
    }
}

@Composable
fun ArPoiCard(
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val clickMod = if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier
    Surface(
        modifier = modifier
            .height(ScanPangDimens.arPoiCardHeight)
            .then(clickMod),
        shape = ScanPangShapes.arPoiCard,
        color = ScanPangColors.Surface,
        shadowElevation = ScanPangDimens.arPoiCardShadowElevation,
    ) {
        Row(
            modifier = Modifier
                .padding(
                    horizontal = ScanPangDimens.arPoiCardHorizontalPad,
                    vertical = ScanPangDimens.arPoiCardVerticalPad,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
        ) {
            Surface(
                modifier = Modifier.size(ScanPangDimens.arPoiIcon24),
                shape = CircleShape,
                color = ScanPangColors.PrimarySoft,
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Rounded.Search,
                        contentDescription = null,
                        modifier = Modifier.size(ScanPangDimens.icon14),
                        tint = ScanPangColors.Primary,
                    )
                }
            }
            Column(verticalArrangement = Arrangement.spacedBy(ScanPangDimens.icon5)) {
                Text(
                    text = title,
                    style = ScanPangType.chip13SemiBold,
                    color = ScanPangColors.ArPoiTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = subtitle,
                    style = ScanPangType.meta11Medium,
                    color = ScanPangColors.ArPoiSubtitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }
    }
}

@Composable
fun ArChatBottomSection(
    userMessage: String,
    agentMessage: String,
    inputPlaceholder: String,
    modifier: Modifier = Modifier,
    agentTag: (@Composable () -> Unit)? = null,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ScanPangColors.ArBottomChatScrim)
            .padding(horizontal = ScanPangDimens.arTopBarHorizontal)
            .padding(bottom = ScanPangDimens.arChatAreaBottomPad)
            .heightIn(max = ScanPangDimens.arChatAreaMaxHeight),
        verticalArrangement = Arrangement.spacedBy(ScanPangDimens.arChatBubbleGap),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Start) {
            Column(horizontalAlignment = Alignment.Start) {
                if (agentTag != null) {
                    agentTag()
                    Spacer(modifier = Modifier.height(ScanPangSpacing.xs))
                }
                Surface(
                    shape = ScanPangShapes.arBubbleAgent,
                    color = ScanPangColors.ArOverlayWhite85,
                ) {
                    Text(
                        text = agentMessage,
                        modifier = Modifier.padding(ScanPangSpacing.md),
                        style = ScanPangType.arChatBody14,
                        color = ScanPangColors.OnSurfaceStrong,
                    )
                }
            }
        }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            Surface(
                shape = ScanPangShapes.arBubbleUser,
                color = ScanPangColors.ArOverlayWhite80,
            ) {
                Text(
                    text = userMessage,
                    modifier = Modifier.padding(ScanPangSpacing.md),
                    style = ScanPangType.arChatBody14,
                    color = ScanPangColors.OnSurfaceStrong,
                )
            }
        }
        ArChatInputBar(placeholder = inputPlaceholder)
    }
}

@Composable
fun ArChatInputBar(
    placeholder: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = ScanPangDimens.arInputBarMinHeight),
        shape = ScanPangShapes.arInputPill,
        color = ScanPangColors.ArOverlayWhite93,
    ) {
        Row(
            modifier = Modifier.padding(
                horizontal = ScanPangDimens.arInputInnerPadH,
                vertical = ScanPangDimens.arInputInnerPadV,
            ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
        ) {
            Icon(
                imageVector = Icons.Rounded.Mic,
                contentDescription = null,
                modifier = Modifier.size(ScanPangDimens.arMicSendIcon),
                tint = ScanPangColors.OnSurfaceMuted,
            )
            Text(
                text = placeholder,
                modifier = Modifier.weight(1f),
                style = ScanPangType.searchPlaceholderRegular,
                color = ScanPangColors.OnSurfacePlaceholder,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Surface(
                shape = CircleShape,
                color = ScanPangColors.ArSendChipBackground,
                modifier = Modifier.size(ScanPangDimens.arMicSendIcon),
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.Send,
                        contentDescription = "전송",
                        modifier = Modifier.size(ScanPangDimens.icon16),
                        tint = ScanPangColors.OnSurfaceMuted,
                    )
                }
            }
        }
    }
}

@Composable
fun ArRecommendHalalTag(text: String) {
    Surface(
        shape = ScanPangShapes.badge6,
        color = ScanPangColors.ArRecommendTagHalalBackground,
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(
                horizontal = ScanPangDimens.arSearchTagHorizontalPad,
                vertical = ScanPangDimens.arSearchTagVerticalPad,
            ),
            style = ScanPangType.tag11Medium,
            color = ScanPangColors.Primary,
        )
    }
}

private val KeyboardRow1 = listOf("ㅂ", "ㅈ", "ㄷ", "ㄱ", "ㅅ", "ㅛ", "ㅕ", "ㅑ", "ㅐ", "ㅔ")
private val KeyboardRow2 = listOf("ㅁ", "ㄴ", "ㅇ", "ㄹ", "ㅎ", "ㅗ", "ㅓ", "ㅏ", "ㅣ")
private val KeyboardRow3 = listOf("ㅋ", "ㅌ", "ㅊ", "ㅍ", "ㅠ", "ㅜ", "ㅡ")

@Composable
fun ArIosStyleKeyboardPanel(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(ScanPangColors.ArKeyboardIosBackground)
            .navigationBarsPadding()
            .padding(ScanPangSpacing.sm),
        verticalArrangement = Arrangement.spacedBy(ScanPangDimens.arKeyboardKeyGap),
    ) {
        ArKeyboardRow(keys = KeyboardRow1)
        ArKeyboardRow(keys = KeyboardRow2, indent = true)
        ArKeyboardRowWithShiftDelete(keys = KeyboardRow3)
        ArKeyboardFunctionRow()
    }
}

@Composable
private fun ArKeyboardRow(
    keys: List<String>,
    indent: Boolean = false,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = if (indent) ScanPangSpacing.lg else ScanPangSpacing.xs),
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.xs),
    ) {
        keys.forEach { key ->
            ArKeyboardLetterKey(
                label = key,
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun ArKeyboardRowWithShiftDelete(keys: List<String>) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.xs),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArKeyboardFunctionKey(
            modifier = Modifier.width(ScanPangDimens.arSideFab44),
            content = {
                Text("⇧", style = ScanPangType.arKeyboardKey22, color = ScanPangColors.OnSurfaceStrong)
            },
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.xs),
        ) {
            keys.forEach { key ->
                ArKeyboardLetterKey(label = key, modifier = Modifier.weight(1f))
            }
        }
        ArKeyboardFunctionKey(
            modifier = Modifier.width(ScanPangDimens.arSideFab44),
            content = {
                Text("⌫", style = ScanPangType.arKeyboardKey22, color = ScanPangColors.OnSurfaceStrong)
            },
        )
    }
}

@Composable
private fun ArKeyboardFunctionRow() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(ScanPangDimens.arKeyboardKeyGap),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ArKeyboardFunctionKey(
            modifier = Modifier.size(ScanPangDimens.arKeyboardKeyHeight),
            content = {
                Text("12", style = ScanPangType.caption12Medium, color = ScanPangColors.OnSurfaceStrong)
            },
        )
        ArKeyboardFunctionKey(
            modifier = Modifier.size(ScanPangDimens.arKeyboardKeyHeight),
            content = {
                Text("😀", style = ScanPangType.caption12Medium, color = ScanPangColors.OnSurfaceStrong)
            },
        )
        Surface(
            modifier = Modifier
                .weight(1f)
                .height(ScanPangDimens.arKeyboardKeyHeight),
            shape = ScanPangShapes.arKeyboardKey,
            color = ScanPangColors.Surface,
        ) {}
        ArKeyboardFunctionKey(
            modifier = Modifier
                .width(ScanPangDimens.arInputBarMinHeight + ScanPangSpacing.lg)
                .height(ScanPangDimens.arKeyboardKeyHeight),
            content = {
                Text("↵", style = ScanPangType.arKeyboardKey22, color = ScanPangColors.OnSurfaceStrong)
            },
        )
    }
}

@Composable
private fun ArKeyboardLetterKey(label: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.height(ScanPangDimens.arKeyboardKeyHeight),
        shape = ScanPangShapes.arKeyboardKey,
        color = ScanPangColors.Surface,
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(text = label, style = ScanPangType.arKeyboardKey22, color = ScanPangColors.OnSurfaceStrong)
        }
    }
}

@Composable
private fun ArKeyboardFunctionKey(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    Surface(
        modifier = modifier,
        shape = ScanPangShapes.arKeyboardKey,
        color = ScanPangColors.ArKeyboardIosFunctionKey,
    ) {
        Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
            content()
        }
    }
}

@Composable
fun BoxScope.ArPoiPinsLayer(
    onPoiOneClick: (() -> Unit)? = null,
    onPoiTwoClick: (() -> Unit)? = null,
) {
    ArPoiCard(
        title = "눈스퀘어",
        subtitle = "쇼핑 · 10m",
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(
                start = ScanPangDimens.arPoiOneStart,
                top = ScanPangDimens.arPoiOneTop,
            ),
        onClick = onPoiOneClick,
    )
    ArPoiCard(
        title = "명동빌딩",
        subtitle = "쇼핑 · 10m",
        modifier = Modifier
            .align(Alignment.TopStart)
            .padding(
                start = ScanPangDimens.arPoiTwoStart,
                top = ScanPangDimens.arPoiTwoTop,
            ),
        onClick = onPoiTwoClick,
    )
}

@Composable
fun BoxScope.ArSideButtonsLayer(
    onVolumeClick: () -> Unit,
    onCameraClick: () -> Unit,
    cameraSurfaceColor: Color = ScanPangColors.ArOverlayWhite93,
    cameraIconTint: Color = ScanPangColors.OnSurfaceStrong,
) {
    ArSideActionColumn(
        onVolumeClick = onVolumeClick,
        onCameraClick = onCameraClick,
        modifier = Modifier
            .align(Alignment.TopEnd)
            .padding(
                end = ScanPangDimens.arSideColumnEnd,
                top = ScanPangDimens.arSideColumnTop,
            ),
        cameraSurfaceColor = cameraSurfaceColor,
        cameraIconTint = cameraIconTint,
    )
}

@Composable
fun ArFilterChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = if (selected) ScanPangColors.PrimarySoft else ScanPangColors.Surface
    val fg = if (selected) ScanPangColors.Primary else ScanPangColors.OnSurfaceStrong
    val borderColor = if (selected) ScanPangColors.Primary else ScanPangColors.OutlineSubtle
    Surface(
        modifier = modifier
            .height(ScanPangDimens.arFilterChipHeight)
            .clip(ScanPangShapes.filterChip)
            .clickable(onClick = onClick)
            .border(ScanPangDimens.borderHairline, borderColor, ScanPangShapes.filterChip),
        shape = ScanPangShapes.filterChip,
        color = bg,
    ) {
        Box(
            modifier = Modifier.padding(horizontal = ScanPangSpacing.md, vertical = ScanPangSpacing.xs),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = label, style = ScanPangType.chip12Medium, color = fg, maxLines = 1)
        }
    }
}

@Composable
fun ArFilterChipRow(
    labels: List<String>,
    selected: String?,
    onSelect: (String) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.sm),
    ) {
        labels.forEach { label ->
            ArFilterChip(
                label = label,
                selected = label == selected,
                onClick = { onSelect(label) },
            )
        }
    }
}
