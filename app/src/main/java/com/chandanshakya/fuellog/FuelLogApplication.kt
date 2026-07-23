package com.chandanshakya.fuellog

import android.app.Application
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.FuelLogDbHelper
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao

class FuelLogApplication : Application() {

    lateinit var dbHelper: FuelLogDbHelper
        private set
    lateinit var vehicleDao: VehicleDao
        private set
    lateinit var fuelEntryDao: FuelEntryDao
        private set
    lateinit var userSettingsDao: UserSettingsDao
        private set

    override fun onCreate() {
        super.onCreate()
        dbHelper = FuelLogDbHelper(this)
        vehicleDao = VehicleDao(dbHelper)
        fuelEntryDao = FuelEntryDao(dbHelper)
        userSettingsDao = UserSettingsDao(dbHelper)
    }
}
