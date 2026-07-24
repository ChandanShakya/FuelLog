package com.chandanshakya.fuellog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String,
    var vehicleType: VehicleType = VehicleType.CAR,
    var distanceUnit: DistanceUnit = DistanceUnit.KM,
    var volumeUnit: VolumeUnit = VolumeUnit.LITERS,
    var createdAt: Long = Instant.now().toEpochMilli(),
    var tankCapacity: Double? = null
)
