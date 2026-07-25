package com.chandanshakya.fuellog.ui.screens

import android.app.Activity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.components.AppButton
import com.chandanshakya.fuellog.ui.components.AppButtonOutlined
import com.chandanshakya.fuellog.ui.components.AppTextField
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.Validation
import com.chandanshakya.fuellog.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToVehicles: () -> Unit,
    viewModel: SettingsViewModel = viewModel(factory = SettingsViewModel.Factory)
) {
    val state by viewModel.settingsState.collectAsStateWithLifecycle()
    val msg by viewModel.message.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    var currency by remember { mutableStateOf("USD") }
    var distanceUnit by remember { mutableStateOf(DistanceUnit.KM) }
    var volumeUnit by remember { mutableStateOf(VolumeUnit.LITERS) }
    var currencyError by remember { mutableStateOf<String?>(null) }
    var showClearDialog by remember { mutableStateOf(false) }
    var showImportDialog by remember { mutableStateOf(false) }
    var pendingImportUri by remember { mutableStateOf<android.net.Uri?>(null) }

    val exportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri ->
        uri?.let { viewModel.exportData(context, it) }
    }

    val importLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        uri?.let {
            pendingImportUri = it
            showImportDialog = true
        }
    }

    LaunchedEffect(state.settings) {
        state.settings?.let { settings ->
            currency = settings.defaultCurrency
            distanceUnit = settings.defaultDistanceUnit
            volumeUnit = settings.defaultVolumeUnit
        }
    }

    LaunchedEffect(msg) {
        msg?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.clearMessage()
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToVehicles) {
                        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.spacingMd)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(bottom = Dimens.spacingXl),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                item {
                    SettingCard(
                        title = "Default Currency",
                        icon = painterResource(R.drawable.ic_currency_exchange),
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
                        icon = painterResource(R.drawable.ic_arrow_range),
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
                        icon = painterResource(R.drawable.ic_airwave),
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
                                verticalAlignment = Alignment.CenterVertically
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
                                scope.launch {
                                    snackbarHostState.showSnackbar("Settings saved")
                                }
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = currencyError == null
                    )
                }

                // --- Data Management Section ---
                item {
                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(Dimens.spacingMd))
                    Text(
                        text = "Data Management",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                item {
                    SettingCard(
                        title = "Export Data",
                        icon = painterResource(R.drawable.ic_point_of_sale),
                        description = "Save a backup of all your data to a file"
                    ) {
                        AppButton(
                            text = "Export to File",
                            onClick = {
                                val filename = "fuellog_backup_${LocalDate.now()}.json"
                                exportLauncher.launch(filename)
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    SettingCard(
                        title = "Import Data",
                        icon = painterResource(R.drawable.ic_date_range),
                        description = "Restore data from a previously exported backup"
                    ) {
                        AppButtonOutlined(
                            text = "Import from File",
                            onClick = {
                                importLauncher.launch(arrayOf("application/json"))
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                item {
                    SettingCard(
                        title = "Clear All Data",
                        icon = painterResource(R.drawable.ic_delete),
                        description = "Permanently delete all vehicles, entries, and settings"
                    ) {
                        AppButton(
                            text = "Clear All Data",
                            onClick = { showClearDialog = true },
                            modifier = Modifier.fillMaxWidth(),
                            containerColor = MaterialTheme.colorScheme.error,
                            contentColor = MaterialTheme.colorScheme.onError
                        )
                    }
                }
            }
        }
    }

    // Clear All Data Confirmation Dialog
    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text("Clear All Data?") },
            text = {
                Text("This will permanently delete all vehicles, fuel entries, odometer readings, pump data, and settings. This action cannot be undone.")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        viewModel.clearAllData()
                    }
                ) {
                    Text("Delete Everything", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }

    // Import Confirmation Dialog
    if (showImportDialog) {
        AlertDialog(
            onDismissRequest = { showImportDialog = false; pendingImportUri = null },
            title = { Text("Import Data?") },
            text = {
                Text("This will replace ALL existing data with the backup file contents. This action cannot be undone. Continue?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        pendingImportUri?.let { uri ->
                            viewModel.importData(context, uri)
                        }
                        showImportDialog = false
                        pendingImportUri = null
                    }
                ) {
                    Text("Import", color = MaterialTheme.colorScheme.primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportDialog = false; pendingImportUri = null }) {
                    Text("Cancel")
                }
            }
        )
    }
}

@Composable
fun SettingCard(
    title: String,
    icon: androidx.compose.ui.graphics.painter.Painter,
    description: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = Dimens.cardElevation()
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
                    painter = icon,
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
