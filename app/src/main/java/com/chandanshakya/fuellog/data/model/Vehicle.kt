package com.chandanshakya.fuellog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String,
    var vehicleType: String = "car",
    var defaultCurrency: String = "USD",
    var distanceUnit: DistanceUnit = DistanceUnit.KM,
    var volumeUnit: VolumeUnit = VolumeUnit.LITERS,
    var createdAt: LocalDate = LocalDate.now()
)
