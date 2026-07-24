package com.chandanshakya.fuellog.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.chart.LineChart
import com.chandanshakya.fuellog.ui.components.AppBadge
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.CurrencyFormatter
import com.chandanshakya.fuellog.util.PumpFillDetail
import com.chandanshakya.fuellog.util.PumpMileageStat
import com.chandanshakya.fuellog.util.UnitConverter
import com.chandanshakya.fuellog.viewmodel.PumpInsightsViewModel
import java.time.format.DateTimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PumpDetailScreen(
    vehicleId: Long,
    pumpId: Long?,
    onNavigateBack: () -> Unit,
    pumpInsightsViewModel: PumpInsightsViewModel = hiltViewModel()
) {
    val pumpStats by pumpInsightsViewModel.pumpStats.collectAsState()
    val currency by pumpInsightsViewModel.currency.collectAsState()
    val pumpDetail = remember(pumpId, pumpStats) {
        pumpInsightsViewModel.getPumpDetail(pumpId)
    }
    val allPumpEntries by pumpInsightsViewModel.getAllEntriesForPump(pumpId).collectAsState()

    val stat = remember(pumpId, pumpStats) {
        pumpStats.find { it.pumpId == pumpId }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stat?.pumpName ?: "Pump Detail") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(Dimens.spacingMd),
            contentPadding = PaddingValues(bottom = Dimens.spacingXl),
            verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
        ) {
            stat?.let { s ->
                item {
                    PumpStatHeader(stat = s)
                }
            }

            item {
                PumpTrendChart(pumpDetail = pumpDetail)
            }

            if (pumpDetail.isNotEmpty()) {
                item {
                    Text(
                        text = "Fill-up History",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                val sortedDesc = pumpDetail.sortedByDescending { it.date }
                items(items = sortedDesc, key = { it.entryId }) { detail ->
                    PumpFillDetailCard(detail = detail, currency = currency)
                }
            }

            if (allPumpEntries.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(Dimens.spacingSm))
                    Text(
                        text = "All Entries at This Pump (${allPumpEntries.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                val sortedEntries = allPumpEntries.sortedByDescending { it.date }
                items(items = sortedEntries, key = { it.id }) { entry ->
                    PumpEntryCard(entry = entry, currency = currency)
                }
            }
        }
    }
}

@Composable
private fun PumpStatHeader(stat: PumpMileageStat) {
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
                text = stat.pumpName,
                style = MaterialTheme.typography.headlineSmall
            )
            Spacer(modifier = Modifier.height(Dimens.spacingSm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                InfoCard(
                    label = "Avg Mileage",
                    value = "%.2f km/L".format(stat.avgMileage),
                    icon = painterResource(R.drawable.ic_speed),
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    label = "Fill-ups",
                    value = stat.fillCount.toString(),
                    icon = painterResource(R.drawable.ic_local_gas_station),
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.height(Dimens.spacingSm))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                InfoCard(
                    label = "Best Mileage",
                    value = "%.2f km/L".format(stat.bestMileage),
                    icon = painterResource(R.drawable.ic_arrow_upward),
                    modifier = Modifier.weight(1f)
                )
                InfoCard(
                    label = "Worst Mileage",
                    value = "%.2f km/L".format(stat.worstMileage),
                    icon = painterResource(R.drawable.ic_arrow_downward),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun PumpTrendChart(pumpDetail: List<PumpFillDetail>) {
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

            if (pumpDetail.size < 2) {
                Text(
                    text = "Not enough fill-ups yet to show a trend",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(Dimens.spacingMd)
                )
            } else {
                val values = remember(pumpDetail) { pumpDetail.map { it.mileage } }
                val dates = remember(pumpDetail) { pumpDetail.map { it.date } }
                LineChart(
                    values = values,
                    dates = dates,
                    valueLabel = { "%.2f".format(it) },
                    lineColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Composable
private fun PumpFillDetailCard(detail: PumpFillDetail, currency: String) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

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
                    painter = painterResource(R.drawable.ic_local_gas_station),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconMedium),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(Dimens.spacingMd))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = detail.date.format(dateFormatter),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "Odometer: ${"%.2f".format(detail.odometer)} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                AppBadge(text = "%.2f km/L".format(detail.mileage))
                AppBadge(text = "${"%.2f".format(detail.fuelVolume)} L")
                AppBadge(text = CurrencyFormatter.formatCurrency(detail.fuelCost, currency))
                AppBadge(text = "${"%.0f".format(detail.distanceSinceLastFill)} km since last")
            }
        }
    }
}

@Composable
private fun PumpEntryCard(
    entry: com.chandanshakya.fuellog.data.model.FuelEntry,
    currency: String
) {
    val dateFormatter = remember { DateTimeFormatter.ofPattern("dd MMM yyyy") }

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
                    painter = painterResource(R.drawable.ic_local_gas_station),
                    contentDescription = null,
                    modifier = Modifier.size(Dimens.iconMedium),
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.size(Dimens.spacingMd))
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = entry.date.format(dateFormatter),
                            style = MaterialTheme.typography.titleMedium
                        )
                        if (entry.isFullTank) {
                            Spacer(modifier = Modifier.width(Dimens.spacingSm))
                            AppBadge(text = "Full Tank")
                        }
                    }
                    Text(
                        text = "Odometer: ${"%.2f".format(entry.odometer)} km",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(Dimens.spacingSm))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
            ) {
                AppBadge(text = "${"%.2f".format(entry.fuelVolume)} L")
                AppBadge(text = CurrencyFormatter.formatCurrency(entry.fuelCost, currency))
            }
        }
    }
}
