package com.chandanshakya.fuellog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CurrencyExchange
import androidx.compose.material.icons.outlined.Ruler
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Straighten
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.components.AppButton
import com.chandanshakya.fuellog.ui.components.AppTextField
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.Validation
import com.chandanshakya.fuellog.viewmodel.SettingsViewModel

/**
 * Settings screen for managing global preferences.
 * 
 * @param onNavigateToVehicles Callback to navigate to vehicles
 */
@Composable
fun SettingsScreen(
    onNavigateToVehicles: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val state by viewModel.settingsState.collectAsStateWithLifecycle()
    
    var currency by remember { mutableStateOf("USD") }
    var distanceUnit by remember { mutableStateOf(DistanceUnit.KM) }
    var volumeUnit by remember { mutableStateOf(VolumeUnit.LITERS) }
    var currencyError by remember { mutableStateOf<String?>(null) }
    
    // Initialize from settings
    state.settings?.let { settings ->
        currency = settings.defaultCurrency
        distanceUnit = settings.defaultDistanceUnit
        volumeUnit = settings.defaultVolumeUnit
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.spacingMd)
    ) {
        Text(
            text = "Settings",
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = Dimens.spacingLg)
        )

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = Dimens.spacingXl),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                item {
                    SettingCard(
                        title = "Default Currency",
                        icon = Icons.Outlined.CurrencyExchange,
                        description = "Currency used for new vehicles"
                    ) {
                        AppTextField(
                            value = currency,
                            onValueChange = { newValue ->
                                currency = newValue.uppercase()
                                currencyError = Validation.getCurrencyCodeError(newValue)
                            },
                            label = "Currency Code",
                            error = currencyError,
                            supportingText = "ISO 4217 code (e.g., USD, EUR, INR)"
                        )
                    }
                }
                
                item {
                    SettingCard(
                        title = "Default Distance Unit",
                        icon = Icons.Outlined.Ruler,
                        description = "Distance unit used for new vehicles"
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = distanceUnit == DistanceUnit.KM,
                                    onClick = { distanceUnit = DistanceUnit.KM }
                                )
                                Spacer(modifier = Modifier.size(Dimens.spacingSm))
                                Text("Kilometers (km)")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = distanceUnit == DistanceUnit.MILES,
                                    onClick = { distanceUnit = DistanceUnit.MILES }
                                )
                                Spacer(modifier = Modifier.size(Dimens.spacingSm))
                                Text("Miles (mi)")
                            }
                        }
                    }
                }
                
                item {
                    SettingCard(
                        title = "Default Volume Unit",
                        icon = Icons.Outlined.Straighten,
                        description = "Volume unit used for new vehicles"
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                RadioButton(
                                    selected = volumeUnit == VolumeUnit.LITERS,
                                    onClick = { volumeUnit = VolumeUnit.LITERS }
                                )
                                Spacer(modifier = Modifier.size(Dimens.spacingSm))
                                Text("Liters (L)")
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalArrangement = Arrangement.Center
                            ) {
                                RadioButton(
                                    selected = volumeUnit == VolumeUnit.GALLONS,
                                    onClick = { volumeUnit = VolumeUnit.GALLONS }
                                )
                                Spacer(modifier = Modifier.size(Dimens.spacingSm))
                                Text("Gallons (gal)")
                            }
                        }
                    }
                }
                
                item {
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                    
                    AppButton(
                        text = "Save Settings",
                        onClick = {
                            if (currencyError == null) {
                                viewModel.updateSettings(
                                    currency = currency,
                                    distanceUnit = distanceUnit,
                                    volumeUnit = volumeUnit
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currencyError == null
                    )
                }
            }
        }
    }
}

/**
 * Setting card component.
 */
@Composable
fun SettingCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = Dimens.cardElevation
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingMd)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconMedium),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.size(Dimens.spacingMd))
                
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.spacingMd))
            
            HorizontalDivider()
            
            Spacer(modifier = Modifier.height(Dimens.spacingMd))
            
            content()
        }
    }
}
