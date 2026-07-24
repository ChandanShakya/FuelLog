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
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
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
    viewModel: FuelLogViewModel = hiltViewModel()
) {
    val state by viewModel.fuelLogState.collectAsStateWithLifecycle()
    val prediction by viewModel.nextFillUpPrediction.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }
    var showOdometerDialog by remember { mutableStateOf(false) }
    var entryToEdit by remember { mutableStateOf<FuelEntry?>(null) }
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
                    IconButton(onClick = onNavigateToInsights) {
                        Icon(painter = painterResource(R.drawable.ic_analytics), contentDescription = "Insights")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(horizontalAlignment = Alignment.End) {
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
                        NextFillUpCard(prediction = prediction!!, distanceUnit = vehicle.distanceUnit)
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
                                distanceUnit = currentVehicle?.distanceUnit ?: DistanceUnit.KM,
                                volumeUnit = currentVehicle?.volumeUnit ?: VolumeUnit.LITERS,
                                currency = state.currency,
                                onEdit = { entryToEdit = entryWithMileage.entry },
                                onDelete = { viewModel.deleteFuelEntry(entryWithMileage.entry.id) }
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
                        text = "Remaining",
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
                        text = "Predicted",
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
            Spacer(modifier = Modifier.height(Dimens.spacingSm))
            Text(
                text = "Based on ${"%.2f".format(prediction.recentMileage)} ${UnitConverter.getEfficiencyLabel(distanceUnit, VolumeUnit.LITERS)} recent avg",
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

@Composable
fun InfoCard(label: String, value: String, icon: androidx.compose.ui.graphics.painter.Painter, modifier: Modifier = Modifier) {
    Card(modifier = modifier, shape = MaterialTheme.shapes.medium, elevation = Dimens.cardElevation()) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Dimens.spacingSm, vertical = Dimens.spacingMd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(painter = icon, contentDescription = null, modifier = Modifier.size(Dimens.iconMedium), tint = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(Dimens.spacingSm))
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (value.length > 8) MaterialTheme.typography.titleSmall.fontSize else MaterialTheme.typography.titleMedium.fontSize
                ),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun FuelEntryCard(
    entry: FuelEntry,
    mileage: Double?,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    currency: String,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(modifier = Modifier.fillMaxWidth(), shape = MaterialTheme.shapes.medium, elevation = Dimens.cardElevation()) {
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
                IconButton(onClick = { showMenu = true }) {
                    Icon(painter = painterResource(R.drawable.ic_more_vert), contentDescription = "More options")
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(text = { Text("Edit") }, onClick = { showMenu = false; onEdit() }, leadingIcon = { Icon(painter = painterResource(R.drawable.ic_edit), null) })
                    DropdownMenuItem(text = { Text("Delete") }, onClick = { showMenu = false; onDelete() }, leadingIcon = { Icon(painter = painterResource(R.drawable.ic_delete), null) })
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)) {
                if (entry.isFullTank) {
                    AppBadge(text = "Full Tank")
                }
                mileage?.let { m ->
                    AppBadge(text = "%.2f ${UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)}".format(m))
                }
                AppBadge(text = "${"%.2f".format(entry.fuelVolume)} ${UnitConverter.getVolumeUnitLabel(volumeUnit)}")
                AppBadge(text = CurrencyFormatter.formatCurrency(entry.fuelCost, currency))
            }
        }
    }
}
