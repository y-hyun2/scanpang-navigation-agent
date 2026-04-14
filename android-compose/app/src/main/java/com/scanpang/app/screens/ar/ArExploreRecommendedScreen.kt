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
import com.scanpang.app.components.ar.ArCameraBackdrop
import com.scanpang.app.components.ar.ArChatBottomSection
import com.scanpang.app.components.ar.ArPoiPinsLayer
import com.scanpang.app.components.ar.ArRecommendHalalTag
import com.scanpang.app.components.ar.ArSideButtonsLayer
import com.scanpang.app.components.ar.ArStatusPillNeutral
import com.scanpang.app.components.ar.ArTopGradientBar
import com.scanpang.app.navigation.AppRoutes

@Composable
fun ArExploreRecommendedScreen(
    navController: NavController,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        ArCameraBackdrop(showFreezeTint = false, modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .align(Alignment.TopStart)
                .fillMaxWidth(),
        ) {
            ArTopGradientBar(
                modifier = Modifier.fillMaxWidth(),
                onHomeClick = { navController.popBackStack() },
                onSearchClick = { navController.navigate(AppRoutes.ArSearch) },
                centerContent = {
                    ArStatusPillNeutral(text = "탐색 중")
                },
            )
        }

        Box(Modifier.fillMaxSize()) {
            ArPoiPinsLayer()
            ArSideButtonsLayer(
                onVolumeClick = { },
                onCameraClick = { },
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .navigationBarsPadding(),
        ) {
            ArChatBottomSection(
                userMessage = "손님, 오늘은 어떤 장소를 찾고 계신가요?",
                agentMessage = "이 주변에는 할랄 인증 음식점이 3곳 있어요. 지도에 표시해 드릴까요?",
                inputPlaceholder = "무엇이든 물어보삼",
                modifier = Modifier.fillMaxWidth(),
                agentTag = {
                    ArRecommendHalalTag(text = "# 할랄")
                },
            )
        }
    }
}
