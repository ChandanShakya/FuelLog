package com.chandanshakya.fuellog.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import com.chandanshakya.fuellog.ui.components.AppButton
import com.chandanshakya.fuellog.ui.components.EmptyState
import com.chandanshakya.fuellog.ui.components.InfoCard
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.chart.FuelPriceChart
import com.chandanshakya.fuellog.ui.chart.MileageChart
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.util.PumpMileageStat
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.InsightsViewModel
import com.chandanshakya.fuellog.viewmodel.MileageTrend
import com.chandanshakya.fuellog.viewmodel.PumpInsightsViewModel
import com.chandanshakya.fuellog.viewmodel.PriceChartDataPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    vehicleId: Long,
    onNavigateToLog: () -> Unit,
    onNavigateToPumpDetail: (vehicleId: Long, pumpId: Long) -> Unit,
    viewModel: InsightsViewModel = hiltViewModel(),
    pumpInsightsViewModel: PumpInsightsViewModel = hiltViewModel()
) {
    val state by viewModel.insightsState.collectAsStateWithLifecycle()
    val pumpStats by pumpInsightsViewModel.pumpStats.collectAsStateWithLifecycle()
    val capacitySuggestion by viewModel.capacitySuggestion.collectAsStateWithLifecycle()
    val recentMileage by viewModel.recentMileage.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(state.vehicle?.let { "${it.name} Insights" } ?: "Insights") },
                navigationIcon = {
                    IconButton(onClick = onNavigateToLog) {
                        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        val vehicle = state.vehicle
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.spacingMd)
        ) {
            if (state.entries.isEmpty()) {
                EmptyState(
                    icon = painterResource(R.drawable.ic_analytics),
                    title = "No Data Available",
                    description = "Add fuel entries to see insights and trends"
                )
            } else {
                val distanceUnit = vehicle?.distanceUnit ?: DistanceUnit.KM
                val volumeUnit = vehicle?.volumeUnit ?: VolumeUnit.LITERS

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
                    currency = state.currency,
                    mileageDataPoints = state.mileageDataPoints,
                    priceDataPoints = state.priceDataPoints,
                    mileageTrend = state.mileageTrend,
                    pumpStats = pumpStats,
                    onPumpClick = { pumpId -> onNavigateToPumpDetail(vehicleId, pumpId) },
                    tankCapacity = vehicle?.tankCapacity,
                    capacitySuggestion = capacitySuggestion,
                    recentMileage = recentMileage,
                    onApplyCapacity = { viewModel.applySuggestedCapacity(it) },
                    modifier = Modifier.fillMaxSize()
                )
            }
        }
    }
}

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
    currency: String,
    mileageDataPoints: List<com.chandanshakya.fuellog.viewmodel.ChartDataPoint>,
    priceDataPoints: List<PriceChartDataPoint>,
    mileageTrend: MileageTrend,
    pumpStats: List<PumpMileageStat>,
    onPumpClick: (Long) -> Unit,
    tankCapacity: Double? = null,
    capacitySuggestion: com.chandanshakya.fuellog.util.CapacitySuggestion? = null,
    recentMileage: Double? = null,
    onApplyCapacity: (Double) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val efficiencyLabel = UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)

    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(bottom = Dimens.spacingXl),
        verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
    ) {
        if (mileageDataPoints.isNotEmpty()) {
            item {
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
                        Text(
                            text = "Mileage Trend",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(Dimens.spacingMd))

                        MileageChart(
                            dataPoints = mileageDataPoints,
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(Dimens.spacingSm))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            when (mileageTrend) {
                                MileageTrend.IMPROVING -> {
                                    Icon(painter = painterResource(R.drawable.ic_arrow_upward), contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                    Spacer(modifier = Modifier.width(Dimens.spacingXs))
                                    Text("Improving", color = MaterialTheme.colorScheme.tertiary, style = MaterialTheme.typography.bodySmall)
                                }
                                MileageTrend.DECLINING -> {
                                    Icon(painter = painterResource(R.drawable.ic_arrow_downward), contentDescription = null, tint = MaterialTheme.colorScheme.error)
                                    Spacer(modifier = Modifier.width(Dimens.spacingXs))
                                    Text("Declining", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                                }
                                MileageTrend.STABLE -> {
                                    Icon(painter = painterResource(R.drawable.ic_trending_flat), contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
                                    Spacer(modifier = Modifier.width(Dimens.spacingXs))
                                    Text("Stable", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                                }
                            }
                        }
                    }
                }
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                InfoCard(
                    label = "Average Mileage",
                    value = averageMileage?.let { "%.2f $efficiencyLabel".format(it) } ?: "N/A",
                    icon = painterResource(R.drawable.ic_speed),
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    label = "Best Mileage",
                    value = bestMileage?.let { "%.2f $efficiencyLabel".format(it) } ?: "N/A",
                    icon = painterResource(R.drawable.ic_arrow_upward),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                InfoCard(
                    label = "Worst Mileage",
                    value = worstMileage?.let { "%.2f $efficiencyLabel".format(it) } ?: "N/A",
                    icon = painterResource(R.drawable.ic_arrow_downward),
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    label = "Total Entries",
                    value = entriesCount.toString(),
                    icon = painterResource(R.drawable.ic_analytics),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                InfoCard(
                    label = "Total Distance",
                    value = "%.2f ${UnitConverter.getDistanceUnitLabel(distanceUnit)}".format(totalDistance),
                    icon = painterResource(R.drawable.ic_road),
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    label = "Total Fuel",
                    value = "%.2f ${UnitConverter.getVolumeUnitLabel(volumeUnit)}".format(totalFuel),
                    icon = painterResource(R.drawable.ic_local_gas_station),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                InfoCard(
                    label = "Total Cost",
                    value = CurrencyFormatter.formatCurrency(totalCost, currency),
                    icon = painterResource(R.drawable.ic_payments),
                    modifier = Modifier.weight(1f)
                )

                InfoCard(
                    label = "Cost per km",
                    value = costPerKm?.let { CurrencyFormatter.formatCurrency(it, currency) } ?: "N/A",
                    icon = painterResource(R.drawable.ic_speed),
                    modifier = Modifier.weight(1f)
                )
            }
        }

        if (priceDataPoints.isNotEmpty()) {
            item {
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
                        Text(
                            text = "Fuel Price Trend (${UnitConverter.getVolumeUnitLabel(volumeUnit)})",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(Dimens.spacingMd))

                        FuelPriceChart(
                            dataPoints = priceDataPoints,
                            currencyCode = currency,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }

        item {
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
                    Text(
                        text = "Mileage by Fuel Pump",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(Dimens.spacingMd))

                    if (pumpStats.isEmpty()) {
                        Text(
                            text = "No fuel pump data recorded yet",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        pumpStats.forEachIndexed { index, stat ->
                            if (index > 0) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = Dimens.spacingSm))
                            }
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onPumpClick(stat.pumpId ?: -1L) }
                                    .padding(vertical = Dimens.spacingSm),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = stat.pumpName,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = "${stat.fillCount} fill-up${if (stat.fillCount != 1) "s" else ""}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Text(
                                    text = "%.2f $efficiencyLabel".format(stat.avgMileage),
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Tank Info Card
        item {
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
                        Text(
                            text = "Tank Information",
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(Dimens.spacingMd))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Stored Capacity",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = tankCapacity?.let { "%.2f ${UnitConverter.getVolumeUnitLabel(volumeUnit)}".format(it) } ?: "Not set",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "Recent Mileage",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Text(
                                    text = recentMileage?.let { "%.2f $efficiencyLabel".format(it) } ?: "N/A",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                            }
                        }

                        if (capacitySuggestion != null) {
                            Spacer(modifier = Modifier.height(Dimens.spacingMd))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(Dimens.spacingSm))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "Suggested: ${"%.2f".format(capacitySuggestion!!.learnedCapacity)} ${UnitConverter.getVolumeUnitLabel(volumeUnit)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "Based on ${capacitySuggestion!!.basedOnFills} fills (${capacitySuggestion!!.confidence} confidence)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                AppButton(
                                    text = "Apply",
                                    onClick = { onApplyCapacity(capacitySuggestion!!.learnedCapacity) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
