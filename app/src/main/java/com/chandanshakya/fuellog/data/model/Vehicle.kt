package com.chandanshakya.fuellog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.Instant

@Entity(tableName = "vehicles")
data class Vehicle(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val vehicleType: VehicleType = VehicleType.CAR,
    val fuelType: FuelType = FuelType.PETROL,
    val distanceUnit: DistanceUnit = DistanceUnit.KM,
    val volumeUnit: VolumeUnit = VolumeUnit.LITERS,
    val createdAt: Long = Instant.now().toEpochMilli(),
    val tankCapacity: Double? = null,
    /** Fuel/energy to keep in reserve for next-fill prediction (same unit as tankCapacity). */
    val reserveAmount: Double? = null
) {
    val isElectric: Boolean get() = fuelType.isElectric

    /** Capacity usable before the reserve zone (same unit as tankCapacity). */
    fun usableCapacity(): Double? {
        val capacity = tankCapacity ?: return null
        if (capacity <= 0) return null
        val reserve = reserveAmount?.coerceAtLeast(0.0) ?: 0.0
        return (capacity - reserve).coerceAtLeast(0.0)
    }
}
