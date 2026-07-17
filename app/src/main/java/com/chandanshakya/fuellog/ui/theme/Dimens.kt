package com.chandanshakya.fuellog.ui.theme

import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object Dimens {
    val cornerRadius = 12.dp
    val cornerRadiusSmall = 8.dp
    val cornerRadiusLarge = 16.dp

    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 16.dp
    val spacingLg = 24.dp
    val spacingXl = 32.dp
    val spacingXxl = 48.dp

    @Composable
    fun cardElevation() = CardDefaults.cardElevation(defaultElevation = 1.dp)

    @Composable
    fun cardElevationHover() = CardDefaults.cardElevation(defaultElevation = 2.dp)

    val borderWidth = 1.dp

    val iconSmall = 16.dp
    val iconMedium = 24.dp
    val iconLarge = 32.dp

    val buttonHeight = 48.dp
    val buttonHeightSmall = 40.dp

    val inputHeight = 56.dp

    val bottomSheetPeekHeight = 64.dp
    val bottomSheetExpandedHeight = 400.dp

    val chartHeight = 200.dp
    val chartPadding = 16.dp

    val listItemHeight = 72.dp
    val listItemPadding = 16.dp

    val dividerHeight = 1.dp
}
