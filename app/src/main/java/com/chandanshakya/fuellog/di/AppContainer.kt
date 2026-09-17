package com.chandanshakya.fuellog.di

import android.content.Context
import androidx.room.Room
import com.chandanshakya.fuellog.data.db.AppDatabase
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.FuelPumpDao
import com.chandanshakya.fuellog.data.db.OdometerReadingDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao

class AppContainer(context: Context) {
    val database: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "fuellog-db"
    )
        // Only wipe on OS/app downgrade. Missing migrations on upgrade throw —
        // ship a real Migration when bumping AppDatabase.version.
        .fallbackToDestructiveMigrationOnDowngrade()
        .build()

    val vehicleDao: VehicleDao = database.vehicleDao()
    val fuelEntryDao: FuelEntryDao = database.fuelEntryDao()
    val fuelPumpDao: FuelPumpDao = database.fuelPumpDao()
    val odometerReadingDao: OdometerReadingDao = database.odometerReadingDao()
    val userSettingsDao: UserSettingsDao = database.userSettingsDao()
}
