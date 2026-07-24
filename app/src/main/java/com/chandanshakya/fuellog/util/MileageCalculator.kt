package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit


data class MileagePair(val distance: Double, val mileage: Double)

fun <T> Iterable<T>.adjacentMileagePairs(
    odometer: (T) -> Double,
    fuelVolume: (T) -> Double,
    distanceUnit: DistanceUnit = DistanceUnit.KM,
    volumeUnit: VolumeUnit = VolumeUnit.LITERS
): List<MileagePair> {
    val list = this.toList()
    val result = mutableListOf<MileagePair>()
    for (i in 1 until list.size) {
        val prev = list[i - 1]
        val curr = list[i]
        val distanceRaw = odometer(curr) - odometer(prev)
        val fuelVolumeRaw = fuelVolume(curr)
        if (distanceRaw > 0 && fuelVolumeRaw > 0) {
            val mileageUserUnits = distanceRaw / fuelVolumeRaw
            result.add(MileagePair(distanceRaw, mileageUserUnits))
        }
    }
    return result
}

object MileageCalculator {
    fun calculateMileage(
        current: FuelEntry,
        previous: FuelEntry?
    ): Double? {
        if (previous == null) return null
        val distance = current.odometer - previous.odometer
        if (distance <= 0 || current.fuelVolume <= 0) return null
        return distance / current.fuelVolume
    }

    fun calculateAverageMileage(
        entries: List<FuelEntry>,
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): Double? {
        val pairs = entries.adjacentMileagePairs({ it.odometer }, { it.fuelVolume }, distanceUnit, volumeUnit)
        if (pairs.isEmpty()) return null
        return pairs.map { it.mileage }.average()
    }

    fun calculateTotalDistance(
        entries: List<FuelEntry>
    ): Double {
        if (entries.isEmpty()) return 0.0
        return entries.last().odometer - entries.first().odometer
    }

    fun calculateTotalFuel(
        entries: List<FuelEntry>
    ): Double {
        return entries.sumOf { it.fuelVolume }
    }

    fun calculateTotalCost(entries: List<FuelEntry>): Double = entries.sumOf { it.fuelCost }
}
