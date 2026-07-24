package com.chandanshakya.fuellog.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.data.model.Vehicle

@Database(
    entities = [Vehicle::class, FuelEntry::class, UserSettings::class, FuelPump::class],
    version = 8,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelEntryDao(): FuelEntryDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun fuelPumpDao(): FuelPumpDao

    companion object {
        val MIGRATION_7_8 = object : Migration(7, 8) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS fuel_pumps (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL)")
                db.execSQL("ALTER TABLE fuel_entries ADD COLUMN fuelPumpId INTEGER DEFAULT NULL REFERENCES fuel_pumps(id)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_fuel_entries_fuelPumpId ON fuel_entries(fuelPumpId)")
            }
        }
    }
}
