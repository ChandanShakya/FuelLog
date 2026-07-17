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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.components.AppBadge
import com.chandanshakya.fuellog.ui.components.AppButton
import com.chandanshakya.fuellog.ui.components.AppButtonOutlined
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.FuelLogViewModel
import java.time.format.DateTimeFormatter

@Composable
fun FuelLogScreen(
    vehicleId: Long,
    onNavigateToInsights: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    viewModel: FuelLogViewModel = hiltViewModel()
) {
    val state by viewModel.fuelLogState.collectAsStateWithLifecycle()
    var showAddDialog by remember { mutableStateOf(false) }

    LaunchedEffect(vehicleId) {
        viewModel.setVehicleId(vehicleId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.spacingMd)
    ) {
        val vehicle = state.vehicle
        if (vehicle != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.DirectionsCar,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconLarge),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.size(Dimens.spacingMd))

                Column {
                    Text(
                        text = vehicle.name,
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "${UnitConverter.getDistanceUnitLabel(vehicle.distanceUnit)} / ${UnitConverter.getVolumeUnitLabel(vehicle.volumeUnit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                AppButtonOutlined(
                    text = "Insights",
                    onClick = onNavigateToInsights
                )
            }

            Spacer(modifier = Modifier.height(Dimens.spacingLg))

            SummaryStats(
                averageMileage = state.averageMileage,
                totalDistance = state.totalDistance,
                totalFuel = state.totalFuel,
                totalCost = state.totalCost,
                distanceUnit = vehicle.distanceUnit,
                volumeUnit = vehicle.volumeUnit,
                currency = vehicle.defaultCurrency
            )

            Spacer(modifier = Modifier.height(Dimens.spacingLg))

            HorizontalDivider()

            Spacer(modifier = Modifier.height(Dimens.spacingMd))
        }

        if (state.entries.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.LocalGasStation,
                title = "No Fuel Entries",
                description = "Add your first fuel fill-up to start tracking"
            )
        } else {
            val currentVehicle = vehicle
            LazyColumn(
                contentPadding = PaddingValues(bottom = Dimens.spacingXl),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                items(
                    items = state.entries,
                    key = { it.entry.id }
                ) { entryWithMileage ->
                    FuelEntryCard(
                        entry = entryWithMileage.entry,
                        mileage = entryWithMileage.mileage,
                        distanceUnit = currentVehicle?.distanceUnit ?: DistanceUnit.KM,
                        volumeUnit = currentVehicle?.volumeUnit ?: VolumeUnit.LITERS,
                        currency = currentVehicle?.defaultCurrency ?: "USD",
                        onEdit = { },
                        onDelete = { viewModel.deleteFuelEntry(entryWithMileage.entry.id) }
                    )
                }
            }
        }
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.BottomEnd
    ) {
        FloatingActionButton(
            onClick = { showAddDialog = true },
            modifier = Modifier.padding(Dimens.spacingLg),
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        ) {
            Icon(
                imageVector = Icons.Outlined.Add,
                contentDescription = "Add fuel entry"
            )
        }
    }

    if (showAddDialog) {
        AddFuelEntryDialog(
            vehicleId = vehicleId,
            distanceUnit = state.vehicle?.distanceUnit ?: DistanceUnit.KM,
            volumeUnit = state.vehicle?.volumeUnit ?: VolumeUnit.LITERS,
            currency = state.vehicle?.defaultCurrency ?: "USD",
            onDismiss = { showAddDialog = false },
            onSave = { date, odometer, fuelVolume, fuelCost, isFullTank, notes ->
                viewModel.addFuelEntry(date, odometer, fuelVolume, fuelCost, isFullTank, notes)
                showAddDialog = false
            }
        )
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
        horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
    ) {
        StatCard(
            label = "Avg Mileage",
            value = averageMileage?.let { "%.1f ${UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)}" } ?: "N/A",
            icon = Icons.Outlined.Speed,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            label = "Total Distance",
            value = "%.0f ${UnitConverter.getDistanceUnitLabel(distanceUnit)}".format(totalDistance),
            icon = Icons.Outlined.DirectionsCar,
            modifier = Modifier.weight(1f)
        )

        StatCard(
            label = "Total Cost",
            value = CurrencyFormatter.formatCurrency(totalCost, currency),
            icon = Icons.Outlined.LocalGasStation,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
fun StatCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = Dimens.cardElevation()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingMd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconMedium),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium
            )

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    imageVector = Icons.Outlined.LocalGasStation,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconMedium),
                    tint = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.size(Dimens.spacingMd))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = DateTimeFormatter.ISO_LOCAL_DATE.format(entry.date),
                        style = MaterialTheme.typography.titleMedium
                    )

                    Text(
                        text = "Odometer: ${"%.0f".format(entry.odometer)} ${UnitConverter.getDistanceUnitLabel(distanceUnit)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(onClick = { showMenu = true }) {
                    Icon(
                        imageVector = Icons.Outlined.MoreVert,
                        contentDescription = "More options"
                    )
                }

                DropdownMenu(
                    expanded = showMenu,
                    onDismissRequest = { showMenu = false }
                ) {
                    DropdownMenuItem(
                        text = { Text("Edit") },
                        onClick = {
                            showMenu = false
                            onEdit()
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Edit, contentDescription = null)
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Delete") },
                        onClick = {
                            showMenu = false
                            onDelete()
                        },
                        leadingIcon = {
                            Icon(Icons.Outlined.Delete, contentDescription = null)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                mileage?.let { m ->
                    AppBadge(
                        text = "%.1f ${UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)}".format(m)
                    )
                }

                AppBadge(
                    text = "${"%.1f".format(entry.fuelVolume)} ${UnitConverter.getVolumeUnitLabel(volumeUnit)}"
                )

                AppBadge(
                    text = CurrencyFormatter.formatCurrency(entry.fuelCost, currency)
                )
            }
        }
    }
}

@Composable
fun AddFuelEntryDialog(
    vehicleId: Long,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    currency: String,
    onDismiss: () -> Unit,
    onSave: (java.time.LocalDate, Double, Double, Double, Boolean, String?) -> Unit
) {
    com.chandanshakya.fuellog.ui.components.AddFuelEntryDialog(
        vehicleId = vehicleId,
        distanceUnit = distanceUnit,
        volumeUnit = volumeUnit,
        currency = currency,
        onDismiss = onDismiss,
        onSave = onSave
    )
}
