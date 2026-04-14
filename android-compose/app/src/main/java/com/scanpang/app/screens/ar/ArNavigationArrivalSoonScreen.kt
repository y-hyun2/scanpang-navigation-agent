package com.scanpang.app.screens.ar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.ArrowUpward
import androidx.compose.material.icons.rounded.LocationOn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import com.scanpang.app.components.ar.ArCameraBackdrop
import com.scanpang.app.components.ar.ArNavActionCardCluster
import com.scanpang.app.components.ar.ArNavDestinationPill
import com.scanpang.app.components.ar.ArNavSideVolumeCamera
import com.scanpang.app.components.ar.ArNavStandaloneChatBlock
import com.scanpang.app.components.ar.ArNavTopHud
import com.scanpang.app.components.ar.ArNavTurnBadge
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens

/**
 * Figma: ScanPang - AR 도착 직전 — node `162:2321`
 */
@Composable
fun ArNavigationArrivalSoonScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        ArCameraBackdrop(showFreezeTint = false, modifier = Modifier.fillMaxSize())

        ArNavActionCardCluster(
            showNextStep = false,
            nextDistance = "",
            currentManeuverIcon = Icons.Rounded.ArrowUpward,
            currentDistance = "80m",
            currentInstruction = "목적지가 바로 앞에 있습니다!",
        )

        ArNavTopHud(
            modifier = Modifier.align(Alignment.TopStart),
            onHomeClick = { navController.popBackStack() },
            onSearchClick = { navController.navigate(AppRoutes.ArSearch) },
            destinationPill = {
                ArNavDestinationPill(
                    text = "명동성당 안내 중",
                    containerColor = ScanPangColors.Primary,
                )
            },
        )

        ArNavSideVolumeCamera(
            onVolumeClick = { },
            onCameraClick = { },
        )

        ArNavTurnBadge(
            icon = Icons.Rounded.LocationOn,
            iconSize = ScanPangDimens.arNavLocationBadgeIcon,
            badgeColor = ScanPangColors.ArNavPrimaryBadge90,
            iconTint = Color.White,
        )

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            ArNavStandaloneChatBlock(
                userMessage = "눈스퀘어가 뭐야?",
                agentMessage = "거의 다 왔어요! 입구는 정면 오른쪽이에요.",
                inputPlaceholder = "무엇이든 물어보세요",
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
