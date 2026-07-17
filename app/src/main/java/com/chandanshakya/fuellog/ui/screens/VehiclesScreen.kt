package com.chandanshakya.fuellog.ui.screens

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.DirectionsCar
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.ui.components.AppButton
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.VehiclesViewModel

@Composable
fun VehiclesScreen(
    onVehicleSelected: (Long) -> Unit,
    onAddVehicle: () -> Unit,
    viewModel: VehiclesViewModel = hiltViewModel()
) {
    val state by viewModel.vehiclesState.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.spacingMd)
    ) {
        Text(
            text = "Vehicles",
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
        } else if (state.vehicles.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.DirectionsCar,
                title = "No Vehicles",
                description = "Add your first vehicle to start tracking fuel fill-ups"
            )
        } else {
            LazyColumn(
                contentPadding = PaddingValues(bottom = Dimens.spacingXl),
                verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                items(
                    items = state.vehicles,
                    key = { it.id }
                ) { vehicle ->
                    VehicleCard(
                        vehicle = vehicle,
                        onClick = { onVehicleSelected(vehicle.id) },
                        onEdit = { },
                        onDelete = { viewModel.deleteVehicle(vehicle.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Dimens.spacingMd))

        AppButton(
            text = "Add Vehicle",
            onClick = { onAddVehicle() },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
fun VehicleCard(
    vehicle: Vehicle,
    onClick: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = MaterialTheme.shapes.medium,
        elevation = Dimens.cardElevation()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Outlined.DirectionsCar,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconLarge),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.size(Dimens.spacingMd))

            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = vehicle.name,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Text(
                    text = "${UnitConverter.getDistanceUnitLabel(vehicle.distanceUnit)} / ${UnitConverter.getVolumeUnitLabel(vehicle.volumeUnit)}",
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
    }
}

@Composable
fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(Dimens.spacingLg))

        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(Dimens.spacingSm))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 2
        )
    }
}
