package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit

object MileageCalculator {
    fun calculateMileage(
        current: FuelEntry,
        previous: FuelEntry?,
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): Double? {
        if (previous == null) return null

        val distanceKm = current.odometer - previous.odometer
        val fuelLiters = current.fuelVolume

        if (fuelLiters <= 0 || distanceKm <= 0) return null

        val mileageKmPerLiter = distanceKm / fuelLiters

        return UnitConverter.convertEfficiency(mileageKmPerLiter, distanceUnit, volumeUnit)
    }

    fun calculateMileageBase(
        current: FuelEntry,
        previous: FuelEntry?
    ): Double? {
        if (previous == null) return null

        val distanceKm = current.odometer - previous.odometer
        val fuelLiters = current.fuelVolume

        if (fuelLiters <= 0 || distanceKm <= 0) return null

        return distanceKm / fuelLiters
    }

    fun calculateAverageMileage(
        entries: List<FuelEntry>,
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): Double? {
        if (entries.size < 2) return null

        val mileages = mutableListOf<Double>()
        for (i in 1 until entries.size) {
            val mileage = calculateMileage(entries[i], entries[i - 1], distanceUnit, volumeUnit)
            mileage?.let { mileages.add(it) }
        }

        return if (mileages.isNotEmpty()) mileages.average() else null
    }

    fun calculateTotalDistance(
        entries: List<FuelEntry>,
        distanceUnit: DistanceUnit
    ): Double {
        if (entries.isEmpty()) return 0.0
        val distanceKm = entries.last().odometer - entries.first().odometer
        return UnitConverter.fromKilometers(distanceKm, distanceUnit)
    }

    fun calculateTotalFuel(
        entries: List<FuelEntry>,
        volumeUnit: VolumeUnit
    ): Double {
        val totalLiters = entries.sumOf { it.fuelVolume }
        return UnitConverter.fromLiters(totalLiters, volumeUnit)
    }

    fun calculateTotalCost(entries: List<FuelEntry>): Double = entries.sumOf { it.fuelCost }
}
