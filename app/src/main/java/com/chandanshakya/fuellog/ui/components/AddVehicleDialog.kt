package com.chandanshakya.fuellog.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
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
import com.chandanshakya.fuellog.util.Validation

@Composable
fun AddVehicleDialog(
    defaultCurrency: String,
    defaultDistanceUnit: DistanceUnit,
    defaultVolumeUnit: VolumeUnit,
    onDismiss: () -> Unit,
    onSave: (String, String, DistanceUnit, VolumeUnit) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var currency by remember { mutableStateOf(defaultCurrency) }
    var distanceUnit by remember { mutableStateOf(defaultDistanceUnit) }
    var volumeUnit by remember { mutableStateOf(defaultVolumeUnit) }

    var nameError by remember { mutableStateOf<String?>(null) }
    var currencyError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Vehicle", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = Dimens.spacingSm)
            ) {
                AppTextField(
                    value = name,
                    onValueChange = { newValue ->
                        name = newValue
                        nameError = Validation.getVehicleNameError(newValue)
                    },
                    label = "Vehicle Name",
                    error = nameError,
                    supportingText = "Enter a name for your vehicle"
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = currency,
                    onValueChange = { newValue ->
                        currency = newValue.uppercase()
                        currencyError = Validation.getCurrencyCodeError(newValue)
                    },
                    label = "Currency",
                    error = currencyError,
                    supportingText = "ISO 4217 code (e.g., USD, EUR, INR)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                Text(
                    text = "Distance Unit",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(Dimens.spacingSm))

                UnitSelector(
                    selectedUnit = distanceUnit,
                    units = DistanceUnit.entries.toList(),
                    onUnitSelected = { distanceUnit = it },
                    labelProvider = { unit ->
                        when (unit) {
                            DistanceUnit.KM -> "Kilometers (km)"
                            DistanceUnit.MILES -> "Miles (mi)"
                        }
                    }
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                Text(
                    text = "Volume Unit",
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.height(Dimens.spacingSm))

                UnitSelector(
                    selectedUnit = volumeUnit,
                    units = VolumeUnit.entries.toList(),
                    onUnitSelected = { volumeUnit = it },
                    labelProvider = { unit ->
                        when (unit) {
                            VolumeUnit.LITERS -> "Liters (L)"
                            VolumeUnit.GALLONS -> "Gallons (gal)"
                        }
                    }
                )
            }
        },
        confirmButton = {
            AppButton(
                text = "Save",
                onClick = {
                    if (nameError == null && currencyError == null) {
                        onSave(name, currency, distanceUnit, volumeUnit)
                    }
                },
                enabled = nameError == null && currencyError == null
            )
        },
        dismissButton = {
            AppButtonOutlined(
                text = "Cancel",
                onClick = onDismiss
            )
        }
    )
}

@Composable
fun <T> UnitSelector(
    selectedUnit: T,
    units: List<T>,
    onUnitSelected: (T) -> Unit,
    labelProvider: @Composable (T) -> String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        for (unit in units) {
            val isSelected = unit == selectedUnit
            val backgroundColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }

            val textColor = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            }

            Text(
                text = labelProvider(unit),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Dimens.spacingMd)
                    .background(
                        color = backgroundColor,
                        shape = MaterialTheme.shapes.small
                    )
                    .padding(Dimens.spacingMd)
                    .clickable { onUnitSelected(unit) },
                color = textColor,
                style = MaterialTheme.typography.bodyMedium
            )

            if (unit != units.last()) {
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
            }
        }
    }
}
