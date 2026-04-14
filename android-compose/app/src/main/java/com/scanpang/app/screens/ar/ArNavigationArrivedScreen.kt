package com.scanpang.app.screens.ar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.scanpang.app.components.ar.ArArrivalBadgeStack
import com.scanpang.app.components.ar.ArCameraBackdrop
import com.scanpang.app.components.ar.ArNavDestinationPill
import com.scanpang.app.components.ar.ArNavSideVolumeCamera
import com.scanpang.app.components.ar.ArNavStandaloneChatBlock
import com.scanpang.app.components.ar.ArNavTopHud
import com.scanpang.app.navigation.AppRoutes
import com.scanpang.app.ui.theme.ScanPangColors

/**
 * Figma: ScanPang - AR 도착 후 안내 — node `162:2161`
 */
@Composable
fun ArNavigationArrivedScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        ArCameraBackdrop(showFreezeTint = false, modifier = Modifier.fillMaxSize())

        ArNavTopHud(
            modifier = Modifier.align(Alignment.TopStart),
            onHomeClick = { navController.popBackStack() },
            onSearchClick = { navController.navigate(AppRoutes.ArSearch) },
            destinationPill = {
                ArNavDestinationPill(
                    text = "명동성당 도착",
                    containerColor = ScanPangColors.StatusOpen,
                )
            },
        )

        ArNavSideVolumeCamera(
            onVolumeClick = { },
            onCameraClick = { },
        )

        ArArrivalBadgeStack(
            showCheckIcon = true,
            arrivalLabel = "도착했어요!",
            badgeColor = ScanPangColors.ArNavSuccessBadge90,
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
