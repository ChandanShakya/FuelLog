package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.db.FuelEntryWithPump
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.VolumeUnit
import java.time.LocalDate

data class PumpFillDetail(
    val entryId: Long,
    val date: LocalDate,
    val odometer: Double,
    val fuelVolume: Double,
    val fuelCost: Double,
    val distanceSinceLastFill: Double,
    val mileage: Double
)

data class PumpMileageStat(
    val pumpId: Long?,
    val pumpName: String,
    val avgMileage: Double,
    val fillCount: Int,
    val bestMileage: Double,
    val worstMileage: Double
)

fun computePumpFillHistory(
    entriesSortedByOdometer: List<FuelEntryWithPump>,
    pumpId: Long?,
    distanceUnit: DistanceUnit = DistanceUnit.KM,
    volumeUnit: VolumeUnit = VolumeUnit.LITERS
): List<PumpFillDetail> {
    val allPairs = computeAllPairs(entriesSortedByOdometer, distanceUnit, volumeUnit)
    return allPairs
        .filter { it.pumpId == pumpId }
        .sortedBy { it.detail.date }
        .map { it.detail }
}

fun computePumpMileageStats(
    entriesSortedByOdometer: List<FuelEntryWithPump>,
    distanceUnit: DistanceUnit = DistanceUnit.KM,
    volumeUnit: VolumeUnit = VolumeUnit.LITERS
): List<PumpMileageStat> {
    val allPairs = computeAllPairs(entriesSortedByOdometer, distanceUnit, volumeUnit)

    // Count actual fill-ups per pump (entries where user refueled at that pump)
    val fillCounts = entriesSortedByOdometer
        .groupBy { it.entry.fuelPumpId }
        .mapValues { it.value.size }

    return allPairs
        .groupBy { it.pumpId }
        .mapNotNull { (pId, pairs) ->
            val mileages = pairs.map { it.detail.mileage }
            if (mileages.isEmpty()) return@mapNotNull null
            val avg = mileages.average()
            val pumpName = pairs.firstOrNull()?.pumpName ?: "Unknown / Not recorded"
            PumpMileageStat(
                pumpId = pId,
                pumpName = pumpName,
                avgMileage = avg,
                fillCount = fillCounts[pId] ?: pairs.size,
                bestMileage = mileages.max(),
                worstMileage = mileages.min()
            )
        }
        .sortedBy { it.pumpName }
}

private data class AttributedPair(
    val pumpId: Long?,
    val pumpName: String?,
    val detail: PumpFillDetail
)

private fun computeAllPairs(
    entries: List<FuelEntryWithPump>,
    distanceUnit: DistanceUnit = DistanceUnit.KM,
    volumeUnit: VolumeUnit = VolumeUnit.LITERS
): List<AttributedPair> {
    return entries.adjacentMileagePairs({ it.entry.odometer }, { it.entry.fuelVolume }, distanceUnit, volumeUnit)
        .mapIndexed { index, pair ->
            val curr = entries[index + 1]
            AttributedPair(
                pumpId = curr.entry.fuelPumpId,
                pumpName = curr.pumpName,
                detail = PumpFillDetail(
                    entryId = curr.entry.id,
                    date = curr.entry.date,
                    odometer = curr.entry.odometer,
                    fuelVolume = curr.entry.fuelVolume,
                    fuelCost = curr.entry.fuelCost,
                    distanceSinceLastFill = pair.distance,
                    mileage = pair.mileage
                )
            )
        }
}
