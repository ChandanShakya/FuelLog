package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.db.FuelEntryWithPump
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
    pumpId: Long?
): List<PumpFillDetail> {
    val allPairs = computeAllPairs(entriesSortedByOdometer)
    return allPairs
        .filter { it.pumpId == pumpId }
        .sortedBy { it.detail.date }
        .map { it.detail }
}

fun computePumpMileageStats(
    entriesSortedByOdometer: List<FuelEntryWithPump>
): List<PumpMileageStat> {
    val allPairs = computeAllPairs(entriesSortedByOdometer)

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
                fillCount = pairs.size,
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

private fun computeAllPairs(entries: List<FuelEntryWithPump>): List<AttributedPair> {
    val result = mutableListOf<AttributedPair>()
    for (i in 1 until entries.size) {
        val prev = entries[i - 1]
        val curr = entries[i]
        val distance = curr.entry.odometer - prev.entry.odometer
        if (distance <= 0 || curr.entry.fuelVolume <= 0) continue
        val mileage = distance / curr.entry.fuelVolume
        result.add(
            AttributedPair(
                pumpId = curr.entry.fuelPumpId,
                pumpName = curr.pumpName,
                detail = PumpFillDetail(
                    entryId = curr.entry.id,
                    date = curr.entry.date,
                    odometer = curr.entry.odometer,
                    fuelVolume = curr.entry.fuelVolume,
                    fuelCost = curr.entry.fuelCost,
                    distanceSinceLastFill = distance,
                    mileage = mileage
                )
            )
        )
    }
    return result
}
