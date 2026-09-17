package com.chandanshakya.fuellog.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.util.TripEstimate
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.util.estimateTrip

@Composable
fun TripCostDialog(
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    currency: String,
    recentMileage: Double?,
    lastRate: Double?,
    onDismiss: () -> Unit
) {
    var distanceText by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var estimate by remember { mutableStateOf<TripEstimate?>(null) }

    val distanceLabel = UnitConverter.getDistanceUnitLabel(distanceUnit)
    val volumeLabel = UnitConverter.getVolumeUnitLabel(volumeUnit)
    val efficiencyLabel = UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)
    val canEstimate = recentMileage != null && recentMileage > 0

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Trip Cost", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.padding(vertical = Dimens.spacingSm)) {
                if (!canEstimate) {
                    Text(
                        text = "Add at least two fill-ups so recent mileage can be estimated.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                } else {
                    Text(
                        text = "Using $efficiencyLabel recent average" +
                                (lastRate?.let { " · ${CurrencyFormatter.formatCurrency(it, currency)}/$volumeLabel last rate" }
                                    ?: " · no rate yet (cost shows $0)"),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(Dimens.spacingMd))
                    AppTextField(
                        value = distanceText,
                        onValueChange = {
                            distanceText = it
                            error = null
                            estimate = null
                        },
                        label = "Planned distance ($distanceLabel)",
                        error = error,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        decimalPlaces = 2
                    )
                    estimate?.let { t ->
                        Spacer(modifier = Modifier.height(Dimens.spacingMd))
                        Text(
                            text = "${"%.2f".format(t.energyNeeded)} $volumeLabel",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = CurrencyFormatter.formatCurrency(t.cost, currency),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = "${"%.0f".format(t.distance)} $distanceLabel at ${"%.2f".format(t.mileage)} $efficiencyLabel",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!canEstimate) {
                AppButtonOutlined(text = "Close", onClick = onDismiss, modifier = Modifier.padding(end = Dimens.spacingMd))
            } else {
                DialogButtonRow(
                    secondaryText = "Close",
                    primaryText = "Estimate",
                    onSecondary = onDismiss,
                    onPrimary = {
                        val d = distanceText.toDoubleOrNull()
                        if (d == null || d <= 0) {
                            error = "Enter a distance greater than 0"
                            estimate = null
                        } else {
                            error = null
                            estimate = estimateTrip(
                                distance = d,
                                recentMileage = recentMileage,
                                lastRate = lastRate,
                                distanceUnit = distanceUnit,
                                volumeUnit = volumeUnit
                            )
                        }
                    }
                )
            }
        }
    )
}
