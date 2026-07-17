package com.chandanshakya.fuellog.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Global user settings that serve as defaults for new vehicles.
 * 
 * @param id Always 1 (singleton settings)
 * @param defaultCurrency Default currency for new vehicles
 * @param defaultDistanceUnit Default distance unit for new vehicles
 * @param defaultVolumeUnit Default volume unit for new vehicles
 */
@Entity(tableName = "user_settings")
data class UserSettings(
    @PrimaryKey val id: Long = 1,
    val defaultCurrency: String = "USD",
    val defaultDistanceUnit: DistanceUnit = DistanceUnit.KM,
    val defaultVolumeUnit: VolumeUnit = VolumeUnit.LITERS
)
