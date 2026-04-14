package com.scanpang.app.screens.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.scanpang.app.components.detail.DetailArPlaceOverlayScaffold
import com.scanpang.app.components.detail.PlaceDetailArTab
import com.scanpang.app.navigation.AppRoutes

/**
 * Figma: AR 장소 상세 — 층별 정보 (`197:1881`)
 */
@Composable
fun ArPlaceDetailFloorsScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    DetailArPlaceOverlayScaffold(
        selectedTab = PlaceDetailArTab.Floors,
        onHomeClick = { navController.popBackStack() },
        onSearchClick = {
            navController.navigate(AppRoutes.ArSearch) { launchSingleTop = true }
        },
        onTabBuilding = {
            navController.navigate(AppRoutes.DetailArPlaceBuilding) { launchSingleTop = true }
        },
        onTabFloors = { },
        onTabAi = {
            navController.navigate(AppRoutes.DetailArPlaceAi) { launchSingleTop = true }
        },
        onVolumeClick = { },
        onCameraClick = { },
        modifier = modifier,
    )
}
