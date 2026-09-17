package com.chandanshakya.fuellog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.chandanshakya.fuellog.ui.theme.Dimens

/**
 * Standard dialog action row: secondary (left) and primary (right) on one line.
 */
@Composable
fun DialogButtonRow(
    secondaryText: String,
    primaryText: String,
    onSecondary: () -> Unit,
    onPrimary: () -> Unit,
    primaryEnabled: Boolean = true,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
    ) {
        AppButtonOutlined(
            text = secondaryText,
            onClick = onSecondary,
            modifier = Modifier.weight(1f)
        )
        AppButton(
            text = primaryText,
            onClick = onPrimary,
            enabled = primaryEnabled,
            modifier = Modifier.weight(1f)
        )
    }
}
