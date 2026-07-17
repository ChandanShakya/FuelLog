package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit

/**
 * Utility class for unit conversions.
 * 
 * All internal calculations use KM and LITERS as the base units.
 * Conversion to/from display units happens only at the UI boundary.
 */
object UnitConverter {
    // Conversion factors
    private const val MILES_TO_KM = 1.609344
    private const val GALLONS_TO_LITERS = 3.78541

    /**
     * Convert distance to kilometers (base unit).
     */
    fun toKilometers(value: Double, unit: DistanceUnit): Double = when (unit) {
        DistanceUnit.KM -> value
        DistanceUnit.MILES -> value * MILES_TO_KM
    }

    /**
     * Convert distance from kilometers to display unit.
     */
    fun fromKilometers(value: Double, unit: DistanceUnit): Double = when (unit) {
        DistanceUnit.KM -> value
        DistanceUnit.MILES -> value / MILES_TO_KM
    }

    /**
     * Convert volume to liters (base unit).
     */
    fun toLiters(value: Double, unit: VolumeUnit): Double = when (unit) {
        VolumeUnit.LITERS -> value
        VolumeUnit.GALLONS -> value * GALLONS_TO_LITERS
    }

    /**
     * Convert volume from liters to display unit.
     */
    fun fromLiters(value: Double, unit: VolumeUnit): Double = when (unit) {
        VolumeUnit.LITERS -> value
        VolumeUnit.GALLONS -> value / GALLONS_TO_LITERS
    }

    /**
     * Calculate fuel efficiency in base units (km/liter).
     * 
     * @param distance Distance traveled in display units
     * @param fuelVolume Fuel consumed in display units
     * @param distanceUnit Display unit for distance
     * @param volumeUnit Display unit for volume
     */
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

    /**
     * Convert efficiency from base units (km/liter) to display units.
     * 
     * @param efficiencyKmPerLiter Efficiency in km/liter
     * @param distanceUnit Display unit for distance
     * @param volumeUnit Display unit for volume
     */
    fun convertEfficiency(
        efficiencyKmPerLiter: Double,
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): Double {
        val distanceFactor = when (distanceUnit) {
            DistanceUnit.KM -> 1.0
            DistanceUnit.MILES -> 1.0 / MILES_TO_KM
        }
        val volumeFactor = when (volumeUnit) {
            VolumeUnit.LITERS -> 1.0
            VolumeUnit.GALLONS -> GALLONS_TO_LITERS
        }
        return efficiencyKmPerLiter * distanceFactor / volumeFactor
    }

    /**
     * Get the display label for distance unit.
     */
    fun getDistanceUnitLabel(unit: DistanceUnit): String = when (unit) {
        DistanceUnit.KM -> "km"
        DistanceUnit.MILES -> "mi"
    }

    /**
     * Get the display label for volume unit.
     */
    fun getVolumeUnitLabel(unit: VolumeUnit): String = when (unit) {
        VolumeUnit.LITERS -> "L"
        VolumeUnit.GALLONS -> "gal"
    }

    /**
     * Get the efficiency label based on units.
     */
    fun getEfficiencyLabel(
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): String = "${getDistanceUnitLabel(distanceUnit)}/${getVolumeUnitLabel(volumeUnit)}"
}
