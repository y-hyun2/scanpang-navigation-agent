package com.scanpang.app.screens.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.scanpang.app.components.detail.DetailArPlaceOverlayScaffold
import com.scanpang.app.components.detail.PlaceDetailArTab
import com.scanpang.app.navigation.AppRoutes

/**
 * Figma: AR 장소 상세 — AI 가이드 (`197:2047`)
 */
@Composable
fun ArPlaceDetailAiGuideScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    DetailArPlaceOverlayScaffold(
        selectedTab = PlaceDetailArTab.AiGuide,
        onHomeClick = { navController.popBackStack() },
        onSearchClick = {
            navController.navigate(AppRoutes.ArSearch) { launchSingleTop = true }
        },
        onTabBuilding = {
            navController.navigate(AppRoutes.DetailArPlaceBuilding) { launchSingleTop = true }
        },
        onTabFloors = {
            navController.navigate(AppRoutes.DetailArPlaceFloors) { launchSingleTop = true }
        },
        onTabAi = { },
        onVolumeClick = { },
        onCameraClick = { },
        modifier = modifier,
    )
}
