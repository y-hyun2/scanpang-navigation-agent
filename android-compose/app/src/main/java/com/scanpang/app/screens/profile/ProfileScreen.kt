package com.scanpang.app.screens.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Logout
import androidx.compose.material.icons.filled.MailOutline
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material3.ripple
import androidx.compose.runtime.remember
import com.scanpang.app.ui.theme.ScanPangElevation
import com.scanpang.app.ui.theme.ScanPangRadius
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangThemeAccessor
import com.scanpang.app.ui.theme.ScanPangTypeScale

private data class RowItem(
    val key: String,
    val icon: ImageVector,
    val label: String,
    val danger: Boolean = false,
)

private val TravelRows = listOf(
    RowItem("lang", Icons.Filled.Language, "언어 설정"),
    RowItem("halal", Icons.Filled.Restaurant, "할랄 우선 설정"),
    RowItem("pray", Icons.Filled.Mosque, "기도 지원 설정"),
    RowItem("tts", Icons.Filled.VolumeUp, "TTS 음성 안내"),
)

private val AppRows = listOf(
    RowItem("saved", Icons.Filled.Bookmark, "저장한 장소"),
    RowItem("notif", Icons.Filled.NotificationsNone, "알림 설정"),
)

private val OtherRows = listOf(
    RowItem("help", Icons.Filled.HelpOutline, "도움말"),
    RowItem("contact", Icons.Filled.MailOutline, "문의하기"),
    RowItem("logout", Icons.Filled.Logout, "로그아웃", danger = true),
)

@Composable
fun ProfileScreen(modifier: Modifier = Modifier) {
    val c = ScanPangThemeAccessor.colors
    val scroll = rememberScrollState()

    Column(
        modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(scroll)
            .padding(ScanPangSpacing.S5),
        verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.S2 + 2.dp),
    ) {
        Text(
            "내 정보",
            fontSize = ScanPangTypeScale.Xl,
            fontWeight = ScanPangTypeScale.W700,
            color = c.textPrimary,
        )
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(ScanPangElevation.Sm, RoundedCornerShape(ScanPangRadius.Lg)),
            shape = RoundedCornerShape(ScanPangRadius.Lg),
            color = c.surface,
        ) {
            Column(
                Modifier.padding(ScanPangSpacing.S3 + 2.dp),
                verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.S2 + 2.dp),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.S4),
                ) {
                    Box(
                        Modifier
                            .size(ScanPangSpacing.S12)
                            .background(c.primary, CircleShape),
                        contentAlignment = Alignment.Center,
                    ) {
                        Text(
                            "F",
                            fontSize = ScanPangTypeScale.Lg,
                            fontWeight = ScanPangTypeScale.W700,
                            color = c.textOnPrimary,
                        )
                    }
                    Column(verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.S1)) {
                        Text(
                            "Fatima",
                            fontSize = 18.sp,
                            fontWeight = ScanPangTypeScale.W700,
                            color = c.textPrimary,
                        )
                        Text(
                            "혼자 여행 중 · 한국어 · English",
                            fontSize = ScanPangTypeScale.Sm,
                            color = c.textSecondary,
                        )
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.S2),
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    listOf("할랄 우선", "AR 탐색 모드", "TTS 활성").forEach { t ->
                        Row(
                            Modifier
                                .background(c.backgroundOverlay, RoundedCornerShape(ScanPangRadius.Pill))
                                .padding(horizontal = ScanPangSpacing.S3, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                        ) {
                            Box(
                                Modifier
                                    .size(14.dp)
                                    .background(c.primary, RoundedCornerShape(2.dp)),
                            )
                            Text(
                                t,
                                fontSize = ScanPangTypeScale.Sm,
                                fontWeight = ScanPangTypeScale.W500,
                                color = c.primary,
                            )
                        }
                    }
                }
            }
        }
        SectionLabel("여행 설정")
        SettingsGroup(TravelRows)
        SectionLabel("앱 설정")
        SettingsGroup(AppRows)
        SectionLabel("기타")
        SettingsGroup(OtherRows)
        Spacer(Modifier.height(ScanPangSpacing.S10))
    }
}

@Composable
private fun SectionLabel(text: String) {
    val c = ScanPangThemeAccessor.colors
    Text(
        text,
        fontSize = ScanPangTypeScale.Sm,
        fontWeight = ScanPangTypeScale.W600,
        color = c.textSecondary,
        modifier = Modifier.padding(top = ScanPangSpacing.S2),
    )
}

@Composable
private fun SettingsGroup(rows: List<RowItem>) {
    val c = ScanPangThemeAccessor.colors
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(ScanPangElevation.Sm, RoundedCornerShape(ScanPangRadius.Lg)),
        shape = RoundedCornerShape(ScanPangRadius.Lg),
        color = c.surface,
    ) {
        Column {
            rows.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(thickness = 1.dp, color = c.border)
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = ripple(),
                            onClick = { },
                        )
                        .padding(
                            horizontal = ScanPangSpacing.S4,
                            vertical = ScanPangSpacing.S3,
                        ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.S3),
                ) {
                    Icon(
                        row.icon,
                        contentDescription = null,
                        tint = if (row.danger) c.error else c.primary,
                        modifier = Modifier.size(24.dp),
                    )
                    Text(
                        row.label,
                        modifier = Modifier.weight(1f),
                        fontSize = ScanPangTypeScale.Base,
                        fontWeight = ScanPangTypeScale.W500,
                        color = if (row.danger) c.error else c.textPrimary,
                    )
                    Icon(
                        Icons.Filled.ChevronRight,
                        contentDescription = null,
                        tint = c.iconMuted,
                        modifier = Modifier.size(18.dp),
                    )
                }
            }
        }
    }
}
