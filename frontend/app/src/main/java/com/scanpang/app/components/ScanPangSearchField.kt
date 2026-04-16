package com.scanpang.app.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import com.scanpang.app.ui.theme.ScanPangColors
import com.scanpang.app.ui.theme.ScanPangDimens
import com.scanpang.app.ui.theme.ScanPangShapes
import com.scanpang.app.ui.theme.ScanPangSpacing
import com.scanpang.app.ui.theme.ScanPangType

@Composable
fun ScanPangSearchFieldPlaceholder(
    placeholder: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ScanPangDimens.searchBarHeightDefault)
            .clip(ScanPangShapes.radius14)
            .background(ScanPangColors.Background)
            .clickable(onClick = onClick)
            .padding(horizontal = ScanPangSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.rowGap10),
    ) {
        Icon(
            imageVector = Icons.Rounded.Search,
            contentDescription = null,
            modifier = Modifier.size(ScanPangDimens.icon18),
            tint = ScanPangColors.OnSurfacePlaceholder,
        )
        Text(
            text = placeholder,
            style = ScanPangType.searchPlaceholderRegular,
            color = ScanPangColors.OnSurfacePlaceholder,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun ScanPangSearchFieldFilled(
    query: String,
    onSearchBarClick: () -> Unit,
    onClearClick: () -> Unit,
    modifier: Modifier = Modifier,
    hintWhenBlank: String? = null,
) {
    val showHint = query.isBlank() && !hintWhenBlank.isNullOrBlank()
    val labelText = if (showHint) hintWhenBlank else query
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(ScanPangDimens.searchBarHeightActive)
            .clip(ScanPangShapes.radius12)
            .background(ScanPangColors.Background)
            .padding(horizontal = ScanPangDimens.searchBarInnerHorizontal),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clickable(onClick = onSearchBarClick),
            horizontalArrangement = Arrangement.spacedBy(ScanPangSpacing.rowGap10),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = null,
                modifier = Modifier.size(ScanPangDimens.icon18),
                tint = if (showHint) ScanPangColors.OnSurfacePlaceholder else ScanPangColors.Primary,
            )
            Text(
                text = labelText,
                style = if (showHint) ScanPangType.searchPlaceholderRegular else ScanPangType.body15Medium,
                color = if (showHint) ScanPangColors.OnSurfacePlaceholder else ScanPangColors.OnSurfaceStrong,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!showHint && query.isNotEmpty()) {
            Icon(
                imageVector = Icons.Rounded.Close,
                contentDescription = "지우기",
                modifier = Modifier
                    .size(ScanPangDimens.icon18)
                    .clickable(onClick = onClearClick),
                tint = ScanPangColors.OnSurfacePlaceholder,
            )
        }
    }
}
