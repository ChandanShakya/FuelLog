package com.chandanshakya.fuellog.data.model

import kotlinx.serialization.Serializable
import java.time.LocalDate

/**
 * Data structure for export/import operations.
 * 
 * @param version Export format version (currently 1)
 * @param vehicles List of all vehicles
 * @param fuelEntries List of all fuel entries
 * @param settings Global user settings
 */
@Serializable
data class ExportData(
    val version: Int = 1,
    val vehicles: List<ExportVehicle> = emptyList(),
    val fuelEntries: List<ExportFuelEntry> = emptyList(),
    val settings: ExportUserSettings = ExportUserSettings()
)

/**
 * Serializable version of Vehicle for export.
 */
@Serializable
data class ExportVehicle(
    val id: Long,
    val name: String,
    val defaultCurrency: String,
    val distanceUnit: String,
    val volumeUnit: String,
    val createdAt: String
)

/**
 * Serializable version of FuelEntry for export.
 */
@Serializable
data class ExportFuelEntry(
    val id: Long,
    val vehicleId: Long,
    val date: String,
    val odometer: Double,
    val fuelVolume: Double,
    val fuelCost: Double,
    val isFullTank: Boolean,
    val notes: String?,
    val time: String? = null
)

/**
 * Serializable version of UserSettings for export.
 */
@Serializable
data class ExportUserSettings(
    val defaultCurrency: String = "USD",
    val defaultDistanceUnit: String = "KM",
    val defaultVolumeUnit: String = "LITERS"
)

/**
 * Extension functions for converting between domain and export models.
 */
fun Vehicle.toExport(): ExportVehicle = ExportVehicle(
    id = id,
    name = name,
    defaultCurrency = defaultCurrency,
    distanceUnit = distanceUnit.name,
    volumeUnit = volumeUnit.name,
    createdAt = createdAt.toString()
)

fun FuelEntry.toExport(): ExportFuelEntry = ExportFuelEntry(
    id = id,
    vehicleId = vehicleId,
    date = date.toString(),
    odometer = odometer,
    fuelVolume = fuelVolume,
    fuelCost = fuelCost,
    isFullTank = isFullTank,
    notes = notes,
    time = time.toString()
)

fun UserSettings.toExport(): ExportUserSettings = ExportUserSettings(
    defaultCurrency = defaultCurrency,
    defaultDistanceUnit = defaultDistanceUnit.name,
    defaultVolumeUnit = defaultVolumeUnit.name
)

fun ExportVehicle.toVehicle(): Vehicle = Vehicle(
    id = id,
    name = name,
    defaultCurrency = defaultCurrency,
    distanceUnit = DistanceUnit.valueOf(distanceUnit),
    volumeUnit = VolumeUnit.valueOf(volumeUnit),
    createdAt = LocalDate.parse(createdAt)
)

fun ExportFuelEntry.toFuelEntry(): FuelEntry = FuelEntry(
    id = id,
    vehicleId = vehicleId,
    date = LocalDate.parse(date),
    odometer = odometer,
    fuelVolume = fuelVolume,
    fuelCost = fuelCost,
    isFullTank = isFullTank,
    notes = notes,
    time = time?.let { java.time.LocalTime.parse(it) } ?: java.time.LocalTime.now()
)

fun ExportUserSettings.toUserSettings(): UserSettings = UserSettings(
    defaultCurrency = defaultCurrency,
    defaultDistanceUnit = DistanceUnit.valueOf(defaultDistanceUnit),
    defaultVolumeUnit = VolumeUnit.valueOf(defaultVolumeUnit)
)
