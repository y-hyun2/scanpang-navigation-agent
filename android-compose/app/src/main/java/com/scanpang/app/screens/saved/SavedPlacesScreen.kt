package com.scanpang.app.screens.saved

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangThemeAccessor
import com.scanpang.app.ui.theme.ScanPangTypeScale

@Composable
fun SavedPlacesScreen(
    modifier: Modifier = Modifier,
) {
    val c = ScanPangThemeAccessor.colors
    Text(
        text = "저장한 장소\n(RN SavedPlacesScreen — 상세 구현 예정)",
        fontSize = ScanPangTypeScale.Md,
        fontWeight = FontWeight.Medium,
        color = c.textPrimary,
        modifier = modifier
            .fillMaxSize()
            .padding(ScanPangSpacing.S5),
    )
}
