package com.scanpang.app.screens.ar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.scanpang.app.navigation.ArRoutes
import com.scanpang.app.ui.theme.ScanPangRadius
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangThemeAccessor
import com.scanpang.app.ui.theme.ScanPangTypeScale

@Composable
fun ArExploreScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val c = ScanPangThemeAccessor.colors
    Box(modifier = modifier.fillMaxSize()) {
        ArCameraPreview(Modifier.fillMaxSize())
        Text(
            text = "AR 탐색 (카메라 프리뷰 · ARCore 예정)",
            color = c.textOnDark,
            fontSize = ScanPangTypeScale.Sm,
            fontWeight = ScanPangTypeScale.W600,
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(top = ScanPangSpacing.S10)
                .background(
                    c.surface.copy(alpha = 0.88f),
                    RoundedCornerShape(ScanPangRadius.Pill),
                )
                .padding(horizontal = ScanPangSpacing.S4, vertical = ScanPangSpacing.S2),
        )
        Button(
            onClick = { navController.navigate(ArRoutes.CHAT) },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = ScanPangSpacing.S10),
            colors = ButtonDefaults.buttonColors(
                containerColor = c.primary,
                contentColor = c.textOnPrimary,
            ),
            shape = RoundedCornerShape(ScanPangRadius.Lg),
        ) {
            Text("AI 채팅으로 이동")
        }
    }
}
