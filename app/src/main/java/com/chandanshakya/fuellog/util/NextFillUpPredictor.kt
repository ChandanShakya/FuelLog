package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.OdometerReading
import com.chandanshakya.fuellog.data.model.VolumeUnit
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class FillUpPrediction(
    val remainingDistance: Double,
    val predictedDate: LocalDate?,
    val recentMileage: Double,
    val tankCapacity: Double
)

/**
 * Compute recency-weighted mileage from the last N consecutive full-tank pairs.
 * Only full-tank entries where fuelVolume > 0 and distance > 0 are considered.
 */
fun computeRecencyWeightedMileage(
    fullTankEntries: List<FuelEntry>,
    windowSize: Int = 3
): Double? {
    if (fullTankEntries.size < 2) return null

    val mileages = mutableListOf<Double>()
    for (i in 1 until fullTankEntries.size) {
        val prev = fullTankEntries[i - 1]
        val curr = fullTankEntries[i]
        val distance = curr.odometer - prev.odometer
        if (distance <= 0 || curr.fuelVolume <= 0) continue
        mileages.add(distance / curr.fuelVolume)
    }

    if (mileages.isEmpty()) return null

    val recent = mileages.takeLast(windowSize)
    return recent.average()
}

/**
 * Predict the next fill-up based on recency-weighted mileage, tank capacity,
 * and the most recent odometer point (fuel entry or standalone reading).
 *
 * @param entries all fuel entries sorted by odometer ASC
 * @param odometerReadings standalone readings sorted by odometer ASC
 * @param tankCapacity in the vehicle's volume unit
 * @param distanceUnit for the result
 * @param volumeUnit for capacity conversion
 * @param recentWindowSize number of recent full-tank pairs for mileage (default 3)
 */
fun predictNextFillUp(
    entries: List<FuelEntry>,
    odometerReadings: List<OdometerReading>,
    tankCapacity: Double?,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    recentWindowSize: Int = 3
): FillUpPrediction? {
    if (tankCapacity == null || tankCapacity <= 0) return null

    val fullTankEntries = entries.filter { it.isFullTank && it.fuelVolume > 0 }
    val recentMileage = computeRecencyWeightedMileage(fullTankEntries, recentWindowSize) ?: return null

    // Find the most recent odometer point (highest odometer from any source)
    val latestFuelOdo = entries.maxOfOrNull { it.odometer } ?: 0.0
    val latestReadingOdo = odometerReadings.maxOfOrNull { it.odometer } ?: 0.0
    val latestOdo = maxOf(latestFuelOdo, latestReadingOdo)

    // Mileage is in user's units (e.g. km/L or mi/gal), capacity is in user's volume unit.
    // remaining distance = capacity * mileage, both in user units.
    val remainingDistance = tankCapacity * recentMileage

    // Estimate driving frequency: average km/day over the last 30 days
    val today = LocalDate.now()
    val thirtyDaysAgo = today.minusDays(30)

    // Collect all odometer points (entries + readings) from last 30 days
    val recentPoints = mutableListOf<Pair<LocalDate, Double>>()
    for (entry in entries) {
        if (!entry.date.isBefore(thirtyDaysAgo)) {
            recentPoints.add(entry.date to entry.odometer)
        }
    }
    for (reading in odometerReadings) {
        if (!reading.date.isBefore(thirtyDaysAgo)) {
            recentPoints.add(reading.date to reading.odometer)
        }
    }

    val predictedDate = if (recentPoints.size >= 2) {
        val sorted = recentPoints.sortedBy { it.first }
        val earliest = sorted.first()
        val latest = sorted.last()
        val daysBetween = ChronoUnit.DAYS.between(earliest.first, latest.first).coerceAtLeast(1)
        val distanceDriven = latest.second - earliest.second
        if (distanceDriven > 0) {
            val avgDistancePerDay = distanceDriven / daysBetween
            val daysUntilEmpty = if (avgDistancePerDay > 0) (remainingDistance / avgDistancePerDay).toLong() else 0L
            today.plusDays(daysUntilEmpty)
        } else null
    } else null

    return FillUpPrediction(
        remainingDistance = remainingDistance,
        predictedDate = predictedDate,
        recentMileage = recentMileage,
        tankCapacity = tankCapacity
    )
}
