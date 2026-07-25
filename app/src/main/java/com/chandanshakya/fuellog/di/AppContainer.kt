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
    private val db: AppDatabase = Room.databaseBuilder(
        context.applicationContext,
        AppDatabase::class.java,
        "fuellog-db"
    ).fallbackToDestructiveMigration().build()

    val vehicleDao: VehicleDao = db.vehicleDao()
    val fuelEntryDao: FuelEntryDao = db.fuelEntryDao()
    val fuelPumpDao: FuelPumpDao = db.fuelPumpDao()
    val odometerReadingDao: OdometerReadingDao = db.odometerReadingDao()
    val userSettingsDao: UserSettingsDao = db.userSettingsDao()
}
