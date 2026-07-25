package com.chandanshakya.fuellog.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.remember
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
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.chart.LineChart
import com.chandanshakya.fuellog.ui.components.InfoCard
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
    pumpInsightsViewModel: PumpInsightsViewModel = viewModel(factory = PumpInsightsViewModel.factory(vehicleId))
) {
    val pumpStats by pumpInsightsViewModel.pumpStats.collectAsStateWithLifecycle()
    val currency by pumpInsightsViewModel.currency.collectAsStateWithLifecycle()
    val distanceUnit by pumpInsightsViewModel.distanceUnit.collectAsStateWithLifecycle()
    val volumeUnit by pumpInsightsViewModel.volumeUnit.collectAsStateWithLifecycle()
    val pumpDetail = remember(pumpId, pumpStats) {
        pumpInsightsViewModel.getPumpDetail(pumpId)
    }
    val allPumpEntries by pumpInsightsViewModel.getAllEntriesForPump(pumpId).collectAsStateWithLifecycle()

    val detailMap = remember(pumpDetail) {
        pumpDetail.associateBy { it.entryId }
    }

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
                    PumpStatHeader(stat = s, distanceUnit = distanceUnit, volumeUnit = volumeUnit)
                }
            }

            item {
                PumpTrendChart(pumpDetail = pumpDetail)
            }

            if (allPumpEntries.isNotEmpty()) {
                val sortedEntries = allPumpEntries.sortedByDescending { it.odometer }
                item {
                    Text(
                        text = "Fill-up History (${allPumpEntries.size})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                items(items = sortedEntries, key = { it.id }) { entry ->
                    val detail = detailMap[entry.id]
                    PumpEntryCard(
                        entry = entry,
                        mileage = detail?.mileage,
                        distanceSinceLast = detail?.distanceSinceLastFill,
                        averageMileage = stat?.avgMileage,
                        currency = currency,
                        distanceUnit = distanceUnit,
                        volumeUnit = volumeUnit,
                        modifier = Modifier.animateItem()
                    )
                }
            }
        }
    }
}

@Composable
private fun PumpStatHeader(stat: PumpMileageStat, distanceUnit: DistanceUnit, volumeUnit: VolumeUnit) {
    val efficiencyLabel = UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)
    Column(verticalArrangement = Arrangement.spacedBy(Dimens.spacingMd)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
        ) {
            InfoCard(
                label = "Avg Mileage",
                value = "%.2f $efficiencyLabel".format(stat.avgMileage),
                icon = painterResource(R.drawable.ic_avg_pace),
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                label = "Fill-ups",
                value = stat.fillCount.toString(),
                icon = painterResource(R.drawable.ic_payments),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Dimens.spacingMd)
        ) {
            InfoCard(
                label = "Best Mileage",
                value = "%.2f $efficiencyLabel".format(stat.bestMileage),
                icon = painterResource(R.drawable.ic_arrow_upward),
                modifier = Modifier.weight(1f)
            )
            InfoCard(
                label = "Worst Mileage",
                value = "%.2f $efficiencyLabel".format(stat.worstMileage),
                icon = painterResource(R.drawable.ic_arrow_downward),
                modifier = Modifier.weight(1f)
            )
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun PumpEntryCard(
    entry: com.chandanshakya.fuellog.data.model.FuelEntry,
    mileage: Double? = null,
    distanceSinceLast: Double? = null,
    averageMileage: Double? = null,
    currency: String,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    modifier: Modifier = Modifier
) {
    val fuelIconShape = RoundedCornerShape(6.dp)
    val efficiencyLabel = UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)
    val distanceLabel = UnitConverter.getDistanceUnitLabel(distanceUnit)
    val volumeLabel = UnitConverter.getVolumeUnitLabel(volumeUnit)

    Card(
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
                        text = "${"%.0f".format(entry.odometer)} $distanceLabel",
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
                        text = efficiencyLabel,
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
                    distanceSinceLast?.let { d ->
                        Text(
                            text = "${"%.0f".format(d)} $distanceLabel since last",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Right: volume + cost
                Text(
                    text = "${"%.2f".format(entry.fuelVolume)} $volumeLabel",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = CurrencyFormatter.formatCurrency(entry.fuelCost, currency),
                    style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
