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
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.ArrowDownward
import androidx.compose.material.icons.outlined.ArrowUpward
import androidx.compose.material.icons.outlined.GasStation
import androidx.compose.material.icons.outlined.HorizontalRule
import androidx.compose.material.icons.outlined.LocalGasStation
import androidx.compose.material.icons.outlined.Monitor
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.chart.MileageChart
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.InsightsViewModel
import com.chandanshakya.fuellog.viewmodel.MileageTrend

/**
 * Insights screen showing statistics and trends for a specific vehicle.
 * 
 * @param vehicleId ID of the vehicle to show insights for
 * @param onNavigateToLog Callback to navigate to fuel log
 * @param onNavigateToVehicles Callback to navigate to vehicles
 */
@Composable
fun InsightsScreen(
    vehicleId: Long,
    onNavigateToLog: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.insightsState.collectAsStateWithLifecycle()
    
    LaunchedEffect(vehicleId) {
        viewModel.setVehicleId(vehicleId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(Dimens.spacingMd)
    ) {
        // Header
        val vehicle = state.vehicle
        if (vehicle != null) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Outlined.Analytics,
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconLarge),
                    tint = MaterialTheme.colorScheme.primary
                )
                
                Spacer(modifier = Modifier.size(Dimens.spacingMd))
                
                Column {
                    Text(
                        text = "${vehicle.name} Insights",
                        style = MaterialTheme.typography.titleLarge
                    )
                    Text(
                        text = "Fuel efficiency analysis",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(Dimens.spacingLg))
        }

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (state.entries.isEmpty()) {
            EmptyState(
                icon = Icons.Outlined.Analytics,
                title = "No Data Available",
                description = "Add fuel entries to see insights and trends"
            )
        } else {
            // Mileage trend chart
            val dataPoints = viewModel.getMileageDataPoints()
            if (dataPoints.isNotEmpty()) {
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
                        Text(
                            text = "Mileage Trend",
                            style = MaterialTheme.typography.titleMedium
                        )
                        
                        Spacer(modifier = Modifier.height(Dimens.spacingMd))
                        
                        MileageChart(
                            dataPoints = dataPoints,
                            modifier = Modifier.fillMaxWidth()
                        )
                        
                        Spacer(modifier = Modifier.height(Dimens.spacingSm))
                        
                        // Trend indicator
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            when (state.mileageTrend) {
                                MileageTrend.IMPROVING -> {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowUpward,
                                        contentDescription = null,
                                        tint = Color.Green
                                    )
                                    Text(
                                        text = "Improving",
                                        color = Color.Green,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                MileageTrend.DECLINING -> {
                                    Icon(
                                        imageVector = Icons.Outlined.ArrowDownward,
                                        contentDescription = null,
                                        tint = Color.Red
                                    )
                                    Text(
                                        text = "Declining",
                                        color = Color.Red,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                MileageTrend.STABLE -> {
                                    Icon(
                                        imageVector = Icons.Outlined.HorizontalRule,
                                        contentDescription = null,
                                        tint = Color.Gray
                                    )
                                    Text(
                                        text = "Stable",
                                        color = Color.Gray,
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                            }
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(Dimens.spacingLg))
            }
            
            // Statistics grid
            val distanceUnit = vehicle?.distanceUnit ?: DistanceUnit.KM
            val volumeUnit = vehicle?.volumeUnit ?: VolumeUnit.LITERS
            val currency = vehicle?.defaultCurrency ?: "USD"
            
            StatisticsGrid(
                averageMileage = state.averageMileageKmPerLiter,
                bestMileage = state.bestMileageKmPerLiter,
                worstMileage = state.worstMileageKmPerLiter,
                totalDistance = state.totalDistanceKm,
                totalFuel = state.totalFuelLiters,
                totalCost = state.totalCost,
                costPerKm = state.costPerKm,
                entriesCount = state.entriesCount,
                distanceUnit = distanceUnit,
                volumeUnit = volumeUnit,
                currency = currency
            )
        }
    }
}

/**
 * Statistics grid showing various metrics.
 */
@Composable
fun StatisticsGrid(
    averageMileage: Double?,
    bestMileage: Double?,
    worstMileage: Double?,
    totalDistance: Double,
    totalFuel: Double,
    totalCost: Double,
    costPerKm: Double?,
    entriesCount: Int,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    currency: String
) {
    val efficiencyLabel = UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)
    
    LazyColumn(
        contentPadding = PaddingValues(bottom = Dimens.spacingXl),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
    ) {
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                InsightCard(
                    label = "Average Mileage",
                    value = averageMileage?.let { "%.1f $efficiencyLabel".format(it) } ?: "N/A",
                    icon = Icons.Outlined.Speed,
                    modifier = Modifier.weight(1f)
                )
                
                InsightCard(
                    label = "Best Mileage",
                    value = bestMileage?.let { "%.1f $efficiencyLabel".format(it) } ?: "N/A",
                    icon = Icons.Outlined.ArrowUpward,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                InsightCard(
                    label = "Worst Mileage",
                    value = worstMileage?.let { "%.1f $efficiencyLabel".format(it) } ?: "N/A",
                    icon = Icons.Outlined.ArrowDownward,
                    modifier = Modifier.weight(1f)
                )
                
                InsightCard(
                    label = "Total Entries",
                    value = entriesCount.toString(),
                    icon = Icons.Outlined.Monitor,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                InsightCard(
                    label = "Total Distance",
                    value = "%.0f ${UnitConverter.getDistanceUnitLabel(distanceUnit)}".format(totalDistance),
                    icon = Icons.Outlined.LocalGasStation,
                    modifier = Modifier.weight(1f)
                )
                
                InsightCard(
                    label = "Total Fuel",
                    value = "%.1f ${UnitConverter.getVolumeUnitLabel(volumeUnit)}".format(totalFuel),
                    icon = Icons.Outlined.GasStation,
                    modifier = Modifier.weight(1f)
                )
            }
        }
        
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                InsightCard(
                    label = "Total Cost",
                    value = CurrencyFormatter.formatCurrency(totalCost, currency),
                    icon = Icons.Outlined.Monitor,
                    modifier = Modifier.weight(1f)
                )
                
                InsightCard(
                    label = "Cost per km",
                    value = costPerKm?.let { CurrencyFormatter.formatCurrency(it, currency) } ?: "N/A",
                    icon = Icons.Outlined.Speed,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

/**
 * Individual insight card.
 */
@Composable
fun InsightCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        elevation = Dimens.cardElevation
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
