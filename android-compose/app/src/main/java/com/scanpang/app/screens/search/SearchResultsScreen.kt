package com.scanpang.app.screens.search

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import com.scanpang.app.screens.common.PlaceholderScreen

@Composable
fun SearchResultsScreen(
    query: String,
    navController: NavHostController,
) {
    PlaceholderScreen(
        title = "검색 결과",
        subtitle = "검색어: $query\n(RN SearchResultsScreen 대응)",
        onBack = { navController.popBackStack() },
    )
}
