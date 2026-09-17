package com.chandanshakya.fuellog.viewmodel

import android.Manifest
import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.chandanshakya.fuellog.FuelLogApplication
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import com.chandanshakya.fuellog.util.NearbyPump
import com.chandanshakya.fuellog.util.OverpassFuelPumps
import com.chandanshakya.fuellog.util.UnitConverter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class NearbyUiState(
    val loading: Boolean = false,
    val error: String? = null,
    val location: Pair<Double, Double>? = null,
    val pumps: List<NearbyPump> = emptyList(),
    val averageMileage: Double? = null,
    val efficiencyLabel: String = ""
)

class NearbyPumpsViewModel(
    private val app: Application
) : ViewModel() {

    companion object {
        val Factory: ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val application = this[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]
                NearbyPumpsViewModel(application as FuelLogApplication)
            }
        }
    }

    private val _state = MutableStateFlow(NearbyUiState())
    val state: StateFlow<NearbyUiState> = _state

    private var locationManager: LocationManager? = null
    private var locationListener: LocationListener? = null

    fun updateAverage(mileage: Double?, distanceUnit: DistanceUnit, volumeUnit: VolumeUnit) {
        _state.value = _state.value.copy(
            averageMileage = mileage,
            efficiencyLabel = UnitConverter.getEfficiencyLabel(distanceUnit, volumeUnit)
        )
    }

    fun onPermissionResult(granted: Boolean) {
        if (granted) {
            startLocationAndLoad()
        } else {
            _state.value = _state.value.copy(loading = false, error = "Location permission denied")
        }
    }

    fun refresh(context: Context) {
        if (hasLocationPermission(context)) startLocationAndLoad()
        else _state.value = _state.value.copy(
            loading = false,
            error = "Location permission required to find nearby pumps"
        )
    }

    fun hasLocationPermission(context: Context): Boolean =
        ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED

    @Suppress("MissingPermission")
    private fun startLocationAndLoad() {
        val lm = app.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationManager = lm
        _state.value = _state.value.copy(loading = true, error = null)

        val provider = when {
            lm.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> null
        }

        val last = provider?.let { runCatching { lm.getLastKnownLocation(it) }.getOrNull() }
            ?: lm.getProviders(true).firstNotNullOfOrNull {
                runCatching { lm.getLastKnownLocation(it) }.getOrNull()
            }

        if (last != null) {
            loadPumps(last.latitude, last.longitude)
            return
        }

        if (provider == null) {
            _state.value = _state.value.copy(
                loading = false,
                error = "Location is turned off. Enable GPS or network location."
            )
            return
        }

        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                loadPumps(location.latitude, location.longitude)
                stopLocationUpdates()
            }

            @Deprecated("Deprecated in Java")
            override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) = Unit
            override fun onProviderEnabled(provider: String) = Unit
            override fun onProviderDisabled(provider: String) = Unit
        }
        locationListener = listener
        runCatching {
            lm.requestSingleUpdate(provider, listener, Looper.getMainLooper())
        }.onFailure {
            _state.value = _state.value.copy(
                loading = false,
                error = "Could not get location. Check that location is on."
            )
        }
    }

    private fun stopLocationUpdates() {
        val listener = locationListener ?: return
        runCatching { locationManager?.removeUpdates(listener) }
        locationListener = null
    }

    private fun loadPumps(lat: Double, lon: Double) {
        viewModelScope.launch {
            _state.value = _state.value.copy(loading = true, location = lat to lon, error = null)
            try {
                val pumps = withContext(Dispatchers.IO) {
                    OverpassFuelPumps.fetchNearby(lat, lon)
                }
                _state.value = _state.value.copy(loading = false, pumps = pumps)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    loading = false,
                    error = e.message ?: "Failed to load nearby pumps"
                )
            }
        }
    }

    override fun onCleared() {
        stopLocationUpdates()
        super.onCleared()
    }
}
