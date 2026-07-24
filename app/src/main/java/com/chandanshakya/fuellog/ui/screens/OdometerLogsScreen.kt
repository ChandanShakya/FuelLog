package com.chandanshakya.fuellog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.chandanshakya.fuellog.data.model.OdometerReading
import com.chandanshakya.fuellog.ui.components.AppButton
import com.chandanshakya.fuellog.ui.components.AppButtonOutlined
import com.chandanshakya.fuellog.ui.components.EmptyState
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.FuelLogViewModel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OdometerLogsScreen(
    vehicleId: Long,
    onNavigateBack: () -> Unit,
    onAddReading: () -> Unit,
    viewModel: FuelLogViewModel = hiltViewModel()
) {
    val readings by viewModel.odometerReadings.collectAsStateWithLifecycle()
    val vehicle = viewModel.fuelLogState.collectAsStateWithLifecycle().value.vehicle
    val distanceUnit = vehicle?.distanceUnit ?: DistanceUnit.KM
    var readingToDelete by remember { mutableStateOf<OdometerReading?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Odometer Logs") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.spacingMd)
        ) {
            if (readings.isEmpty()) {
                EmptyState(
                    icon = painterResource(R.drawable.ic_speed),
                    title = "No Odometer Readings",
                    description = "Tap the odometer button on the fuel log screen to record readings"
                )
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                ) {
                    items(items = readings, key = { it.id }) { reading ->
                        OdometerReadingCard(
                            reading = reading,
                            distanceUnit = distanceUnit,
                            onClick = { readingToDelete = reading }
                        )
                    }
                }
            }
        }
    }

    readingToDelete?.let { reading ->
        AlertDialog(
            onDismissRequest = { readingToDelete = null },
            title = { Text("Delete Reading", style = MaterialTheme.typography.titleLarge) },
            text = {
                Text("Delete odometer reading from ${DateTimeFormatter.ISO_LOCAL_DATE.format(reading.date)}?")
            },
            confirmButton = {
                AppButton(
                    text = "Delete",
                    onClick = {
                        viewModel.deleteOdometerReading(reading.id)
                        readingToDelete = null
                    }
                )
            },
            dismissButton = {
                AppButtonOutlined(text = "Cancel", onClick = { readingToDelete = null })
            }
        )
    }
}

@Composable
fun OdometerReadingCard(
    reading: OdometerReading,
    distanceUnit: DistanceUnit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val distanceLabel = UnitConverter.getDistanceUnitLabel(distanceUnit)
    val daysAgo = ChronoUnit.DAYS.between(reading.date, LocalDate.now()).coerceAtLeast(0)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingMd),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = DateTimeFormatter.ISO_LOCAL_DATE.format(reading.date),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    text = if (daysAgo == 0L) "Today" else "$daysAgo days ago",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Text(
                text = "${"%.2f".format(reading.odometer)} $distanceLabel",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
