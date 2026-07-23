package com.chandanshakya.fuellog.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
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
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.util.Validation
import java.time.LocalDate

enum class FuelInputMode {
    VOLUME_RATE,
    VOLUME_COST,
    RATE_COST
}

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
    var odometer by remember { mutableStateOf(entry?.odometer?.let { "%.2f".format(it) } ?: "") }
    var fuelVolume by remember { mutableStateOf(entry?.fuelVolume?.let { "%.2f".format(it) } ?: "") }
    var rate by remember {
        mutableStateOf(
            if (entry != null && entry.fuelVolume > 0) "%.2f".format(entry.fuelCost / entry.fuelVolume)
            else ""
        )
    }
    var totalCost by remember { mutableStateOf(entry?.fuelCost?.let { "%.2f".format(it) } ?: "") }
    var notes by remember { mutableStateOf(entry?.notes ?: "") }

    var inputMode by remember {
        mutableStateOf(
            when {
                entry != null && entry.fuelVolume > 0 && entry.fuelCost > 0 -> FuelInputMode.VOLUME_RATE
                else -> FuelInputMode.VOLUME_RATE
            }
        )
    }

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
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decimalPlaces = 2
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))
                Text("How do you want to enter fuel cost?", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Dimens.spacingSm))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = inputMode == FuelInputMode.VOLUME_RATE,
                        onClick = { inputMode = FuelInputMode.VOLUME_RATE }
                    )
                    Text("Volume + Rate")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = inputMode == FuelInputMode.VOLUME_COST,
                        onClick = { inputMode = FuelInputMode.VOLUME_COST }
                    )
                    Text("Volume + Cost")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = inputMode == FuelInputMode.RATE_COST,
                        onClick = { inputMode = FuelInputMode.RATE_COST }
                    )
                    Text("Rate + Cost")
                }

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                when (inputMode) {
                    FuelInputMode.VOLUME_RATE -> {
                        AppTextField(
                            value = fuelVolume,
                            onValueChange = { newValue ->
                                fuelVolume = newValue
                                fuelVolumeError = Validation.getFuelVolumeError(newValue.toDoubleOrNull() ?: 0.0)
                                val vol = newValue.toDoubleOrNull()
                                val r = rate.toDoubleOrNull()
                                if (vol != null && vol > 0 && r != null && r > 0) {
                                    totalCost = "%.2f".format(vol * r)
                                }
                            },
                            label = "Fuel Volume ($volumeLabel)",
                            error = fuelVolumeError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decimalPlaces = 2
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacingMd))
                        AppTextField(
                            value = rate,
                            onValueChange = { newValue ->
                                rate = newValue
                                val vol = fuelVolume.toDoubleOrNull()
                                val r = newValue.toDoubleOrNull()
                                if (vol != null && vol > 0 && r != null && r > 0) {
                                    totalCost = "%.2f".format(vol * r)
                                }
                            },
                            label = "Rate ($currency/$volumeLabel)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decimalPlaces = 2
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacingMd))
                        Text(
                            text = "Total Cost ($currency): ${if (totalCost.isNotEmpty()) totalCost else "--"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    FuelInputMode.VOLUME_COST -> {
                        AppTextField(
                            value = fuelVolume,
                            onValueChange = { newValue ->
                                fuelVolume = newValue
                                fuelVolumeError = Validation.getFuelVolumeError(newValue.toDoubleOrNull() ?: 0.0)
                                val vol = newValue.toDoubleOrNull()
                                val cost = totalCost.toDoubleOrNull()
                                if (vol != null && vol > 0 && cost != null && cost > 0) {
                                    rate = "%.2f".format(cost / vol)
                                }
                            },
                            label = "Fuel Volume ($volumeLabel)",
                            error = fuelVolumeError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decimalPlaces = 2
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacingMd))
                        AppTextField(
                            value = totalCost,
                            onValueChange = { newValue ->
                                totalCost = newValue
                                totalCostError = Validation.getFuelCostError(newValue.toDoubleOrNull() ?: 0.0)
                                val vol = fuelVolume.toDoubleOrNull()
                                val cost = newValue.toDoubleOrNull()
                                if (vol != null && vol > 0 && cost != null && cost > 0) {
                                    rate = "%.2f".format(cost / vol)
                                }
                            },
                            label = "Total Cost ($currency)",
                            error = totalCostError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decimalPlaces = 2
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacingMd))
                        Text(
                            text = "Rate ($currency/$volumeLabel): ${if (rate.isNotEmpty()) rate else "--"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    FuelInputMode.RATE_COST -> {
                        AppTextField(
                            value = rate,
                            onValueChange = { newValue ->
                                rate = newValue
                                val r = newValue.toDoubleOrNull()
                                val cost = totalCost.toDoubleOrNull()
                                if (r != null && r > 0 && cost != null && cost > 0) {
                                    fuelVolume = "%.2f".format(cost / r)
                                }
                            },
                            label = "Rate ($currency/$volumeLabel)",
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decimalPlaces = 2
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacingMd))
                        AppTextField(
                            value = totalCost,
                            onValueChange = { newValue ->
                                totalCost = newValue
                                totalCostError = Validation.getFuelCostError(newValue.toDoubleOrNull() ?: 0.0)
                                val r = rate.toDoubleOrNull()
                                val cost = newValue.toDoubleOrNull()
                                if (r != null && r > 0 && cost != null && cost > 0) {
                                    fuelVolume = "%.2f".format(cost / r)
                                }
                            },
                            label = "Total Cost ($currency)",
                            error = totalCostError,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            decimalPlaces = 2
                        )
                        Spacer(modifier = Modifier.height(Dimens.spacingMd))
                        Text(
                            text = "Fuel Volume ($volumeLabel): ${if (fuelVolume.isNotEmpty()) fuelVolume else "--"}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }

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
