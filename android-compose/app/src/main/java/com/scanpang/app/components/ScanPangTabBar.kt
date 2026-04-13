package com.scanpang.app.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.CenterFocusWeak
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonOutline
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.scanpang.app.navigation.TabRoutes
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangElevation
import com.scanpang.app.ui.theme.ScanPangRadius
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangThemeAccessor
import com.scanpang.app.ui.theme.ScanPangTypeScale

enum class ScanPangTabId {
    Home,
    Search,
    Explorer,
    Save,
    Profile,
}

@Composable
fun ScanPangTabBar(
    activeTab: ScanPangTabId,
    onHomePress: () -> Unit,
    onSearchPress: () -> Unit,
    onExplorePress: () -> Unit,
    onSavePress: () -> Unit,
    onProfilePress: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ScanPangThemeAccessor.colors
    val tabPillHeight = ScanPangSpacing.S14 + ScanPangSpacing.S2 + 2.dp
    val hMargin = ScanPangSpacing.S5 + 2.dp
    val fabDiameter = ScanPangSpacing.S14
    val float = ScanPangDimens.IconBtnMd

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = hMargin),
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(tabPillHeight)
                .shadow(ScanPangElevation.Sm, RoundedCornerShape(ScanPangRadius.Pill)),
            shape = RoundedCornerShape(ScanPangRadius.Pill),
            color = c.surface,
            border = BorderStroke(0.5.dp, c.border),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ScanPangSpacing.S1),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TabItem(
                    icon = Icons.Filled.Home,
                    label = "홈",
                    selected = activeTab == ScanPangTabId.Home,
                    onClick = onHomePress,
                    modifier = Modifier.weight(1f),
                )
                TabItem(
                    icon = Icons.Filled.Search,
                    label = "검색",
                    selected = activeTab == ScanPangTabId.Search,
                    onClick = onSearchPress,
                    modifier = Modifier.weight(1f),
                )
                Spacer(modifier = Modifier.weight(1f))
                TabItem(
                    icon = if (activeTab == ScanPangTabId.Save) Icons.Filled.Bookmark else Icons.Filled.BookmarkBorder,
                    label = "저장",
                    selected = activeTab == ScanPangTabId.Save,
                    onClick = onSavePress,
                    modifier = Modifier.weight(1f),
                )
                TabItem(
                    icon = if (activeTab == ScanPangTabId.Profile) Icons.Filled.Person else Icons.Filled.PersonOutline,
                    label = "내 정보",
                    selected = activeTab == ScanPangTabId.Profile,
                    onClick = onProfilePress,
                    modifier = Modifier.weight(1f),
                )
            }
        }

        val fabBottomFromPillTop = fabDiameter / 2 + ScanPangSpacing.S3
        val fabGapAndLabel = 2.dp + ScanPangSpacing.S4
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(
                    top = tabPillHeight - fabBottomFromPillTop - fabGapAndLabel,
                ),
            horizontalAlignment = Alignment.CenterVertically,
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(fabDiameter)
                    .shadow(ScanPangElevation.Md, CircleShape)
                    .background(c.primary, CircleShape)
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = androidx.compose.material3.ripple(bounded = true, radius = fabDiameter / 2),
                        onClick = onExplorePress,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = Icons.Filled.CenterFocusWeak,
                    contentDescription = "탐색",
                    tint = c.iconOnPrimary,
                    modifier = Modifier.size(float),
                )
            }
            Text(
                text = "탐색",
                fontSize = ScanPangTypeScale.Xs,
                fontWeight = ScanPangTypeScale.W600,
                letterSpacing = ScanPangTypeScale.LetterWide,
                color = c.primary,
            )
        }
    }
}

@Composable
private fun TabItem(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ScanPangThemeAccessor.colors
    val iconColor = if (selected) c.primary else c.iconMuted
    val labelWeight = if (selected) ScanPangTypeScale.W600 else ScanPangTypeScale.W500
    val labelColor = if (selected) c.primary else c.textPlaceholder

    Column(
        modifier = modifier
            .height(ScanPangSpacing.S14 + ScanPangSpacing.S2)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = iconColor,
            modifier = Modifier.size(ScanPangDimens.IconDefault),
        )
        Text(
            text = label,
            fontSize = ScanPangTypeScale.Xs,
            fontWeight = labelWeight,
            letterSpacing = ScanPangTypeScale.LetterWide,
            color = labelColor,
        )
    }
}

fun tabIdForRoute(route: String?): ScanPangTabId {
    val r = route ?: return ScanPangTabId.Home
    return when {
        r.startsWith("home_") -> ScanPangTabId.Home
        r.startsWith("search_") -> ScanPangTabId.Search
        r.startsWith("ar_") -> ScanPangTabId.Explorer
        r.startsWith("saved_") -> ScanPangTabId.Save
        r.startsWith("profile_") -> ScanPangTabId.Profile
        else -> ScanPangTabId.Home
    }
}

fun tabRouteForId(id: ScanPangTabId): String = when (id) {
    ScanPangTabId.Home -> TabRoutes.HOME_TAB
    ScanPangTabId.Search -> TabRoutes.SEARCH_TAB
    ScanPangTabId.Explorer -> TabRoutes.AR_TAB
    ScanPangTabId.Save -> TabRoutes.SAVED_TAB
    ScanPangTabId.Profile -> TabRoutes.PROFILE_TAB
}
