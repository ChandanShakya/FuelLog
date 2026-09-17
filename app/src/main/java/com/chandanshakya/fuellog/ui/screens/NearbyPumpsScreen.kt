package com.chandanshakya.fuellog.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.chandanshakya.fuellog.R
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.ui.components.AppButton
import com.chandanshakya.fuellog.ui.components.EmptyState
import com.chandanshakya.fuellog.ui.theme.Dimens
import com.chandanshakya.fuellog.util.GeoMath
import com.chandanshakya.fuellog.util.NearbyPump
import com.chandanshakya.fuellog.viewmodel.NearbyPumpsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NearbyPumpsScreen(
    onNavigateBack: () -> Unit,
    averageMileage: Double? = null,
    distanceUnit: DistanceUnit = DistanceUnit.KM,
    volumeUnit: VolumeUnit = VolumeUnit.LITERS,
    viewModel: NearbyPumpsViewModel = viewModel(factory = NearbyPumpsViewModel.Factory)
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(averageMileage, distanceUnit, volumeUnit) {
        viewModel.updateAverage(averageMileage, distanceUnit, volumeUnit)
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { grants ->
        val granted = grants[android.Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                grants[android.Manifest.permission.ACCESS_COARSE_LOCATION] == true
        viewModel.onPermissionResult(granted)
    }

    LaunchedEffect(Unit) {
        if (viewModel.hasLocationPermission(context)) {
            viewModel.refresh(context)
        } else {
            permissionLauncher.launch(
                arrayOf(
                    android.Manifest.permission.ACCESS_COARSE_LOCATION,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                )
            )
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nearby Fuel Pumps") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(painter = painterResource(R.drawable.ic_arrow_back), contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(Dimens.spacingMd)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                state.averageMileage?.let { mileage ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(Dimens.spacingMd)) {
                            Text("Your average mileage", style = MaterialTheme.typography.labelMedium)
                            Text(
                                text = "${"%.2f".format(mileage)} ${state.efficiencyLabel}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "From your logged fill-ups",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(Dimens.spacingMd))
                }

                when {
                    state.loading -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text("Finding pumps near you…", style = MaterialTheme.typography.bodyLarge)
                        }
                    }

                    state.error != null -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = state.error ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.error
                            )
                            Spacer(modifier = Modifier.height(Dimens.spacingMd))
                            AppButton(
                                text = "Try again",
                                onClick = {
                                    if (viewModel.hasLocationPermission(context)) viewModel.refresh(context)
                                    else permissionLauncher.launch(
                                        arrayOf(
                                            android.Manifest.permission.ACCESS_COARSE_LOCATION,
                                            android.Manifest.permission.ACCESS_FINE_LOCATION
                                        )
                                    )
                                }
                            )
                        }
                    }

                    state.pumps.isEmpty() -> {
                        EmptyState(
                            icon = painterResource(R.drawable.ic_local_gas_station),
                            title = "No pumps found",
                            description = "No fuel stations in OSM within 7 km. Try again when you have a better signal."
                        )
                    }

                    else -> {
                        LazyColumn(
                            contentPadding = PaddingValues(bottom = 24.dp),
                            verticalArrangement = Arrangement.spacedBy(Dimens.spacingSm)
                        ) {
                            items(state.pumps, key = { it.osmId }) { pump ->
                                NearbyPumpRow(pump = pump)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun NearbyPumpRow(pump: NearbyPump) {
    val context = LocalContext.current
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Dimens.spacingMd),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(pump.name, style = MaterialTheme.typography.titleMedium, maxLines = 2)
                val subtitle = listOfNotNull(
                    pump.brand,
                    pump.operator,
                    GeoMath.formatDistance(pump.distanceMeters)
                ).joinToString(" · ")
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            AppButton(
                text = "Go",
                onClick = {
                    val uri = Uri.parse("geo:${pump.lat},${pump.lon}?q=${pump.lat},${pump.lon}(${Uri.encode(pump.name)})")
                    val intent = Intent(Intent.ACTION_VIEW, uri)
                    runCatching { context.startActivity(intent) }
                }
            )
        }
    }
}
