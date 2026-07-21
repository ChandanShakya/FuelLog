package com.chandanshakya.fuellog.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.screens.getVehicleIcon
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.Validation

private val VEHICLE_TYPES = listOf("car", "bus", "bike", "scooter", "truck")

@Composable
fun AddVehicleDialog(
    vehicle: Vehicle? = null,
    defaultCurrency: String,
    defaultDistanceUnit: DistanceUnit,
    defaultVolumeUnit: VolumeUnit,
    onDismiss: () -> Unit,
    onSave: (name: String, vehicleType: String, currency: String, distanceUnit: DistanceUnit, volumeUnit: VolumeUnit) -> Unit
) {
    var name by remember { mutableStateOf(vehicle?.name ?: "") }
    var vehicleType by remember { mutableStateOf(vehicle?.vehicleType ?: "car") }
    var currency by remember { mutableStateOf(vehicle?.defaultCurrency ?: defaultCurrency) }
    var distanceUnit by remember { mutableStateOf(vehicle?.distanceUnit ?: defaultDistanceUnit) }
    var volumeUnit by remember { mutableStateOf(vehicle?.volumeUnit ?: defaultVolumeUnit) }
    var nameError by remember { mutableStateOf<String?>(null) }
    var currencyError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle != null) "Edit Vehicle" else "Add Vehicle", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.padding(vertical = Dimens.spacingSm)) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it; nameError = Validation.getVehicleNameError(it) },
                    label = "Vehicle Name",
                    error = nameError,
                    supportingText = "e.g. My Car, Honda Activa"
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))
                Text("Vehicle Type", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Dimens.spacingSm))

                Row(modifier = Modifier.fillMaxWidth()) {
                    VEHICLE_TYPES.forEach { type ->
                        val isSelected = type == vehicleType
                        Column(
                            modifier = Modifier
                                .weight(1f)
                                .clickable { vehicleType = type }
                                .padding(4.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Icon(
                                imageVector = getVehicleIcon(type),
                                contentDescription = type,
                                modifier = Modifier.size(28.dp),
                                tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                text = type.replaceFirstChar { it.uppercase() },
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = currency,
                    onValueChange = { currency = it.uppercase(); currencyError = Validation.getCurrencyCodeError(it) },
                    label = "Currency",
                    error = currencyError,
                    supportingText = "ISO 4217 (e.g. USD, EUR, INR)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))
                Text("Distance Unit", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(selected = distanceUnit == DistanceUnit.KM, onClick = { distanceUnit = DistanceUnit.KM })
                    Text("km", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = distanceUnit == DistanceUnit.MILES, onClick = { distanceUnit = DistanceUnit.MILES })
                    Text("miles", modifier = Modifier.align(Alignment.CenterVertically))
                }

                Spacer(modifier = Modifier.height(Dimens.spacingMd))
                Text("Volume Unit", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                Row(modifier = Modifier.fillMaxWidth()) {
                    RadioButton(selected = volumeUnit == VolumeUnit.LITERS, onClick = { volumeUnit = VolumeUnit.LITERS })
                    Text("Liters", modifier = Modifier.align(Alignment.CenterVertically))
                    Spacer(modifier = Modifier.width(16.dp))
                    RadioButton(selected = volumeUnit == VolumeUnit.GALLONS, onClick = { volumeUnit = VolumeUnit.GALLONS })
                    Text("Gallons", modifier = Modifier.align(Alignment.CenterVertically))
                }
            }
        },
        confirmButton = {
            AppButton(
                text = "Save",
                onClick = {
                    if (nameError == null && currencyError == null) {
                        onSave(name, vehicleType, currency, distanceUnit, volumeUnit)
                    }
                },
                enabled = nameError == null && currencyError == null
            )
        },
        dismissButton = {
            AppButtonOutlined(text = "Cancel", onClick = onDismiss)
        }
    )
}
