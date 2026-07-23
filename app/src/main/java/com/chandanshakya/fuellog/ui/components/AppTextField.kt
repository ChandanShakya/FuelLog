package com.chandanshakya.fuellog.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import com.chandanshakya.fuellog.R

private fun filterDecimalInput(value: String, maxDecimalPlaces: Int): String {
    if (value.isEmpty()) return value
    val hasMinus = value.startsWith('-')
    val raw = if (hasMinus) value.removePrefix("-") else value
    val dotIndex = raw.indexOf('.')
    if (dotIndex == -1) return value
    val integerPart = raw.substring(0, dotIndex)
    val decimalPart = raw.substring(dotIndex + 1).filter { it.isDigit() }
    val truncated = decimalPart.take(maxDecimalPlaces)
    return (if (hasMinus) "-" else "") + integerPart + "." + truncated
}

/**
 * Custom text field wrapper to avoid naming conflicts with Material 3 components.
 * 
 * @param value Current value
 * @param onValueChange Callback for value changes
 * @param label Label text
 * @param modifier Modifier for the component
 * @param keyboardOptions Keyboard options
 * @param visualTransformation Visual transformation for the text
 * @param error Error message (if any)
 * @param supportingText Supporting text below the field
 */
@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    label: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None,
    error: String? = null,
    supportingText: String? = null,
    decimalPlaces: Int = 0
) {
    val filteredOnValueChange: (String) -> Unit = if (decimalPlaces > 0) {
        { newValue ->
            val filtered = filterDecimalInput(newValue, decimalPlaces)
            onValueChange(filtered)
        }
    } else {
        onValueChange
    }

    OutlinedTextField(
        value = value,
        onValueChange = filteredOnValueChange,
        label = { Text(label) },
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        enabled = enabled,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        isError = error != null,
        supportingText = {
            if (error != null) {
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error
                )
            } else if (supportingText != null) {
                Text(text = supportingText)
            }
        },
        trailingIcon = {
            if (error != null) {
                Icon(
                    painter = painterResource(R.drawable.ic_error),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error
                )
            }
        },
        singleLine = true
    )
}
