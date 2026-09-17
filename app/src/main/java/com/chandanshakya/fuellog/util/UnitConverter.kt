package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelType
import com.chandanshakya.fuellog.data.model.Vehicle
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
        // kWh is not convertible to liquid volume
        VolumeUnit.KWH -> value
    }

    fun fromLiters(value: Double, unit: VolumeUnit): Double = when (unit) {
        VolumeUnit.LITERS -> value
        VolumeUnit.GALLONS -> value / GALLONS_TO_LITERS
        VolumeUnit.KWH -> value
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
            VolumeUnit.KWH -> 1.0
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
        VolumeUnit.KWH -> "kWh"
    }

    fun getEfficiencyLabel(
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): String = "${getDistanceUnitLabel(distanceUnit)}/${getVolumeUnitLabel(volumeUnit)}"

    fun convertDistance(value: Double, from: DistanceUnit, to: DistanceUnit): Double {
        val km = toKilometers(value, from)
        return fromKilometers(km, to)
    }

    fun convertVolume(value: Double, from: VolumeUnit, to: VolumeUnit): Double {
        if (from == to) return value
        // Never convert between energy (kWh) and liquid volume
        if (from.isEnergy || to.isEnergy) return value
        val liters = toLiters(value, from)
        return fromLiters(liters, to)
    }
}

/** UI copy that switches between ICE and EV wording. */
object FuelLabels {
    fun isEv(fuelType: FuelType?): Boolean = fuelType?.isElectric == true

    fun isEv(vehicle: Vehicle?): Boolean = isEv(vehicle?.fuelType)

    fun fillVerb(ev: Boolean): String = if (ev) "Charge" else "Fill-up"
    fun fillNoun(ev: Boolean): String = if (ev) "charge" else "fill"
    fun fullTankQuestion(ev: Boolean): String = if (ev) "Full charge?" else "Full tank?"
    fun capacityLabel(ev: Boolean): String = if (ev) "Battery Capacity" else "Tank Capacity"
    fun volumePrompt(ev: Boolean, volumeUnit: VolumeUnit): String {
        val label = UnitConverter.getVolumeUnitLabel(volumeUnit)
        return if (ev) "Energy Added ($label)" else "Fuel Volume ($label)"
    }
    fun ratePrompt(ev: Boolean, currency: String, volumeUnit: VolumeUnit): String {
        val label = UnitConverter.getVolumeUnitLabel(volumeUnit)
        return if (ev) "$currency/$label" else "$currency/$label"
    }
    fun nextFillTitle(ev: Boolean): String = if (ev) "Next Charge" else "Next Fill-Up"
    fun refuelAfter(ev: Boolean): String = if (ev) "Recharge after" else "Refuel after"
    fun predictedChargeOdo(ev: Boolean): String =
        if (ev) "Recharge at odometer" else "Refuel at odometer"
}
