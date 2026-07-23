package com.chandanshakya.fuellog.data.db

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class FuelLogDbHelper(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(SQL_CREATE_VEHICLES)
        db.execSQL(SQL_CREATE_FUEL_ENTRIES)
        db.execSQL(SQL_CREATE_USER_SETTINGS)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        // Room used fallbackToDestructiveMigration, so we do the same
        db.execSQL("DROP TABLE IF EXISTS fuel_entries")
        db.execSQL("DROP TABLE IF EXISTS vehicles")
        db.execSQL("DROP TABLE IF EXISTS user_settings")
        onCreate(db)
    }

    companion object {
        const val DATABASE_NAME = "fuellog-db"
        const val DATABASE_VERSION = 7

        private const val SQL_CREATE_VEHICLES = """
            CREATE TABLE vehicles (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                name TEXT NOT NULL,
                vehicleType TEXT NOT NULL DEFAULT 'CAR',
                distanceUnit TEXT NOT NULL DEFAULT 'KM',
                volumeUnit TEXT NOT NULL DEFAULT 'LITERS',
                createdAt TEXT NOT NULL
            )
        """

        private const val SQL_CREATE_FUEL_ENTRIES = """
            CREATE TABLE fuel_entries (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                vehicleId INTEGER NOT NULL,
                date TEXT NOT NULL,
                odometer REAL NOT NULL,
                fuelVolume REAL NOT NULL,
                fuelCost REAL NOT NULL,
                FOREIGN KEY (vehicleId) REFERENCES vehicles(id) ON DELETE CASCADE
            )
        """

        private const val SQL_CREATE_USER_SETTINGS = """
            CREATE TABLE user_settings (
                id INTEGER PRIMARY KEY,
                defaultCurrency TEXT NOT NULL DEFAULT 'USD',
                defaultDistanceUnit TEXT NOT NULL DEFAULT 'KM',
                defaultVolumeUnit TEXT NOT NULL DEFAULT 'LITERS'
            )
        """
    }
}
