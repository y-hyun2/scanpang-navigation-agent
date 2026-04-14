package com.scanpang.app.screens.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.scanpang.app.components.detail.DetailArPlaceOverlayScaffold
import com.scanpang.app.components.detail.PlaceDetailArTab
import com.scanpang.app.navigation.AppRoutes

/**
 * Figma: AR 장소 상세 — 건물 정보 (`197:1595`)
 */
@Composable
fun ArPlaceDetailBuildingScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    DetailArPlaceOverlayScaffold(
        selectedTab = PlaceDetailArTab.Building,
        onHomeClick = { navController.popBackStack() },
        onSearchClick = {
            navController.navigate(AppRoutes.ArSearch) { launchSingleTop = true }
        },
        onTabBuilding = { },
        onTabFloors = {
            navController.navigate(AppRoutes.DetailArPlaceFloors) { launchSingleTop = true }
        },
        onTabAi = {
            navController.navigate(AppRoutes.DetailArPlaceAi) { launchSingleTop = true }
        },
        onVolumeClick = { },
        onCameraClick = { },
        modifier = modifier,
    )
}
