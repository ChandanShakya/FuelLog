package com.chandanshakya.fuellog.ui.components

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
import androidx.compose.material3.Checkbox
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.util.Validation
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

enum class FuelInputMode {
    VOLUME_RATE,
    VOLUME_COST,
    RATE_COST
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddFuelEntryDialog(
    vehicleId: Long,
    entry: FuelEntry? = null,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    currency: String,
    existingPumps: List<FuelPump> = emptyList(),
    initialPumpName: String? = null,
    onEditPump: (FuelPump) -> Unit = {},
    onDeletePump: (Long) -> Unit = {},
    onDismiss: () -> Unit,
    onSave: (LocalDate, Double, Double, Double, String?, Boolean) -> Unit
) {
    var date by remember { mutableStateOf(entry?.date ?: LocalDate.now()) }
    var odometer by remember { mutableStateOf(entry?.odometer?.let { "%.2f".format(it) } ?: "") }
    var isFullTank by remember { mutableStateOf(entry?.isFullTank ?: false) }
    var fuelVolume by remember { mutableStateOf(entry?.fuelVolume?.let { "%.2f".format(it) } ?: "") }
    var rate by remember {
        mutableStateOf(
            if (entry != null && entry.fuelVolume > 0) "%.2f".format(entry.fuelCost / entry.fuelVolume)
            else ""
        )
    }
    var totalCost by remember { mutableStateOf(entry?.fuelCost?.let { "%.2f".format(it) } ?: "") }

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

    var showDatePicker by remember { mutableStateOf(false) }

    var pumpText by remember {
        mutableStateOf(
            initialPumpName
                ?: entry?.fuelPumpId?.let { id -> existingPumps.find { it.id == id }?.name }
                ?: ""
        )
    }

    // Resolve pump name once existingPumps loads (handles timing with StateFlow)
    LaunchedEffect(existingPumps, entry) {
        if (pumpText.isBlank() && entry?.fuelPumpId != null && existingPumps.isNotEmpty()) {
            pumpText = existingPumps.find { it.id == entry.fuelPumpId }?.name ?: ""
        }
    }
    var pumpDropdownExpanded by remember { mutableStateOf(false) }

    val filteredPumps = remember(pumpText, existingPumps) {
        if (pumpText.isBlank()) existingPumps
        else existingPumps.filter { it.name.contains(pumpText, ignoreCase = true) }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (entry != null) "Edit Fuel Entry" else "Add Fuel Entry", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.padding(vertical = Dimens.spacingSm)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "Date: $date", style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(painter = painterResource(R.drawable.ic_calendar), contentDescription = "Pick date")
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = odometer,
                    onValueChange = { odometer = it; odometerError = Validation.getOdometerError(it.toDoubleOrNull() ?: 0.0) },
                    label = "Odometer ($distanceLabel)",
                    error = odometerError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decimalPlaces = 2
                )

                Spacer(modifier = Modifier.height(Dimens.spacingSm))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(checked = isFullTank, onCheckedChange = { isFullTank = it })
                    Text(text = "Full tank?", style = MaterialTheme.typography.bodyMedium)
                }

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                ExposedDropdownMenuBox(
                    expanded = pumpDropdownExpanded,
                    onExpandedChange = { pumpDropdownExpanded = it }
                ) {
                    OutlinedTextField(
                        value = pumpText,
                        onValueChange = {
                            pumpText = it
                            pumpDropdownExpanded = true
                        },
                        label = { Text("Fuel Pump (optional)") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(MenuAnchorType.PrimaryNotEditable),
                        singleLine = true,
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = pumpDropdownExpanded) }
                    )
                    if (filteredPumps.isNotEmpty() && pumpDropdownExpanded) {
                        ExposedDropdownMenu(
                            expanded = pumpDropdownExpanded,
                            onDismissRequest = { pumpDropdownExpanded = false }
                        ) {
                            filteredPumps.forEach { pump ->
                                DropdownMenuItem(
                                    text = { Text(pump.name) },
                                    onClick = {
                                        pumpText = pump.name
                                        pumpDropdownExpanded = false
                                    },
                                    trailingIcon = {
                                        Row {
                                            IconButton(onClick = { onEditPump(pump) }, modifier = Modifier.size(32.dp)) {
                                                Icon(painter = painterResource(R.drawable.ic_edit), contentDescription = "Edit pump", modifier = Modifier.size(16.dp))
                                            }
                                            IconButton(onClick = { onDeletePump(pump.id) }, modifier = Modifier.size(32.dp)) {
                                                Icon(painter = painterResource(R.drawable.ic_delete), contentDescription = "Delete pump", modifier = Modifier.size(16.dp), tint = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                    FuelInputMode.entries.forEachIndexed { index, mode ->
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = index, count = FuelInputMode.entries.size),
                            onClick = { inputMode = mode },
                            selected = inputMode == mode,
                            label = {
                                Text(
                                    when (mode) {
                                        FuelInputMode.VOLUME_RATE -> "Vol+Rate"
                                        FuelInputMode.VOLUME_COST -> "Vol+Cost"
                                        FuelInputMode.RATE_COST -> "Rate+Cost"
                                    }
                                )
                            }
                        )
                    }
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
                    val r = rate.toDoubleOrNull() ?: 0.0
                    val cost = totalCost.toDoubleOrNull() ?: 0.0
                    val vol = fuelVolume.toDoubleOrNull() ?: 0.0
                    val (finalVol, finalCost) = when (inputMode) {
                        FuelInputMode.VOLUME_RATE -> vol to if (vol > 0 && r > 0) vol * r else cost
                        FuelInputMode.VOLUME_COST -> vol to cost
                        FuelInputMode.RATE_COST -> (if (r > 0) cost / r else vol) to cost
                    }
                    if (odometerError == null && fuelVolumeError == null && totalCostError == null) {
                        val pumpName = pumpText.trim().ifBlank { null }
                        onSave(date, odo, finalVol, finalCost, pumpName, isFullTank)
                    }
                },
                enabled = odometerError == null && fuelVolumeError == null && totalCostError == null
            )
        },
        dismissButton = {
            AppButtonOutlined(text = "Cancel", onClick = onDismiss)
        }
    )

    if (showDatePicker) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = date.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        date = Instant.ofEpochMilli(millis).atZone(ZoneId.systemDefault()).toLocalDate()
                    }
                    showDatePicker = false
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}
