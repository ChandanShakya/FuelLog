package com.chandanshakya.fuellog.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.Validation
import java.time.LocalDate

@Composable
fun AddFuelEntryDialog(
    vehicleId: Long,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (LocalDate, Double, Double, Double, Boolean, String?) -> Unit
) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var odometer by remember { mutableStateOf("") }
    var fuelVolume by remember { mutableStateOf("") }
    var fuelCost by remember { mutableStateOf("") }
    var isFullTank by remember { mutableStateOf(false) }
    var notes by remember { mutableStateOf("") }

    var odometerError by remember { mutableStateOf<String?>(null) }
    var fuelVolumeError by remember { mutableStateOf<String?>(null) }
    var fuelCostError by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Add Fuel Entry", style = MaterialTheme.typography.titleLarge)
        },
        text = {
            Column(
                modifier = Modifier.padding(vertical = Dimens.spacingSm)
            ) {
                Text(
                    text = "Date: ${date.toString()}",
                    style = MaterialTheme.typography.bodyMedium
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = odometer,
                    onValueChange = { newValue ->
                        odometer = newValue
                        odometerError = try {
                            val value = newValue.toDoubleOrNull() ?: 0.0
                            Validation.getOdometerError(value)
                        } catch (e: Exception) {
                            Validation.getOdometerError(0.0)
                        }
                    },
                    label = "Odometer (${when (distanceUnit) {
                        DistanceUnit.KM -> "km"
                        DistanceUnit.MILES -> "mi"
                    }})",
                    error = odometerError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = fuelVolume,
                    onValueChange = { newValue ->
                        fuelVolume = newValue
                        fuelVolumeError = try {
                            val value = newValue.toDoubleOrNull() ?: 0.0
                            Validation.getFuelVolumeError(value)
                        } catch (e: Exception) {
                            Validation.getFuelVolumeError(0.0)
                        }
                    },
                    label = "Fuel Volume (${when (volumeUnit) {
                        VolumeUnit.LITERS -> "L"
                        VolumeUnit.GALLONS -> "gal"
                    }})",
                    error = fuelVolumeError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = fuelCost,
                    onValueChange = { newValue ->
                        fuelCost = newValue
                        fuelCostError = try {
                            val value = newValue.toDoubleOrNull() ?: 0.0
                            Validation.getFuelCostError(value)
                        } catch (e: Exception) {
                            Validation.getFuelCostError(0.0)
                        }
                    },
                    label = "Fuel Cost ($currency)",
                    error = fuelCostError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Checkbox(
                        checked = isFullTank,
                        onCheckedChange = { isFullTank = it }
                    )
                    Spacer(modifier = Modifier.size(Dimens.spacingSm))
                    Text("Full Tank")
                }

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = "Notes (optional)",
                    supportingText = "Max 500 characters"
                )
            }
        },
        confirmButton = {
            AppButton(
                text = "Save",
                onClick = {
                    val odo = odometer.toDoubleOrNull() ?: 0.0
                    val vol = fuelVolume.toDoubleOrNull() ?: 0.0
                    val cost = fuelCost.toDoubleOrNull() ?: 0.0

                    if (odometerError == null && fuelVolumeError == null && fuelCostError == null) {
                        onSave(date, odo, vol, cost, isFullTank, if (notes.isBlank()) null else notes)
                    }
                },
                enabled = odometerError == null && fuelVolumeError == null && fuelCostError == null
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
