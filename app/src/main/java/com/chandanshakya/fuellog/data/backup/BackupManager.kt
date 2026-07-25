package com.chandanshakya.fuellog.data.backup

import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.FuelPumpDao
import com.chandanshakya.fuellog.data.db.OdometerReadingDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.OdometerReading
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VehicleType
import com.chandanshakya.fuellog.data.model.VolumeUnit
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate

class BackupManager(
    private val vehicleDao: VehicleDao,
    private val fuelEntryDao: FuelEntryDao,
    private val fuelPumpDao: FuelPumpDao,
    private val odometerReadingDao: OdometerReadingDao,
    private val userSettingsDao: UserSettingsDao
) {
    suspend fun exportToStream(output: OutputStream) {
        val fuelPumps = fuelPumpDao.getAll().first()
        val vehicles = vehicleDao.getAll().first()
        val fuelEntries = fuelEntryDao.getAll()
        val odometerReadings = odometerReadingDao.getAll()
        val userSettings = userSettingsDao.getSettings().first()

        val root = JSONObject().apply {
            put("version", 1)
            put("exportDate", Instant.now().toString())
            put("fuelPumps", JSONArray().apply {
                fuelPumps.forEach { pump ->
                    put(JSONObject().apply {
                        put("id", pump.id)
                        put("name", pump.name)
                    })
                }
            })
            put("vehicles", JSONArray().apply {
                vehicles.forEach { v ->
                    put(JSONObject().apply {
                        put("id", v.id)
                        put("name", v.name)
                        put("vehicleType", v.vehicleType.name)
                        put("distanceUnit", v.distanceUnit.name)
                        put("volumeUnit", v.volumeUnit.name)
                        put("createdAt", v.createdAt)
                        put("tankCapacity", v.tankCapacity ?: JSONObject.NULL)
                    })
                }
            })
            put("fuelEntries", JSONArray().apply {
                fuelEntries.forEach { e ->
                    put(JSONObject().apply {
                        put("id", e.id)
                        put("vehicleId", e.vehicleId)
                        put("date", e.date.toString())
                        put("odometer", e.odometer)
                        put("fuelVolume", e.fuelVolume)
                        put("fuelCost", e.fuelCost)
                        put("fuelPumpId", e.fuelPumpId ?: JSONObject.NULL)
                        put("isFullTank", e.isFullTank)
                    })
                }
            })
            put("odometerReadings", JSONArray().apply {
                odometerReadings.forEach { r ->
                    put(JSONObject().apply {
                        put("id", r.id)
                        put("vehicleId", r.vehicleId)
                        put("date", r.date.toString())
                        put("odometer", r.odometer)
                    })
                }
            })
            put("userSettings", if (userSettings != null) {
                JSONObject().apply {
                    put("id", userSettings.id)
                    put("defaultCurrency", userSettings.defaultCurrency)
                    put("defaultDistanceUnit", userSettings.defaultDistanceUnit.name)
                    put("defaultVolumeUnit", userSettings.defaultVolumeUnit.name)
                }
            } else JSONObject())
        }
        output.bufferedWriter().use { it.write(root.toString(2)) }
    }

    suspend fun importFromStream(input: InputStream) {
        val text = input.bufferedReader().use { it.readText() }
        val root = JSONObject(text)

        // Clear in FK dependency order
        fuelEntryDao.deleteAll()
        odometerReadingDao.deleteAll()
        userSettingsDao.deleteAll()
        fuelPumpDao.deleteAll()
        vehicleDao.deleteAll()

        // Insert in dependency order
        val fuelPumpsArray = root.getJSONArray("fuelPumps")
        for (i in 0 until fuelPumpsArray.length()) {
            val obj = fuelPumpsArray.getJSONObject(i)
            fuelPumpDao.insert(FuelPump(id = obj.getLong("id"), name = obj.getString("name")))
        }

        val vehiclesArray = root.getJSONArray("vehicles")
        for (i in 0 until vehiclesArray.length()) {
            val obj = vehiclesArray.getJSONObject(i)
            vehicleDao.insert(jsonToVehicle(obj))
        }

        val entriesArray = root.getJSONArray("fuelEntries")
        for (i in 0 until entriesArray.length()) {
            val obj = entriesArray.getJSONObject(i)
            fuelEntryDao.insert(jsonToFuelEntry(obj))
        }

        val readingsArray = root.getJSONArray("odometerReadings")
        for (i in 0 until readingsArray.length()) {
            val obj = readingsArray.getJSONObject(i)
            odometerReadingDao.insert(jsonToOdometerReading(obj))
        }

        if (root.has("userSettings") && !root.isNull("userSettings")) {
            val settingsObj = root.getJSONObject("userSettings")
            if (settingsObj.length() > 0) {
                userSettingsDao.insert(jsonToUserSettings(settingsObj))
            }
        }
    }

    suspend fun clearAll() {
        fuelEntryDao.deleteAll()
        odometerReadingDao.deleteAll()
        userSettingsDao.deleteAll()
        fuelPumpDao.deleteAll()
        vehicleDao.deleteAll()
    }

    private fun jsonToVehicle(obj: JSONObject) = Vehicle(
        id = obj.getLong("id"),
        name = obj.getString("name"),
        vehicleType = VehicleType.valueOf(obj.getString("vehicleType")),
        distanceUnit = DistanceUnit.valueOf(obj.getString("distanceUnit")),
        volumeUnit = VolumeUnit.valueOf(obj.getString("volumeUnit")),
        createdAt = obj.getLong("createdAt"),
        tankCapacity = if (obj.isNull("tankCapacity")) null else obj.getDouble("tankCapacity")
    )

    private fun jsonToFuelEntry(obj: JSONObject) = FuelEntry(
        id = obj.getLong("id"),
        vehicleId = obj.getLong("vehicleId"),
        date = LocalDate.parse(obj.getString("date")),
        odometer = obj.getDouble("odometer"),
        fuelVolume = obj.getDouble("fuelVolume"),
        fuelCost = obj.getDouble("fuelCost"),
        fuelPumpId = if (obj.isNull("fuelPumpId")) null else obj.getLong("fuelPumpId"),
        isFullTank = obj.getBoolean("isFullTank")
    )

    private fun jsonToOdometerReading(obj: JSONObject) = OdometerReading(
        id = obj.getLong("id"),
        vehicleId = obj.getLong("vehicleId"),
        date = LocalDate.parse(obj.getString("date")),
        odometer = obj.getDouble("odometer")
    )

    private fun jsonToUserSettings(obj: JSONObject) = UserSettings(
        id = obj.getLong("id"),
        defaultCurrency = obj.getString("defaultCurrency"),
        defaultDistanceUnit = DistanceUnit.valueOf(obj.getString("defaultDistanceUnit")),
        defaultVolumeUnit = VolumeUnit.valueOf(obj.getString("defaultVolumeUnit"))
    )
}
