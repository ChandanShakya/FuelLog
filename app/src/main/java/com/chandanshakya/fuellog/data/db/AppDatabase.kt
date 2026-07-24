package com.chandanshakya.fuellog.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelPump
import com.chandanshakya.fuellog.data.model.OdometerReading
import com.chandanshakya.fuellog.data.model.UserSettings
import com.chandanshakya.fuellog.data.model.Vehicle

@Database(
    entities = [Vehicle::class, FuelEntry::class, UserSettings::class, FuelPump::class, OdometerReading::class],
    version = 11,
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
