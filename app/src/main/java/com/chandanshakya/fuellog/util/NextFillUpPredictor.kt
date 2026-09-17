package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.OdometerReading
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VolumeUnit
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class FillUpPrediction(
    val remainingDistance: Double,
    val predictedDate: LocalDate?,
    val predictedOdometer: Double?,
    val recentMileage: Double,
    val tankCapacity: Double,
    val reserveAmount: Double = 0.0,
    /** Remaining distance until the reserve zone (usually same as remaining when reserve is set). */
    val remainingToReserve: Double = 0.0
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
 * Estimate usable range from fill history.
 * Full fills set tank level to [tankCapacity]; reserve is subtracted once at the end.
 */
internal fun estimateRemainingDistance(
    entries: List<FuelEntry>,
    odometerReadings: List<OdometerReading>,
    tankCapacity: Double,
    usableCapacity: Double,
    recentMileage: Double
): Pair<Double, Double>? {
    if (recentMileage <= 0 || tankCapacity <= 0) return null

    val fills = entries.filter { it.fuelVolume > 0 }.sortedBy { it.odometer }
    val readings = odometerReadings.sortedBy { it.odometer }

    val latestFuelOdo = fills.lastOrNull()?.odometer
    val latestReadingOdo = readings.lastOrNull()?.odometer
    val latestOdo = maxOf(latestFuelOdo ?: 0.0, latestReadingOdo ?: 0.0)
    if (latestOdo <= 0.0 && fills.isEmpty()) return null

    var fuelInTank: Double? = null
    var lastFillOdo = 0.0
    for (fill in fills) {
        val previousFuel = fuelInTank
        fuelInTank = when {
            fill.isFullTank -> tankCapacity
            previousFuel != null -> minOf(tankCapacity, previousFuel + fill.fuelVolume)
            else -> minOf(tankCapacity, fill.fuelVolume)
        }
        lastFillOdo = fill.odometer
    }

    val fuelAtLastFill = fuelInTank ?: return null
    val distanceSinceLastFill = (latestOdo - lastFillOdo).coerceAtLeast(0.0)
    // Subtract reserve once: prediction target is the reserve zone, not empty.
    val reserve = (tankCapacity - usableCapacity).coerceAtLeast(0.0)
    val usableFuel = (fuelAtLastFill - reserve).coerceAtLeast(0.0)
    val remaining = usableFuel * recentMileage - distanceSinceLastFill
    return latestOdo to remaining.coerceAtLeast(0.0)
}

fun predictNextFillUp(
    entries: List<FuelEntry>,
    odometerReadings: List<OdometerReading>,
    tankCapacity: Double?,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit,
    reserveAmount: Double? = null,
    recentWindowSize: Int = 5
): FillUpPrediction? {
    if (tankCapacity == null || tankCapacity <= 0) return null

    val reserve = reserveAmount?.coerceAtLeast(0.0) ?: 0.0
    val usableCapacity = (tankCapacity - reserve).coerceAtLeast(0.0)
    if (usableCapacity <= 0) return null

    val usableEntries = entries.filter { it.fuelVolume > 0 }.sortedBy { it.odometer }
    val recentMileage = computeRecencyWeightedMileage(
        usableEntries, recentWindowSize, distanceUnit, volumeUnit
    ) ?: return null

    val (latestOdo, remainingDistance) = estimateRemainingDistance(
        entries = usableEntries,
        odometerReadings = odometerReadings,
        tankCapacity = tankCapacity,
        usableCapacity = usableCapacity,
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
        tankCapacity = tankCapacity,
        reserveAmount = reserve,
        remainingToReserve = remainingDistance
    )
}

fun predictNextFillUp(
    entries: List<FuelEntry>,
    odometerReadings: List<OdometerReading>,
    vehicle: Vehicle,
    recentWindowSize: Int = 5
): FillUpPrediction? = predictNextFillUp(
    entries = entries,
    odometerReadings = odometerReadings,
    tankCapacity = vehicle.tankCapacity,
    distanceUnit = vehicle.distanceUnit,
    volumeUnit = vehicle.volumeUnit,
    reserveAmount = vehicle.reserveAmount,
    recentWindowSize = recentWindowSize
)

data class TripEstimate(
    val distance: Double,
    val energyNeeded: Double,
    val cost: Double,
    val mileage: Double,
    val rate: Double,
    val distanceUnit: DistanceUnit,
    val volumeUnit: VolumeUnit
)

/**
 * Estimate energy/fuel and cost for a planned trip using recent mileage and last rate.
 */
fun estimateTrip(
    distance: Double,
    recentMileage: Double?,
    lastRate: Double?,
    distanceUnit: DistanceUnit,
    volumeUnit: VolumeUnit
): TripEstimate? {
    if (distance <= 0 || recentMileage == null || recentMileage <= 0) return null
    val energy = distance / recentMileage
    val rate = lastRate ?: 0.0
    return TripEstimate(
        distance = distance,
        energyNeeded = energy,
        cost = Money.roundToCents(energy * rate),
        mileage = recentMileage,
        rate = Money.roundToCents(rate),
        distanceUnit = distanceUnit,
        volumeUnit = volumeUnit
    )
}

/** Last price per unit from the most recent fill with volume > 0. */
fun lastFuelRate(entries: List<FuelEntry>): Double? {
    val last = entries.filter { it.fuelVolume > 0 }.maxByOrNull { it.odometer } ?: return null
    return Money.rate(last.fuelVolume, last.fuelCost)
}
