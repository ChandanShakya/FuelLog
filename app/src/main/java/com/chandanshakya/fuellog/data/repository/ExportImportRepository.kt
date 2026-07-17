package com.chandanshakya.fuellog.data.repository

import android.content.Context
import android.net.Uri
import com.chandanshakya.fuellog.data.model.ExportData
import com.chandanshakya.fuellog.data.model.ExportFuelEntry
import com.chandanshakya.fuellog.data.model.ExportUserSettings
import com.chandanshakya.fuellog.data.model.ExportVehicle
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.toExport
import com.chandanshakya.fuellog.data.model.toFuelEntry
import com.chandanshakya.fuellog.data.model.toUserSettings
import com.chandanshakya.fuellog.data.model.toVehicle
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.InputStream
import java.io.OutputStream

/**
 * Repository for export and import operations.
 * 
 * Uses Storage Access Framework (SAF) for file operations without MANAGE_EXTERNAL_STORAGE.
 */
class ExportImportRepository(
    private val vehicleRepository: VehicleRepository,
    private val fuelRepository: FuelRepository,
    private val settingsRepository: SettingsRepository
) {
    private val json = Json { 
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    /**
     * Export all data to a JSON file.
     * 
     * @param context Android context
     * @param uri URI to write the export file to
     * @return true if export was successful
     */
    suspend fun exportData(context: Context, uri: Uri): Boolean {
        return try {
            val vehicles = vehicleRepository.getAll().firstOrNull() ?: emptyList()
            val allFuelEntries = mutableListOf<FuelEntry>()
            
            // Get all fuel entries for all vehicles
            for (vehicle in vehicles) {
                val entries = fuelRepository.getAllByVehicleSuspend(vehicle.id)
                allFuelEntries.addAll(entries)
            }
            
            val settings = settingsRepository.getSettingsSuspend()
            
            val exportData = ExportData(
                version = 1,
                vehicles = vehicles.map { it.toExport() },
                fuelEntries = allFuelEntries.map { it.toExport() },
                settings = settings?.toExport() ?: ExportUserSettings()
            )
            
            val jsonString = json.encodeToString(exportData)
            
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                outputStream.write(jsonString.toByteArray())
            }
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Import data from a JSON file.
     * 
     * @param context Android context
     * @param uri URI to read the import file from
     * @return true if import was successful
     */
    suspend fun importData(context: Context, uri: Uri): Boolean {
        return try {
            val jsonString = context.contentResolver.openInputStream(uri)?.use { inputStream ->
                inputStream.bufferedReader().use { it.readText() }
            } ?: return false
            
            val exportData = json.decodeFromString<ExportData>(jsonString)
            
            if (exportData.version != 1) {
                return false // Unsupported version
            }
            
            // Import vehicles
            for (exportVehicle in exportData.vehicles) {
                val vehicle = exportVehicle.toVehicle()
                vehicleRepository.insert(vehicle)
            }
            
            // Import fuel entries
            for (exportEntry in exportData.fuelEntries) {
                val entry = exportEntry.toFuelEntry()
                fuelRepository.insert(entry)
            }
            
            // Import settings
            val settings = exportData.settings.toUserSettings()
            settingsRepository.saveSettings(settings)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Get export data as JSON string (for testing/debugging).
     */
    suspend fun getExportDataJson(): String {
        val vehicles = vehicleRepository.getAll().firstOrNull() ?: emptyList()
        val allFuelEntries = mutableListOf<FuelEntry>()
        
        for (vehicle in vehicles) {
            val entries = fuelRepository.getAllByVehicleSuspend(vehicle.id)
            allFuelEntries.addAll(entries)
        }
        
        val settings = settingsRepository.getSettingsSuspend()
        
        val exportData = ExportData(
            version = 1,
            vehicles = vehicles.map { it.toExport() },
            fuelEntries = allFuelEntries.map { it.toExport() },
            settings = settings?.toExport() ?: ExportUserSettings()
        )
        
        return json.encodeToString(exportData)
    }
}
