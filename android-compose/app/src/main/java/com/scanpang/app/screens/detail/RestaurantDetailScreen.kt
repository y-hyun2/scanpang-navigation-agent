package com.scanpang.app.screens.detail

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.scanpang.app.components.ScreenBackButton
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangThemeAccessor

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RestaurantDetailScreen(
    title: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val c = ScanPangThemeAccessor.colors
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = c.background,
        topBar = {
            TopAppBar(
                title = { Text(title.ifBlank { "식당 상세" }, color = c.textPrimary) },
                navigationIcon = { ScreenBackButton(onClick = onBack) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = c.background,
                    navigationIconContentColor = c.iconDefault,
                    titleContentColor = c.textPrimary,
                ),
            )
        },
    ) { inner ->
        Column(Modifier.padding(inner).padding(ScanPangSpacing.S5)) {
            Text(
                "할랄 식당 상세 (RN RestaurantDetail 대응)",
                style = MaterialTheme.typography.bodyLarge,
                color = c.textSecondary,
            )
        }
    }
}
