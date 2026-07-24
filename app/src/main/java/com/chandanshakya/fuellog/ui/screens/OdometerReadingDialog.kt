package com.chandanshakya.fuellog.ui.screens

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
import com.chandanshakya.fuellog.ui.components.AppButton
import com.chandanshakya.fuellog.ui.components.AppButtonOutlined
import com.chandanshakya.fuellog.ui.components.AppTextField
import com.chandanshakya.fuellog.ui.components.DatePickerField
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.UnitConverter
import java.time.LocalDate

@Composable
fun OdometerReadingDialog(
    distanceUnit: DistanceUnit,
    onDismiss: () -> Unit,
    onSave: (LocalDate, Double) -> Unit
) {
    var date by remember { mutableStateOf(LocalDate.now()) }
    var odometer by remember { mutableStateOf("") }
    var odometerError by remember { mutableStateOf<String?>(null) }

    val distanceLabel = UnitConverter.getDistanceUnitLabel(distanceUnit)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Log Odometer Reading", style = MaterialTheme.typography.titleLarge) },
        text = {
            Column(modifier = Modifier.padding(vertical = Dimens.spacingSm)) {
                Text(
                    text = "Record your current odometer without logging a fuel purchase.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                DatePickerField(date = date, onDateChange = { date = it })

                Spacer(modifier = Modifier.height(Dimens.spacingMd))

                AppTextField(
                    value = odometer,
                    onValueChange = {
                        odometer = it
                        odometerError = if (it.toDoubleOrNull() == null || (it.toDoubleOrNull() ?: 0.0) < 0) {
                            "Odometer cannot be negative"
                        } else null
                    },
                    label = "Odometer ($distanceLabel)",
                    error = odometerError,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    decimalPlaces = 2
                )
            }
        },
        confirmButton = {
            AppButton(
                text = "Save",
                onClick = {
                    val odo = odometer.toDoubleOrNull() ?: 0.0
                    if (odometerError == null && odo >= 0) {
                        onSave(date, odo)
                    }
                },
                enabled = odometerError == null && odometer.toDoubleOrNull() != null
            )
        },
        dismissButton = {
            AppButtonOutlined(text = "Cancel", onClick = onDismiss)
        }
    )
}
