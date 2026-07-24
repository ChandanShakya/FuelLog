package com.chandanshakya.fuellog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "fuel_pumps")
data class FuelPump(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String
)
