package com.chandanshakya.fuellog.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ListItem
import androidx.compose.material3.ListItemDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.viewmodel.SettingsViewModel
import com.chandanshakya.fuellog.util.Validation
import kotlinx.coroutines.delay
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
            showClearDialog = false
            viewModel.importData(context, it)
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
                .verticalScroll(rememberScrollState())
        ) {
            // --- Section: Defaults ---
            SectionHeader("DEFAULTS FOR NEW VEHICLES")

            // Default Currency
            ListItem(
                headlineContent = { Text("Currency") },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_currency_exchange),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    OutlinedTextField(
                        value = currency,
                        onValueChange = { newValue ->
                            val filtered = newValue.uppercase().take(3)
                            currency = filtered
                            currencyError = Validation.getCurrencyCodeError(filtered)
                            if (currencyError == null && filtered.length == 3) {
                                viewModel.updateSettings(
                                    currency = filtered,
                                    distanceUnit = distanceUnit,
                                    volumeUnit = volumeUnit
                                )
                                scope.launch {
                                    delay(500)
                                    snackbarHostState.showSnackbar("Settings saved")
                                }
                            }
                        },
                        modifier = Modifier.width(64.dp),
                        singleLine = true,
                        isError = currencyError != null,
                        textStyle = MaterialTheme.typography.bodyLarge.copy(
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Characters,
                            keyboardType = KeyboardType.Text
                        )
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Default Distance Unit
            ListItem(
                headlineContent = { Text("Distance") },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_range),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.width(140.dp)) {
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            onClick = {
                                distanceUnit = DistanceUnit.KM
                                viewModel.updateSettings(currency, DistanceUnit.KM, volumeUnit)
                                scope.launch {
                                    delay(500)
                                    snackbarHostState.showSnackbar("Settings saved")
                                }
                            },
                            selected = distanceUnit == DistanceUnit.KM,
                            label = { Text("km", fontSize = 13.sp) }
                        )
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            onClick = {
                                distanceUnit = DistanceUnit.MILES
                                viewModel.updateSettings(currency, DistanceUnit.MILES, volumeUnit)
                                scope.launch {
                                    delay(500)
                                    snackbarHostState.showSnackbar("Settings saved")
                                }
                            },
                            selected = distanceUnit == DistanceUnit.MILES,
                            label = { Text("mi", fontSize = 13.sp) }
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Default Volume Unit
            ListItem(
                headlineContent = { Text("Volume") },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_airwave),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    SingleChoiceSegmentedButtonRow(modifier = Modifier.width(140.dp)) {
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2),
                            onClick = {
                                volumeUnit = VolumeUnit.LITERS
                                viewModel.updateSettings(currency, distanceUnit, VolumeUnit.LITERS)
                                scope.launch {
                                    delay(500)
                                    snackbarHostState.showSnackbar("Settings saved")
                                }
                            },
                            selected = volumeUnit == VolumeUnit.LITERS,
                            label = { Text("L", fontSize = 13.sp) }
                        )
                        SegmentedButton(
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2),
                            onClick = {
                                volumeUnit = VolumeUnit.GALLONS
                                viewModel.updateSettings(currency, distanceUnit, VolumeUnit.GALLONS)
                                scope.launch {
                                    delay(500)
                                    snackbarHostState.showSnackbar("Settings saved")
                                }
                            },
                            selected = volumeUnit == VolumeUnit.GALLONS,
                            label = { Text("gal", fontSize = 13.sp) }
                        )
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // --- Section: Data Management ---
            Spacer(modifier = Modifier.height(16.dp))
            SectionHeader("DATA MANAGEMENT")

            // Backup & Restore
            ListItem(
                headlineContent = { Text("Backup & restore") },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_date_range),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                },
                trailingContent = {
                    Row {
                        OutlinedButton(
                            onClick = { importLauncher.launch(arrayOf("application/json")) },
                            modifier = Modifier.height(32.dp),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text("Import", fontSize = 12.sp)
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = {
                                val filename = "fuellog_backup_${LocalDate.now()}.json"
                                exportLauncher.launch(filename)
                            },
                            modifier = Modifier.height(32.dp),
                            contentPadding = ButtonDefaults.ContentPadding
                        ) {
                            Text("Export", fontSize = 12.sp)
                        }
                    }
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent)
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp))

            // Clear All Data — with eyebrow label
            ListItem(
                headlineContent = {
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "IRREVERSIBLE",
                                style = MaterialTheme.typography.labelSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                    letterSpacing = 1.sp
                                ),
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(
                                painter = painterResource(R.drawable.ic_error),
                                contentDescription = null,
                                modifier = Modifier.size(10.dp),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                        Text(
                            text = "Clear All Data",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                },
                leadingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_delete),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                trailingContent = {
                    Icon(
                        painter = painterResource(R.drawable.ic_arrow_range),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error
                    )
                },
                colors = ListItemDefaults.colors(containerColor = Color.Transparent),
                modifier = Modifier.clickable { showClearDialog = true }
            )

            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    // Clear All Data Confirmation Dialog
    if (showClearDialog) {
        var deleteText by remember { mutableStateOf("") }
        val isDeleteEnabled = deleteText == "DELETE"

        AlertDialog(
            onDismissRequest = { showClearDialog = false; deleteText = "" },
            title = { Text("Clear All Data?") },
            text = {
                Column {
                    Text(
                        "This will permanently delete all vehicles, fuel entries, odometer readings, pump data, and settings. This action cannot be undone."
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = deleteText,
                        onValueChange = { deleteText = it },
                        label = { Text("Type DELETE to confirm") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text)
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showClearDialog = false
                        deleteText = ""
                        viewModel.clearAllData()
                    },
                    enabled = isDeleteEnabled,
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = MaterialTheme.colorScheme.error,
                        disabledContentColor = MaterialTheme.colorScheme.error.copy(alpha = 0.38f)
                    )
                ) {
                    Text("Delete Everything")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false; deleteText = "" }) {
                    Text("Cancel")
                }
            },
            shape = RoundedCornerShape(28.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall.copy(
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        ),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp)
    )
}
