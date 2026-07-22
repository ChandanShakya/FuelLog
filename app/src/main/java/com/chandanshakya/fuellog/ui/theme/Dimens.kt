package com.chandanshakya.fuellog.ui.theme

import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

object Dimens {
    val spacingXs = 4.dp
    val spacingSm = 8.dp
    val spacingMd = 16.dp
    val spacingLg = 24.dp
    val spacingXl = 32.dp

    @Composable
    fun cardElevation() = CardDefaults.cardElevation(defaultElevation = 1.dp)

    val iconMedium = 24.dp
    val iconLarge = 32.dp
    val chartHeight = 200.dp
}
