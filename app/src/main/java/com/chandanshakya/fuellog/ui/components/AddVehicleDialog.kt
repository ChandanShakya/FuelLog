package com.chandanshakya.fuellog.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
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
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelType
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VehicleType
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.FuelLabels
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.util.Validation

data class VehicleFormResult(
    val name: String,
    val vehicleType: VehicleType,
    val fuelType: FuelType,
    val distanceUnit: DistanceUnit,
    val volumeUnit: VolumeUnit,
    val tankCapacity: Double?,
    val reserveAmount: Double?
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddVehicleDialog(
    vehicle: Vehicle? = null,
    defaultDistanceUnit: DistanceUnit,
    defaultVolumeUnit: VolumeUnit,
    onDismiss: () -> Unit,
    onSave: (VehicleFormResult) -> Unit
) {
    var name by remember { mutableStateOf(vehicle?.name ?: "") }
    var vehicleType by remember { mutableStateOf(vehicle?.vehicleType ?: VehicleType.CAR) }
    var fuelType by remember { mutableStateOf(vehicle?.fuelType ?: FuelType.PETROL) }
    var distanceUnit by remember { mutableStateOf(vehicle?.distanceUnit ?: defaultDistanceUnit) }
    var volumeUnit by remember {
        mutableStateOf(
            vehicle?.volumeUnit
                ?: if ((vehicle?.fuelType ?: FuelType.PETROL).isElectric) VolumeUnit.KWH
                else defaultVolumeUnit
        )
    }
    var tankCapacityText by remember { mutableStateOf(vehicle?.tankCapacity?.let { "%.2f".format(it) } ?: "") }
    var reserveText by remember { mutableStateOf(vehicle?.reserveAmount?.let { "%.2f".format(it) } ?: "") }
    var nameError by remember { mutableStateOf<String?>(null) }

    val isEv = fuelType.isElectric
    val previousVolumeUnit = remember { mutableStateOf(volumeUnit) }
    LaunchedEffect(volumeUnit) {
        val from = previousVolumeUnit.value
        if (from != volumeUnit && !from.isEnergy && !volumeUnit.isEnergy) {
            val current = tankCapacityText.toDoubleOrNull()
            if (current != null && current > 0) {
                tankCapacityText = "%.2f".format(UnitConverter.convertVolume(current, from, volumeUnit))
            }
            val reserve = reserveText.toDoubleOrNull()
            if (reserve != null && reserve > 0) {
                reserveText = "%.2f".format(UnitConverter.convertVolume(reserve, from, volumeUnit))
            }
            previousVolumeUnit.value = volumeUnit
        } else {
            previousVolumeUnit.value = volumeUnit
        }
    }

    // EV forces kWh
    LaunchedEffect(fuelType) {
        if (fuelType.isElectric) volumeUnit = VolumeUnit.KWH
        else if (volumeUnit == VolumeUnit.KWH) volumeUnit = defaultVolumeUnit.takeIf { !it.isEnergy }
            ?: VolumeUnit.LITERS
    }

    val volumeLabel = UnitConverter.getVolumeUnitLabel(volumeUnit)
    val capacityTitle = FuelLabels.capacityLabel(isEv)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (vehicle != null) "Edit Vehicle" else "Add Vehicle", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(
                modifier = Modifier
                    .verticalScroll(rememberScrollState())
                    .padding(vertical = Dimens.spacingSm)
            ) {
                AppTextField(
                    value = name,
                    onValueChange = { name = it; nameError = Validation.getVehicleNameError(it) },
                    label = "Vehicle Name",
                    error = nameError,
                    supportingText = "e.g. My Car, Nexon EV"
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))
                Text("Vehicle Type", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    VehicleType.entries.chunked(3).forEach { row ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            row.forEach { type ->
                                val isSelected = vehicleType == type
                                OutlinedButton(
                                    onClick = { vehicleType = type },
                                    modifier = Modifier.weight(1f).height(64.dp),
                                    shape = RoundedCornerShape(8.dp),
                                    border = BorderStroke(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.outline
                                    )
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Icon(
                                            painter = painterResource(type.iconRes),
                                            contentDescription = type.label,
                                            modifier = Modifier.size(24.dp),
                                            tint = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            text = type.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) MaterialTheme.colorScheme.primary
                                            else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(Dimens.spacingMd))
                Text("Fuel / Energy", style = MaterialTheme.typography.bodyMedium)
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FuelType.entries.forEach { type ->
                        FilterChip(
                            selected = fuelType == type,
                            onClick = { fuelType = type },
                            label = { Text(type.label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }

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

                if (!isEv) {
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
                } else {
                    Spacer(modifier = Modifier.height(Dimens.spacingMd))
                    Text("Volume unit: kWh (locked for electric)", style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(Dimens.spacingMd))
                AppTextField(
                    value = tankCapacityText,
                    onValueChange = { tankCapacityText = it },
                    label = "$capacityTitle ($volumeLabel, optional)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decimalPlaces = 2,
                    supportingText = if (isEv) "Usable battery size for charge prediction" else "Used for next fill-up prediction"
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))
                AppTextField(
                    value = reserveText,
                    onValueChange = { reserveText = it },
                    label = "Reserve ($volumeLabel, optional)",
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decimalPlaces = 2,
                    supportingText = "Prediction stops at this ${if (isEv) "charge" else "fuel"} level"
                )
            }
        },
        confirmButton = {
            AppButton(
                text = "Save",
                onClick = {
                    if (nameError == null) {
                        val capacity = tankCapacityText.toDoubleOrNull()?.takeIf { it > 0 }
                        val reserve = reserveText.toDoubleOrNull()?.takeIf { it >= 0 }
                        onSave(
                            VehicleFormResult(
                                name = name,
                                vehicleType = vehicleType,
                                fuelType = fuelType,
                                distanceUnit = distanceUnit,
                                volumeUnit = if (fuelType.isElectric) VolumeUnit.KWH else volumeUnit,
                                tankCapacity = capacity,
                                reserveAmount = reserve
                            )
                        )
                    }
                },
                enabled = nameError == null
            )
        },
        dismissButton = {
            AppButtonOutlined(text = "Cancel", onClick = onDismiss)
        }
    )
}
