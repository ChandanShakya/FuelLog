package com.chandanshakya.fuellog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val vehicleType: VehicleType = VehicleType.CAR,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val volumeUnit: VolumeUnit = VolumeUnit.LITERS,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val tankCapacity: Double? = null
)
