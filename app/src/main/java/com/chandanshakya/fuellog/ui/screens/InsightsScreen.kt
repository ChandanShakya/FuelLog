package com.chandanshakya.fuellog.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
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
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.InsightsViewModel
import com.chandanshakya.fuellog.viewmodel.MileageTrend
import com.chandanshakya.fuellog.viewmodel.PriceChartDataPoint

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InsightsScreen(
    vehicleId: Long,
    onNavigateToLog: () -> Unit,
    onNavigateToVehicles: () -> Unit,
    onNavigateToSettings: () -> Unit = {},
    viewModel: InsightsViewModel = hiltViewModel()
) {
    val state by viewModel.insightsState.collectAsStateWithLifecycle()

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
                                    Icon(painter = painterResource(R.drawable.ic_arrow_upward), contentDescription = null, tint = Color.Green)
                                    Spacer(modifier = Modifier.width(Dimens.spacingXs))
                                    Text("Improving", color = Color.Green, style = MaterialTheme.typography.bodySmall)
                                }
                                MileageTrend.DECLINING -> {
                                    Icon(painter = painterResource(R.drawable.ic_arrow_downward), contentDescription = null, tint = Color.Red)
                                    Spacer(modifier = Modifier.width(Dimens.spacingXs))
                                    Text("Declining", color = Color.Red, style = MaterialTheme.typography.bodySmall)
                                }
                                MileageTrend.STABLE -> {
                                    Icon(painter = painterResource(R.drawable.ic_trending_flat), contentDescription = null, tint = Color.Gray)
                                    Spacer(modifier = Modifier.width(Dimens.spacingXs))
                                    Text("Stable", color = Color.Gray, style = MaterialTheme.typography.bodySmall)
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
                InsightCard(
                    label = "Average Mileage",
                    value = averageMileage?.let { "%.1f $efficiencyLabel".format(it) } ?: "N/A",
                    icon = painterResource(R.drawable.ic_speed),
                    modifier = Modifier.weight(1f)
                )

                InsightCard(
                    label = "Best Mileage",
                    value = bestMileage?.let { "%.1f $efficiencyLabel".format(it) } ?: "N/A",
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
                InsightCard(
                    label = "Worst Mileage",
                    value = worstMileage?.let { "%.1f $efficiencyLabel".format(it) } ?: "N/A",
                    icon = painterResource(R.drawable.ic_arrow_downward),
                    modifier = Modifier.weight(1f)
                )

                InsightCard(
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
                InsightCard(
                    label = "Total Distance",
                    value = "%.0f ${UnitConverter.getDistanceUnitLabel(distanceUnit)}".format(totalDistance),
                    icon = painterResource(R.drawable.ic_road),
                    modifier = Modifier.weight(1f)
                )

                InsightCard(
                    label = "Total Fuel",
                    value = "%.1f ${UnitConverter.getVolumeUnitLabel(volumeUnit)}".format(totalFuel),
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
                InsightCard(
                    label = "Total Cost",
                    value = CurrencyFormatter.formatCurrency(totalCost, currency),
                    icon = painterResource(R.drawable.ic_payments),
                    modifier = Modifier.weight(1f)
                )

                InsightCard(
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
    }
}

@Composable
fun InsightCard(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.painter.Painter,
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
                .padding(horizontal = Dimens.spacingSm, vertical = Dimens.spacingMd),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                painter = icon,
                contentDescription = null,
                modifier = Modifier.size(Dimens.iconMedium),
                tint = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            androidx.compose.material3.Text(
                text = value,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = if (value.length > 8) MaterialTheme.typography.titleSmall.fontSize else MaterialTheme.typography.titleMedium.fontSize
                ),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )

            androidx.compose.material3.Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
            )
        }
    }
}
