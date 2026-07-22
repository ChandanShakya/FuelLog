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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VehicleType
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.Validation

@Composable
fun AddVehicleDialog(
    vehicle: Vehicle? = null,
    defaultDistanceUnit: DistanceUnit,
    defaultVolumeUnit: VolumeUnit,
    onDismiss: () -> Unit,
    onSave: (name: String, vehicleType: VehicleType, distanceUnit: DistanceUnit, volumeUnit: VolumeUnit) -> Unit
) {
    var name by remember { mutableStateOf(vehicle?.name ?: "") }
    var vehicleType by remember { mutableStateOf(vehicle?.vehicleType ?: VehicleType.CAR) }
    var distanceUnit by remember { mutableStateOf(vehicle?.distanceUnit ?: defaultDistanceUnit) }
    var volumeUnit by remember { mutableStateOf(vehicle?.volumeUnit ?: defaultVolumeUnit) }
    var nameError by remember { mutableStateOf<String?>(null) }

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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    VehicleType.entries.forEach { type ->
                        val isSelected = vehicleType == type
                        OutlinedButton(
                            onClick = { vehicleType = type },
                            modifier = Modifier.weight(1f).height(56.dp),
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
                                    modifier = Modifier.size(20.dp),
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
                    if (nameError == null) {
                        onSave(name, vehicleType, distanceUnit, volumeUnit)
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
