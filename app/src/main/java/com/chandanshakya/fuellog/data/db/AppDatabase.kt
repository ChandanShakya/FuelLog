package com.chandanshakya.fuellog.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.OdometerReading
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.data.model.Vehicle

@Database(
    entities = [Vehicle::class, FuelEntry::class, UserSettings::class, FuelPump::class, OdometerReading::class],
    version = 9,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelEntryDao(): FuelEntryDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun fuelPumpDao(): FuelPumpDao
    abstract fun odometerReadingDao(): OdometerReadingDao

    companion object {
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS fuel_pumps (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)")
                db.execSQL("ALTER TABLE fuel_entries ADD COLUMN fuelPumpId INTEGER DEFAULT NULL REFERENCES fuel_pumps(id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fuel_entries_fuelPumpId ON fuel_entries(fuelPumpId)")
            }
        }

        val MIGRATION_8_9 = object : Migration(8, 9) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("ALTER TABLE vehicles ADD COLUMN tankCapacity REAL DEFAULT NULL")
                db.execSQL("ALTER TABLE fuel_entries ADD COLUMN isFullTank INTEGER NOT NULL DEFAULT 0")
                db.execSQL("CREATE TABLE IF NOT EXISTS odometer_readings (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, vehicleId INTEGER NOT NULL REFERENCES vehicles(id) ON DELETE CASCADE, date TEXT NOT NULL, odometer REAL NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_odometer_readings_vehicleId_odometer ON odometer_readings(vehicleId, odometer)")
            }
        }
    }
}
