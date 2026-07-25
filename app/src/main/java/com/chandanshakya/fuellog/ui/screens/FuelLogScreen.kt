package com.chandanshakya.fuellog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
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
    viewModel: FuelLogViewModel = viewModel(factory = FuelLogViewModel.factory(vehicleId))
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
                        NextFillUpCard(prediction = prediction!!, distanceUnit = vehicle.distanceUnit, volumeUnit = vehicle.volumeUnit, entryCount = state.entries.size)
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
                                onClick = { entryToEdit = entryWithMileage.entry },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }
                }
            }
        }
    }

    val showEntryDialog = showAddDialog || entryToEdit != null
    if (showEntryDialog) {
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
            onDismiss = { showAddDialog = false; entryToEdit = null },
            onSave = { date, odometer, fuelVolume, totalCost, pumpName, isFullTank ->
                val existing = entryToEdit
                if (existing != null) {
                    viewModel.updateFuelEntry(
                        id = existing.id, date = date, odometer = odometer,
                        fuelVolume = fuelVolume, fuelCost = totalCost,
                        pumpName = pumpName, isFullTank = isFullTank
                    )
                } else {
                    viewModel.addFuelEntry(date, odometer, fuelVolume, totalCost, pumpName, isFullTank)
                }
                showAddDialog = false; entryToEdit = null
            },
            onDelete = {
                entryToEdit?.let { existing -> viewModel.deleteFuelEntry(existing.id) }
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
    volumeUnit: VolumeUnit,
    entryCount: Int = 0,
    modifier: Modifier = Modifier
) {
    val distanceLabel = UnitConverter.getDistanceUnitLabel(distanceUnit)
    val daysUntil = prediction.predictedDate?.let {
        ChronoUnit.DAYS.between(LocalDate.now(), it).coerceAtLeast(0)
    }

    val isLowUrgency = daysUntil != null && daysUntil <= 1

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (isLowUrgency) Modifier.border(1.dp, MaterialTheme.colorScheme.errorContainer, MaterialTheme.shapes.medium) else Modifier),
        shape = MaterialTheme.shapes.medium,
        elevation = Dimens.cardElevation(),
        colors = CardDefaults.cardColors(
            containerColor = if (isLowUrgency) {
                MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0f)
            }
        )
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
            if (entryCount < 5) {
                Text(
                    text = "Estimate based on limited history — $entryCount entries logged",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "Avg: ${"%.2f".format(prediction.recentMileage)} ${UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Text(
                    text = "Based on ${"%.2f".format(prediction.recentMileage)} ${UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)} recent avg",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
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
        InfoCard(label = "Avg Mileage", value = averageMileage?.let { "%.2f ${UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)}".format(it) } ?: "N/A", icon = painterResource(R.drawable.ic_avg_pace), modifier = Modifier.weight(1f))
        InfoCard(label = "Total Distance", value = "%.2f ${UnitConverter.getDistanceUnitLabel(distanceUnit)}".format(totalDistance), icon = painterResource(R.drawable.ic_road), modifier = Modifier.weight(1f))
        InfoCard(label = "Total Cost", value = CurrencyFormatter.formatCurrency(totalCost, currency), icon = painterResource(R.drawable.ic_payments), modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
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
    val fuelIconShape = RoundedCornerShape(6.dp)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        elevation = Dimens.cardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Line 1: header row — icon + date/odo on left, mileage on right
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .background(MaterialTheme.colorScheme.primaryContainer, fuelIconShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_local_gas_station),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                Spacer(modifier = Modifier.size(8.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = DateTimeFormatter.ISO_LOCAL_DATE.format(entry.date),
                        style = MaterialTheme.typography.titleSmall
                    )
                    Text(
                        text = "${"%.0f".format(entry.odometer)} ${UnitConverter.getDistanceUnitLabel(distanceUnit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                if (mileage != null) {
                    val mileageColor = when {
                        averageMileage == null -> MaterialTheme.colorScheme.primary
                        mileage >= averageMileage * 1.1 -> MaterialTheme.colorScheme.tertiary
                        mileage <= averageMileage * 0.9 -> MaterialTheme.colorScheme.error
                        else -> MaterialTheme.colorScheme.primary
                    }
                    Text(
                        text = "%.2f".format(mileage),
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = mileageColor
                    )
                    Spacer(modifier = Modifier.size(2.dp))
                    Text(
                        text = UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit),
                        style = MaterialTheme.typography.labelSmall,
                        color = mileageColor
                    )
                }
            }

            // Divider
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)

            // Line 2: detail row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: tags
                FlowRow(
                    modifier = Modifier.weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    verticalArrangement = Arrangement.spacedBy(2.dp)
                ) {
                    if (entry.isFullTank) {
                        Box(
                            modifier = Modifier
                                .background(MaterialTheme.colorScheme.tertiaryContainer, RoundedCornerShape(4.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "Full",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onTertiaryContainer
                            )
                        }
                    }
                    if (entry.isFullTank && pumpName != null) {
                        Text(
                            text = "·",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    pumpName?.let { name ->
                        Text(
                            text = name,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.widthIn(max = 150.dp)
                        )
                    }
                }

                // Right: volume + cost
                Text(
                    text = "${"%.2f".format(entry.fuelVolume)} ${UnitConverter.getVolumeUnitLabel(volumeUnit)}",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = CurrencyFormatter.formatCurrency(entry.fuelCost.toLong().toDouble(), currency),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
