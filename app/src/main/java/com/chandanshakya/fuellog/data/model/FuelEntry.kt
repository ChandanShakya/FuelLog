package com.chandanshakya.fuellog.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "fuel_entries",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = FuelPump::class,
            parentColumns = ["id"],
            childColumns = ["fuelPumpId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["vehicleId", "odometer"]),
        Index(value = ["date"]),
        Index(value = ["fuelPumpId"])
    ]
)
data class FuelEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val vehicleId: Long,
    val date: LocalDate,
    val odometer: Double,
    val fuelVolume: Double,
    val fuelCost: Double,
    val fuelPumpId: Long? = null,
    val isFullTank: Boolean = false
)
