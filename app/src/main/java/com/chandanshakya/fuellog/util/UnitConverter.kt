package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit

object UnitConverter {
    private const val MILES_TO_KM = 1.609344
    private const val GALLONS_TO_LITERS = 3.78541

    fun toKilometers(value: Double, unit: DistanceUnit): Double = when (unit) {
        DistanceUnit.KM -> value
        DistanceUnit.MILES -> value * MILES_TO_KM
    }

    fun fromKilometers(value: Double, unit: DistanceUnit): Double = when (unit) {
        DistanceUnit.KM -> value
        DistanceUnit.MILES -> value / MILES_TO_KM
    }

    fun toLiters(value: Double, unit: VolumeUnit): Double = when (unit) {
        VolumeUnit.LITERS -> value
        VolumeUnit.GALLONS -> value * GALLONS_TO_LITERS
    }

    fun fromLiters(value: Double, unit: VolumeUnit): Double = when (unit) {
        VolumeUnit.LITERS -> value
        VolumeUnit.GALLONS -> value / GALLONS_TO_LITERS
    }

    fun calculateEfficiencyBase(
        distance: Double,
        fuelVolume: Double,
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): Double {
        val distanceKm = toKilometers(distance, distanceUnit)
        val volumeLiters = toLiters(fuelVolume, volumeUnit)
        return if (volumeLiters > 0) distanceKm / volumeLiters else 0.0
    }

    fun convertEfficiency(
        efficiencyKmPerLiter: Double,
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): Double {
        val distanceFactor = when (distanceUnit) {
            DistanceUnit.KM -> 1.0
            DistanceUnit.MILES -> MILES_TO_KM
        }
        val volumeFactor = when (volumeUnit) {
            VolumeUnit.LITERS -> 1.0
            VolumeUnit.GALLONS -> GALLONS_TO_LITERS
        }
        return efficiencyKmPerLiter * volumeFactor / distanceFactor
    }

    fun getDistanceUnitLabel(unit: DistanceUnit): String = when (unit) {
        DistanceUnit.KM -> "km"
        DistanceUnit.MILES -> "mi"
    }

    fun getVolumeUnitLabel(unit: VolumeUnit): String = when (unit) {
        VolumeUnit.LITERS -> "L"
        VolumeUnit.GALLONS -> "gal"
    }

    fun getEfficiencyLabel(
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): String = "${getDistanceUnitLabel(distanceUnit)}/${getVolumeUnitLabel(volumeUnit)}"
}
