package com.chandanshakya.fuellog.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.time.LocalDate

@Entity(
    tableName = "odometer_readings",
    foreignKeys = [
        ForeignKey(
            entity = Vehicle::class,
            parentColumns = ["id"],
            childColumns = ["vehicleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["vehicleId", "odometer"])
    ]
)
data class OdometerReading(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    var vehicleId: Long,
    var date: LocalDate,
    var odometer: Double
)
