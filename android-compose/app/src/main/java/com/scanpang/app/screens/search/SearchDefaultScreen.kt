package com.scanpang.app.screens.search

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CurrencyExchange
import androidx.compose.material.icons.filled.LocalCafe
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Medication
import androidx.compose.material.icons.filled.Mosque
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import android.net.Uri
import androidx.navigation.NavHostController
import com.scanpang.app.navigation.SearchRoutes
import com.scanpang.app.ui.theme.ScanPangRadius
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangThemeAccessor
import com.scanpang.app.ui.theme.ScanPangTypeScale

private val Radius14 = ScanPangRadius.Lg - 2.dp

private val CategoryRows: List<List<Pair<ImageVector, String>>> = listOf(
    listOf(
        Icons.Filled.Restaurant to "할랄 식당",
        Icons.Filled.Mosque to "기도실",
        Icons.Filled.LocalCafe to "카페",
        Icons.Filled.ShoppingBag to "쇼핑",
    ),
    listOf(
        Icons.Filled.LocalHospital to "병원",
        Icons.Filled.Medication to "약국",
        Icons.Filled.CurrencyExchange to "환전소",
        Icons.Filled.PushPin to "관광지",
    ),
)

@Composable
fun SearchDefaultScreen(
    navController: NavHostController,
    modifier: Modifier = Modifier,
) {
    val c = ScanPangThemeAccessor.colors
    var query by remember { mutableStateOf("") }
    val scroll = rememberScrollState()

    Column(
        modifier
            .fillMaxSize()
            .background(c.background)
            .verticalScroll(scroll)
            .padding(ScanPangSpacing.S5),
        verticalArrangement = Arrangement.spacedBy(ScanPangSpacing.S6),
    ) {
        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            placeholder = { Text("장소, 카테고리 검색", color = c.textPlaceholder) },
            leadingIcon = {
                Icon(Icons.Filled.Search, contentDescription = null, tint = c.iconMuted)
            },
            singleLine = true,
            shape = RoundedCornerShape(Radius14),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = c.surfaceSubtle,
                unfocusedContainerColor = c.surfaceSubtle,
                unfocusedBorderColor = c.border.copy(alpha = 0f),
                focusedBorderColor = c.primary,
            ),
        )
        Text(
            "카테고리",
            fontSize = ScanPangTypeScale.Md,
            fontWeight = ScanPangTypeScale.W700,
            color = c.textPrimary,
        )
        CategoryRows.forEach { row ->
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.S3),
            ) {
                row.forEach { (icon, label) ->
                    Column(
                        Modifier
                            .weight(1f)
                            .height(120.dp)
                            .background(c.surfaceSubtle, RoundedCornerShape(Radius14))
                            .clickable {
                                navController.navigate(
                                    "${SearchRoutes.RESULTS}/${Uri.encode(label)}",
                                )
                            }
                            .padding(ScanPangSpacing.S3),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center,
                    ) {
                        Icon(icon, contentDescription = label, tint = c.primary)
                        Text(
                            label,
                            fontSize = ScanPangTypeScale.Sm,
                            color = c.textPrimary,
                            modifier = Modifier.padding(top = ScanPangSpacing.S2),
                        )
                    }
                }
            }
        }
    }
}
