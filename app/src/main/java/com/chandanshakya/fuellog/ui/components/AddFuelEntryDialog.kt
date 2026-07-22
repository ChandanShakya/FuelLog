package com.chandanshakya.fuellog.ui.components

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
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.util.Validation
import java.time.LocalDate

@Composable
fun AddFuelEntryDialog(
    vehicleId: Long,
    entry: FuelEntry? = null,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (LocalDate, Double, Double, Double, String?) -> Unit
) {
    var date by remember { mutableStateOf(entry?.date ?: LocalDate.now()) }
    var odometer by remember { mutableStateOf(entry?.odometer?.toString() ?: "") }
    var fuelVolume by remember { mutableStateOf(entry?.fuelVolume?.toString() ?: "") }
    var rate by remember {
        mutableStateOf(
            if (entry != null && entry.fuelVolume > 0) (entry.fuelCost / entry.fuelVolume).toString()
            else ""
        )
    }
    var totalCost by remember { mutableStateOf(entry?.fuelCost?.toString() ?: "") }
    var notes by remember { mutableStateOf(entry?.notes ?: "") }

    var odometerError by remember { mutableStateOf<String?>(null) }
    var fuelVolumeError by remember { mutableStateOf<String?>(null) }
    var totalCostError by remember { mutableStateOf<String?>(null) }

    val volumeLabel = UnitConverter.getVolumeUnitLabel(volumeUnit)
    val distanceLabel = UnitConverter.getDistanceUnitLabel(distanceUnit)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry != null) "Edit Fuel Entry" else "Add Fuel Entry", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.padding(vertical = Dimens.spacingSm)) {
                Text(text = "Date: ${date}", style = MaterialTheme.typography.bodyMedium)

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = odometer,
                    onValueChange = { odometer = it; odometerError = Validation.getOdometerError(it.toDoubleOrNull() ?: 0.0) },
                    label = "Odometer ($distanceLabel)",
                    error = odometerError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = fuelVolume,
                    onValueChange = { newValue ->
                        fuelVolume = newValue
                        fuelVolumeError = Validation.getFuelVolumeError(newValue.toDoubleOrNull() ?: 0.0)
                        val vol = newValue.toDoubleOrNull()
                        val r = rate.toDoubleOrNull()
                        if (vol != null && vol > 0 && r != null && r > 0) {
                            val cost = "%.2f".format(vol * r)
                            totalCost = cost
                            totalCostError = Validation.getFuelCostError(cost.toDoubleOrNull() ?: 0.0)
                        }
                    },
                    label = "Fuel Volume ($volumeLabel)",
                    error = fuelVolumeError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = rate,
                    onValueChange = { newValue ->
                        rate = newValue
                        val vol = fuelVolume.toDoubleOrNull()
                        val r = newValue.toDoubleOrNull()
                        val cost = totalCost.toDoubleOrNull()
                        if (r != null && r > 0) {
                            if (vol != null && vol > 0 && (cost == null || cost <= 0)) {
                                val calculatedCost = "%.2f".format(vol * r)
                                totalCost = calculatedCost
                                totalCostError = Validation.getFuelCostError(calculatedCost.toDoubleOrNull() ?: 0.0)
                            } else if (cost != null && cost > 0 && (vol == null || vol <= 0)) {
                                fuelVolume = "%.2f".format(cost / r)
                            }
                        }
                    },
                    label = "Rate ($currency/$volumeLabel)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = totalCost,
                    onValueChange = { newValue ->
                        totalCost = newValue
                        totalCostError = Validation.getFuelCostError(newValue.toDoubleOrNull() ?: 0.0)
                        val vol = fuelVolume.toDoubleOrNull()
                        val r = rate.toDoubleOrNull()
                        val cost = newValue.toDoubleOrNull()
                        if (cost != null && cost > 0) {
                            if (vol != null && vol > 0 && (r == null || r <= 0)) {
                                rate = "%.2f".format(cost / vol)
                            } else if (r != null && r > 0 && (vol == null || vol <= 0)) {
                                fuelVolume = "%.2f".format(cost / r)
                            }
                        }
                    },
                    label = "Total Cost ($currency)",
                    error = totalCostError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

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
                    val cost = totalCost.toDoubleOrNull() ?: 0.0
                    if (odometerError == null && fuelVolumeError == null && totalCostError == null) {
                        onSave(date, odo, vol, cost, if (notes.isBlank()) null else notes)
                    }
                },
                enabled = odometerError == null && fuelVolumeError == null && totalCostError == null
            )
        },
        dismissButton = {
            AppButtonOutlined(text = "Cancel", onClick = onDismiss)
        }
    )
}
