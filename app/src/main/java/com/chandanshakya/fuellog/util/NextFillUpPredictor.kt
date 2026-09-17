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
    val predictedOdometer: Double?,
    val recentMileage: Double,
    val tankCapacity: Double
)

/**
 * Compute recency-weighted mileage from adjacent fill pairs using an EWMA.
 * Only pairs where fuelVolume > 0 and distance > 0 are considered.
 */
fun computeRecencyWeightedMileage(
    entries: List<FuelEntry>,
    effectiveWindow: Int = 5,
    distanceUnit: DistanceUnit = DistanceUnit.KM,
    volumeUnit: VolumeUnit = VolumeUnit.LITERS
): Double? {
    if (entries.size < 2) return null

    val mileages = entries
        .adjacentMileagePairs({ it.odometer }, { it.fuelVolume }, distanceUnit, volumeUnit)
        .map { it.mileage }
    if (mileages.isEmpty()) return null

    val alpha = 2.0 / (effectiveWindow + 1)
    var ewma = mileages.first()
    for (i in 1 until mileages.size) {
        ewma = alpha * mileages[i] + (1 - alpha) * ewma
    }
    return ewma
}

/**
 * Estimate fuel left in the tank at [latestOdo] by walking fill history.
 *
 * Full-tank fills reset the tank to [tankCapacity]. Partial fills add volume
 * (capped at capacity). Standalone odometer readings after the last fill reduce
 * remaining range by distance driven / mileage.
 */
internal fun estimateRemainingDistance(
    entries: List<FuelEntry>,
    odometerReadings: List<OdometerReading>,
    tankCapacity: Double,
    recentMileage: Double
): Pair<Double, Double>? {
    if (recentMileage <= 0 || tankCapacity <= 0) return null

    val fills = entries.filter { it.fuelVolume > 0 }.sortedBy { it.odometer }
    val readings = odometerReadings.sortedBy { it.odometer }

    val latestFuelOdo = fills.lastOrNull()?.odometer
    val latestReadingOdo = readings.lastOrNull()?.odometer
    val latestOdo = maxOf(latestFuelOdo ?: 0.0, latestReadingOdo ?: 0.0)
    if (latestOdo <= 0.0 && fills.isEmpty()) return null

    // Track estimated fuel in tank at the last fuel event.
    var fuelInTank: Double? = null
    var lastFillOdo = 0.0
    for (fill in fills) {
        val previousFuel = fuelInTank
        fuelInTank = when {
            fill.isFullTank -> tankCapacity
            previousFuel != null -> minOf(tankCapacity, previousFuel + fill.fuelVolume)
            // Partial fill with unknown prior level: treat added volume as current fuel
            // (optimistic lower bound on range after that fill).
            else -> minOf(tankCapacity, fill.fuelVolume)
        }
        lastFillOdo = fill.odometer
    }

    val fuelAtLastFill = fuelInTank ?: return null
    val distanceSinceLastFill = (latestOdo - lastFillOdo).coerceAtLeast(0.0)
    val remaining = fuelAtLastFill * recentMileage - distanceSinceLastFill
    return latestOdo to remaining.coerceAtLeast(0.0)
}

/**
 * Predict the next fill-up from recency-weighted mileage, tank capacity,
 * the latest odometer point (fuel entry **or** standalone reading), and fuel
 * estimated to be left after that point.
 *
 * Logging a new odometer reading after a fill reduces remaining distance and
 * moves the predicted date/odometer earlier.
 */
fun predictNextFillUp(
    entries: List<FuelEntry>,
    odometerReadings: List<OdometerReading>,
    tankCapacity: Double?,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    recentWindowSize: Int = 5
): FillUpPrediction? {
    if (tankCapacity == null || tankCapacity <= 0) return null

    val usableEntries = entries.filter { it.fuelVolume > 0 }.sortedBy { it.odometer }
    val recentMileage = computeRecencyWeightedMileage(
        usableEntries, recentWindowSize, distanceUnit, volumeUnit
    ) ?: return null

    val (latestOdo, remainingDistance) = estimateRemainingDistance(
        entries = usableEntries,
        odometerReadings = odometerReadings,
        tankCapacity = tankCapacity,
        recentMileage = recentMileage
    ) ?: return null

    val today = LocalDate.now()
    val thirtyDaysAgo = today.minusDays(30)

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

    val predictedDate = if (remainingDistance <= 0) {
        today
    } else if (recentPoints.size >= 2) {
        val sorted = recentPoints.sortedBy { it.first }
        val earliest = sorted.first()
        val latest = sorted.last()
        val daysBetween = ChronoUnit.DAYS.between(earliest.first, latest.first).coerceAtLeast(1)
        val distanceDriven = latest.second - earliest.second
        if (distanceDriven > 0) {
            val avgDistancePerDay = distanceDriven / daysBetween
            val daysUntilEmpty = if (avgDistancePerDay > 0) {
                (remainingDistance / avgDistancePerDay).toLong()
            } else 0L
            today.plusDays(daysUntilEmpty)
        } else null
    } else null

    return FillUpPrediction(
        remainingDistance = remainingDistance,
        predictedDate = predictedDate,
        predictedOdometer = if (latestOdo > 0) latestOdo + remainingDistance else null,
        recentMileage = recentMileage,
        tankCapacity = tankCapacity
    )
}
