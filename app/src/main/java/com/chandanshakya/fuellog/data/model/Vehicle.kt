package com.chandanshakya.fuellog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Represents a vehicle that tracks fuel fill-ups.
 * 
 * @param id Unique identifier for the vehicle
 * @param name Display name of the vehicle
 * @param defaultCurrency Currency code for monetary values (ISO 4217)
 * @param distanceUnit Default distance unit for this vehicle
 * @param volumeUnit Default volume unit for this vehicle
 * @param createdAt Date when the vehicle was added
 */
@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var name: String,
    var defaultCurrency: String = "USD",
    var distanceUnit: DistanceUnit = DistanceUnit.KM,
    var volumeUnit: VolumeUnit = VolumeUnit.LITERS,
    var createdAt: LocalDate = LocalDate.now()
)
