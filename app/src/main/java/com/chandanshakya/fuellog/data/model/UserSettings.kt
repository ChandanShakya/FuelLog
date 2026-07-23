package com.chandanshakya.fuellog.data.model

data class UserSettings(
    val id: Long = 1,
    var defaultCurrency: String = "USD",
    var defaultDistanceUnit: DistanceUnit = DistanceUnit.KM,
    var defaultVolumeUnit: VolumeUnit = VolumeUnit.LITERS
)
