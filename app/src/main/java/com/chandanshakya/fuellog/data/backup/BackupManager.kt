package com.chandanshakya.fuellog.data.backup

import androidx.room.withTransaction
import com.chandanshakya.fuellog.data.db.AppDatabase
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
import org.json.JSONException
import org.json.JSONObject
import java.io.InputStream
import java.io.OutputStream
import java.time.Instant
import java.time.LocalDate

class BackupException(message: String, cause: Throwable? = null) : Exception(message, cause)

class BackupManager(
    private val database: AppDatabase,
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
            put("version", BACKUP_VERSION)
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

    /**
     * Import is fully transactional: JSON is parsed and validated first, then
     * clear+insert runs in a single Room transaction. A failed import leaves
     * existing data untouched.
     */
    suspend fun importFromStream(input: InputStream) {
        val text = input.bufferedReader().use { it.readText() }
        val payload = try {
            parseBackup(text)
        } catch (e: JSONException) {
            throw BackupException("Invalid backup file: ${e.message}", e)
        } catch (e: IllegalArgumentException) {
            throw BackupException(e.message ?: "Invalid backup file", e)
        }

        database.withTransaction {
            fuelEntryDao.deleteAll()
            odometerReadingDao.deleteAll()
            userSettingsDao.deleteAll()
            fuelPumpDao.deleteAll()
            vehicleDao.deleteAll()

            payload.pumps.forEach { fuelPumpDao.insert(it) }
            payload.vehicles.forEach { vehicleDao.insert(it) }
            payload.entries.forEach { fuelEntryDao.insert(it) }
            payload.readings.forEach { odometerReadingDao.insert(it) }
            payload.settings?.let { userSettingsDao.insert(it) }
        }
    }

    suspend fun clearAll() {
        database.withTransaction {
            fuelEntryDao.deleteAll()
            odometerReadingDao.deleteAll()
            userSettingsDao.deleteAll()
            fuelPumpDao.deleteAll()
            vehicleDao.deleteAll()
        }
    }

    private data class ParsedBackup(
        val pumps: List<FuelPump>,
        val vehicles: List<Vehicle>,
        val entries: List<FuelEntry>,
        val readings: List<OdometerReading>,
        val settings: UserSettings?
    )

    private fun parseBackup(text: String): ParsedBackup {
        val root = JSONObject(text)
        val version = root.optInt("version", 1)
        if (version > BACKUP_VERSION) {
            throw IllegalArgumentException("Backup version $version is newer than supported $BACKUP_VERSION")
        }

        val pumps = root.optJSONArray("fuelPumps").orEmpty().map { obj ->
            FuelPump(
                id = obj.getLong("id"),
                name = obj.getString("name")
            )
        }

        val vehicles = root.optJSONArray("vehicles").orEmpty().map { obj ->
            Vehicle(
                id = obj.getLong("id"),
                name = obj.getString("name"),
                vehicleType = parseEnum(VehicleType::class.java, obj.getString("vehicleType"), "vehicleType"),
                distanceUnit = parseEnum(DistanceUnit::class.java, obj.getString("distanceUnit"), "distanceUnit"),
                volumeUnit = parseEnum(VolumeUnit::class.java, obj.getString("volumeUnit"), "volumeUnit"),
                createdAt = obj.getLong("createdAt"),
                tankCapacity = if (obj.isNull("tankCapacity")) null else obj.getDouble("tankCapacity")
            )
        }

        val entries = root.optJSONArray("fuelEntries").orEmpty().map { obj ->
            FuelEntry(
                id = obj.getLong("id"),
                vehicleId = obj.getLong("vehicleId"),
                date = parseDate(obj.getString("date")),
                odometer = obj.getDouble("odometer"),
                fuelVolume = obj.getDouble("fuelVolume"),
                fuelCost = obj.getDouble("fuelCost"),
                fuelPumpId = if (obj.isNull("fuelPumpId")) null else obj.getLong("fuelPumpId"),
                isFullTank = obj.getBoolean("isFullTank")
            )
        }

        val readings = root.optJSONArray("odometerReadings").orEmpty().map { obj ->
            OdometerReading(
                id = obj.getLong("id"),
                vehicleId = obj.getLong("vehicleId"),
                date = parseDate(obj.getString("date")),
                odometer = obj.getDouble("odometer")
            )
        }

        val settings = if (root.has("userSettings") && !root.isNull("userSettings")) {
            val settingsObj = root.getJSONObject("userSettings")
            if (settingsObj.length() > 0) {
                UserSettings(
                    id = settingsObj.optLong("id", 1L),
                    defaultCurrency = settingsObj.getString("defaultCurrency"),
                    defaultDistanceUnit = parseEnum(
                        DistanceUnit::class.java,
                        settingsObj.getString("defaultDistanceUnit"),
                        "defaultDistanceUnit"
                    ),
                    defaultVolumeUnit = parseEnum(
                        VolumeUnit::class.java,
                        settingsObj.getString("defaultVolumeUnit"),
                        "defaultVolumeUnit"
                    )
                )
            } else null
        } else null

        return ParsedBackup(pumps, vehicles, entries, readings, settings)
    }

    private fun parseDate(value: String): LocalDate = try {
        LocalDate.parse(value)
    } catch (e: Exception) {
        throw IllegalArgumentException("Invalid date in backup: $value", e)
    }

    private fun <T : Enum<T>> parseEnum(type: Class<T>, value: String, field: String): T = try {
        java.lang.Enum.valueOf(type, value)
    } catch (e: IllegalArgumentException) {
        throw IllegalArgumentException("Unknown $field value: $value", e)
    }

    private fun JSONArray?.orEmpty(): List<JSONObject> {
        if (this == null) return emptyList()
        return (0 until length()).map { getJSONObject(it) }
    }

    companion object {
        const val BACKUP_VERSION = 1
    }
}
