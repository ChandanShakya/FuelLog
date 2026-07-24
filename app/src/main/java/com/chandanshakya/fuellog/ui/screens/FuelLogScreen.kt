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
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.components.AppBadge
import com.chandanshakya.fuellog.ui.components.AppButton
import com.chandanshakya.fuellog.ui.components.AppButtonOutlined
import com.chandanshakya.fuellog.ui.components.AppTextField
import com.chandanshakya.fuellog.ui.components.EmptyState
import com.chandanshakya.fuellog.ui.components.InfoCard
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.util.FillUpPrediction
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.FuelLogViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelLogScreen(
    vehicleId: Long,
    onNavigateToInsights: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToOdometerLogs: () -> Unit = {},
    viewModel: FuelLogViewModel = hiltViewModel()
) {
    val state by viewModel.fuelLogState.collectAsStateWithLifecycle()
    val prediction by viewModel.nextFillUpPrediction.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showOdometerDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<FuelEntry?>(null) }
    var entryToView by remember { mutableStateOf<FuelEntry?>(null) }
    var pumpToEdit by remember { mutableStateOf<com.chandanshakya.fuellog.data.model.FuelPump?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.vehicle?.name ?: "Fuel Log") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToVehicles) {
                        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onNavigateToOdometerLogs) {
                        Icon(painter = painterResource(R.drawable.ic_speed), contentDescription = "Odometer Logs")
                    }
                    IconButton(onClick = onNavigateToInsights) {
                        Icon(painter = painterResource(R.drawable.ic_analytics), contentDescription = "Insights")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                SmallFloatingActionButton(
                    onClick = { showOdometerDialog = true },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer
                ) {
                    Icon(painter = painterResource(R.drawable.ic_speed), contentDescription = "Log odometer reading")
                }
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                FloatingActionButton(
                    onClick = { showAddDialog = true },
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(painter = painterResource(R.drawable.ic_add), contentDescription = "Add fuel entry")
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(Dimens.spacingMd)
            ) {
                val vehicle = state.vehicle
                if (vehicle != null) {
                    SummaryStats(
                        averageMileage = state.averageMileage,
                        totalDistance = state.totalDistance,
                        totalFuel = state.totalFuel,
                        totalCost = state.totalCost,
                        distanceUnit = vehicle.distanceUnit,
                        volumeUnit = vehicle.volumeUnit,
                        currency = state.currency
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacingSm))

                    if (prediction != null) {
                        NextFillUpCard(prediction = prediction!!, distanceUnit = vehicle.distanceUnit, volumeUnit = vehicle.volumeUnit)
                    } else if (vehicle.tankCapacity == null || vehicle.tankCapacity!! <= 0) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.medium,
                            elevation = Dimens.cardElevation()
                        ) {
                            Text(
                                text = "Set tank capacity in vehicle settings to see next fill-up prediction",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(Dimens.spacingMd)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(Dimens.spacingLg))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(Dimens.spacingMd))
                }

                if (state.entries.isEmpty()) {
                    EmptyState(
                        icon = painterResource(R.drawable.ic_local_gas_station),
                        title = "No Fuel Entries",
                        description = "Tap + to add your first fill-up"
                    )
                } else {
                    val currentVehicle = vehicle
                    LazyColumn(
                        contentPadding = PaddingValues(bottom = 80.dp),
                        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
                    ) {
                        items(items = state.entries, key = { it.entry.id }, contentType = { "fuel_entry" }) { entryWithMileage ->
                            FuelEntryCard(
                                entry = entryWithMileage.entry,
                                mileage = entryWithMileage.mileage,
                                averageMileage = state.averageMileage,
                                pumpName = entryWithMileage.pumpName,
                                distanceUnit = currentVehicle?.distanceUnit ?: DistanceUnit.KM,
                                volumeUnit = currentVehicle?.volumeUnit ?: VolumeUnit.LITERS,
                                currency = state.currency,
                                onClick = { entryToView = entryWithMileage.entry },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        val fuelPumps by viewModel.fuelPumps.collectAsStateWithLifecycle()
        com.chandanshakya.fuellog.ui.components.AddFuelEntryDialog(
            vehicleId = vehicleId,
            distanceUnit = state.vehicle?.distanceUnit ?: DistanceUnit.KM,
            volumeUnit = state.vehicle?.volumeUnit ?: VolumeUnit.LITERS,
            currency = state.currency,
            existingPumps = fuelPumps,
            onEditPump = { pump -> pumpToEdit = pump },
            onDeletePump = { pumpId -> viewModel.deletePump(pumpId) },
            onDismiss = { showAddDialog = false },
            onSave = { date, odometer, fuelVolume, totalCost, pumpName, isFullTank ->
                viewModel.addFuelEntry(date, odometer, fuelVolume, totalCost, pumpName, isFullTank)
                showAddDialog = false
            }
        )
    }

    if (entryToEdit != null) {
        val fuelPumps by viewModel.fuelPumps.collectAsStateWithLifecycle()
        val editPumpName = entryToEdit?.fuelPumpId?.let { pumpId ->
            fuelPumps.find { it.id == pumpId }?.name
        }
        com.chandanshakya.fuellog.ui.components.AddFuelEntryDialog(
            vehicleId = vehicleId,
            entry = entryToEdit,
            distanceUnit = state.vehicle?.distanceUnit ?: DistanceUnit.KM,
            volumeUnit = state.vehicle?.volumeUnit ?: VolumeUnit.LITERS,
            currency = state.currency,
            existingPumps = fuelPumps,
            initialPumpName = editPumpName,
            onEditPump = { pump -> pumpToEdit = pump },
            onDeletePump = { pumpId -> viewModel.deletePump(pumpId) },
            onDismiss = { entryToEdit = null },
            onSave = { date, odometer, fuelVolume, totalCost, pumpName, isFullTank ->
                entryToEdit?.let { existing ->
                    viewModel.updateFuelEntry(
                        id = existing.id,
                        date = date,
                        odometer = odometer,
                        fuelVolume = fuelVolume,
                        fuelCost = totalCost,
                        pumpName = pumpName,
                        isFullTank = isFullTank
                    )
                }
                entryToEdit = null
            }
        )
    }

    if (showOdometerDialog) {
        OdometerReadingDialog(
            distanceUnit = state.vehicle?.distanceUnit ?: DistanceUnit.KM,
            onDismiss = { showOdometerDialog = false },
            onSave = { date, odometer ->
                viewModel.addOdometerReading(date, odometer)
                showOdometerDialog = false
            }
        )
    }

    if (entryToView != null) {
        val fuelPumps by viewModel.fuelPumps.collectAsStateWithLifecycle()
        val viewEntry = entryToView
        val pumpName = viewEntry?.fuelPumpId?.let { pumpId ->
            fuelPumps.find { it.id == pumpId }?.name
        }
        FuelEntryDetailDialog(
            entry = viewEntry!!,
            mileage = null,
            pumpName = pumpName,
            distanceUnit = state.vehicle?.distanceUnit ?: DistanceUnit.KM,
            volumeUnit = state.vehicle?.volumeUnit ?: VolumeUnit.LITERS,
            currency = state.currency,
            onDismiss = { entryToView = null },
            onDelete = {
                viewModel.deleteFuelEntry(viewEntry.id)
                entryToView = null
            }
        )
    }

    if (pumpToEdit != null) {
        var editName by remember { mutableStateOf(pumpToEdit!!.name) }
        AlertDialog(
            onDismissRequest = { pumpToEdit = null },
            title = { Text("Rename Pump", style = MaterialTheme.typography.titleLarge) },
            text = {
                AppTextField(
                    value = editName,
                    onValueChange = { editName = it },
                    label = "Pump Name"
                )
            },
            confirmButton = {
                AppButton(
                    text = "Save",
                    onClick = {
                        if (editName.isNotBlank()) {
                            viewModel.updatePump(pumpToEdit!!.copy(name = editName.trim()))
                            pumpToEdit = null
                        }
                    },
                    enabled = editName.isNotBlank()
                )
            },
            dismissButton = {
                AppButtonOutlined(text = "Cancel", onClick = { pumpToEdit = null })
            }
        )
    }
}

@Composable
fun NextFillUpCard(
    prediction: FillUpPrediction,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    modifier: Modifier = Modifier
) {
    val distanceLabel = UnitConverter.getDistanceUnitLabel(distanceUnit)
    val daysUntil = prediction.predictedDate?.let {
        ChronoUnit.DAYS.between(LocalDate.now(), it).coerceAtLeast(0)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = Dimens.cardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingMd)
        ) {
            Text(
                text = "Next Fill-Up",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(Dimens.spacingSm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Refuel after",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "~${"%.0f".format(prediction.remainingDistance)} $distanceLabel",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Predicted date",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = if (daysUntil != null) {
                            if (daysUntil == 0L) "Today"
                            else if (daysUntil == 1L) "Tomorrow"
                            else "~$daysUntil days"
                        } else "N/A",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            prediction.predictedOdometer?.let { odo ->
                Spacer(modifier = Modifier.height(Dimens.spacingSm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Refuel at odometer",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "~${"%.0f".format(odo)} $distanceLabel",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))
            Text(
                text = "Based on ${"%.2f".format(prediction.recentMileage)} ${UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)} recent avg",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun SummaryStats(
    averageMileage: Double?,
    totalDistance: Double,
    totalFuel: Double,
    totalCost: Double,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    currency: String
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
    ) {
        InfoCard(label = "Avg Mileage", value = averageMileage?.let { "%.2f ${UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)}".format(it) } ?: "N/A", icon = painterResource(R.drawable.ic_speed), modifier = Modifier.weight(1f))
        InfoCard(label = "Total Distance", value = "%.2f ${UnitConverter.getDistanceUnitLabel(distanceUnit)}".format(totalDistance), icon = painterResource(R.drawable.ic_road), modifier = Modifier.weight(1f))
        InfoCard(label = "Total Cost", value = CurrencyFormatter.formatCurrency(totalCost, currency), icon = painterResource(R.drawable.ic_local_gas_station), modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelEntryCard(
    entry: FuelEntry,
    mileage: Double?,
    averageMileage: Double? = null,
    pumpName: String? = null,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    currency: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = Dimens.cardElevation()
    ) {
        Column(modifier = Modifier.fillMaxWidth().padding(Dimens.spacingMd)) {
            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Icon(painter = painterResource(R.drawable.ic_local_gas_station), contentDescription = null, modifier = Modifier.size(Dimens.iconMedium), tint = MaterialTheme.colorScheme.primary)
                Spacer(modifier = Modifier.size(Dimens.spacingMd))
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = DateTimeFormatter.ISO_LOCAL_DATE.format(entry.date), style = MaterialTheme.typography.titleMedium)
                    Text(
                        text = "Odometer: ${"%.2f".format(entry.odometer)} ${UnitConverter.getDistanceUnitLabel(distanceUnit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)) {
                if (entry.isFullTank) {
                    AppBadge(text = "Full Tank")
                }
                pumpName?.let { name ->
                    AppBadge(text = name)
                }
                mileage?.let { m ->
                    val badgeColor = when {
                        averageMileage == null -> MaterialTheme.colorScheme.primaryContainer
                        m >= averageMileage * 1.1 -> Color(0xFF4CAF50).copy(alpha = 0.15f)
                        m <= averageMileage * 0.9 -> Color(0xFFF44336).copy(alpha = 0.15f)
                        else -> MaterialTheme.colorScheme.primaryContainer
                    }
                    val textColor = when {
                        averageMileage == null -> MaterialTheme.colorScheme.onPrimaryContainer
                        m >= averageMileage * 1.1 -> Color(0xFF4CAF50)
                        m <= averageMileage * 0.9 -> Color(0xFFF44336)
                        else -> MaterialTheme.colorScheme.onPrimaryContainer
                    }
                    AppBadge(
                        text = "%.2f ${UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)}".format(m),
                        backgroundColor = badgeColor,
                        textColor = textColor
                    )
                }
                AppBadge(text = "${"%.2f".format(entry.fuelVolume)} ${UnitConverter.getVolumeUnitLabel(volumeUnit)}")
                AppBadge(text = CurrencyFormatter.formatCurrency(entry.fuelCost, currency))
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FuelEntryDetailDialog(
    entry: FuelEntry,
    mileage: Double?,
    pumpName: String? = null,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    currency: String,
    onDismiss: () -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteConfirm by remember { mutableStateOf(false) }

    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            title = { Text("Delete Entry", style = MaterialTheme.typography.titleLarge) },
            text = { Text("Are you sure you want to delete this fuel entry from ${DateTimeFormatter.ISO_LOCAL_DATE.format(entry.date)}? This action cannot be undone.") },
            confirmButton = {
                AppButton(
                    text = "Delete",
                    onClick = {
                        showDeleteConfirm = false
                        onDelete()
                    }
                )
            },
            dismissButton = {
                AppButtonOutlined(text = "Cancel", onClick = { showDeleteConfirm = false })
            }
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = DateTimeFormatter.ISO_LOCAL_DATE.format(entry.date),
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                DetailRow(label = "Odometer", value = "${"%.2f".format(entry.odometer)} ${UnitConverter.getDistanceUnitLabel(distanceUnit)}")
                DetailRow(label = "Volume", value = "${"%.2f".format(entry.fuelVolume)} ${UnitConverter.getVolumeUnitLabel(volumeUnit)}")
                DetailRow(label = "Cost", value = CurrencyFormatter.formatCurrency(entry.fuelCost, currency))
                if (entry.fuelVolume > 0) {
                    DetailRow(label = "Rate", value = "${"%.2f".format(entry.fuelCost / entry.fuelVolume)} ${currency}/${UnitConverter.getVolumeUnitLabel(volumeUnit)}")
                }
                if (pumpName != null) {
                    DetailRow(label = "Pump", value = pumpName)
                }
                if (entry.isFullTank) {
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))
                    AppBadge(text = "Full Tank")
                }
                mileage?.let { m ->
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))
                    AppBadge(
                        text = "Mileage: ${"%.2f".format(m)} ${UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)}"
                    )
                }
            }
        },
        confirmButton = {
            AppButton(
                text = "Delete",
                onClick = { showDeleteConfirm = true }
            )
        },
        dismissButton = {
            AppButtonOutlined(text = "Close", onClick = onDismiss)
        }
    )
}

@Composable
fun DetailRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = Dimens.spacingXs),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
