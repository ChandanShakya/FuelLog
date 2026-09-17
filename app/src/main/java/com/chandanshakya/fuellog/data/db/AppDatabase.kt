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

/**
 * v12: fuelType + reserveAmount on vehicles (EV support and reserve-aware prediction).
 */
val MIGRATION_11_12 = object : Migration(11, 12) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL("ALTER TABLE vehicles ADD COLUMN fuelType TEXT NOT NULL DEFAULT 'PETROL'")
        db.execSQL("ALTER TABLE vehicles ADD COLUMN reserveAmount REAL")
    }
}

@Database(
    entities = [Vehicle::class, FuelEntry::class, UserSettings::class, FuelPump::class, OdometerReading::class],
    version = 12,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun vehicleDao(): VehicleDao
    abstract fun fuelEntryDao(): FuelEntryDao
    abstract fun userSettingsDao(): UserSettingsDao
    abstract fun fuelPumpDao(): FuelPumpDao
    abstract fun odometerReadingDao(): OdometerReadingDao
}
