package com.scanpang.app.screens.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.scanpang.app.components.detail.DetailArStoreOverlayScaffold
import com.scanpang.app.navigation.AppRoutes

/**
 * Figma: 매장 상세 AR 오버레이 (`197:2548`)
 */
@Composable
fun ArStoreDetailOverlayScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    DetailArStoreOverlayScaffold(
        onHomeClick = { navController.popBackStack() },
        onSearchClick = {
            navController.navigate(AppRoutes.ArSearch) { launchSingleTop = true }
        },
        onVolumeClick = { },
        onCameraClick = { },
        modifier = modifier,
    )
}
