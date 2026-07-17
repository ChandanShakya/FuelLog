package com.chandanshakya.fuellog.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

/**
 * Represents a single fuel fill-up entry for a vehicle.
 * 
 * @param id Unique identifier for the entry
 * @param vehicleId Foreign key to the associated vehicle
 * @param date Date of the fill-up
 * @param odometer Odometer reading at fill-up
 * @param fuelVolume Amount of fuel added
 * @param fuelCost Total cost of the fuel
 * @param isFullTank Whether this was a full tank fill-up
 * @param notes Optional notes about this entry
 */
@Entity(
    tableName = "fuel_entries",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["vehicleId", "odometer"]),
        Index(value = ["vehicleId"]),
        Index(value = ["date"])
    ]
)
data class FuelEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val date: LocalDate,
    val odometer: Double,
    val fuelVolume: Double,
    val fuelCost: Double,
    val isFullTank: Boolean = false,
    val notes: String? = null
)
