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

private fun computeAllPairs(entries: List<FuelEntryWithPump>): List<AttributedPair> {
    val result = mutableListOf<AttributedPair>()
    for (i in 1 until entries.size) {
        val prev = entries[i - 1]
        val curr = entries[i]
        val distance = curr.entry.odometer - prev.entry.odometer
        if (distance <= 0 || prev.entry.fuelVolume <= 0) continue
        val mileage = distance / prev.entry.fuelVolume
        result.add(
            AttributedPair(
                pumpId = prev.entry.fuelPumpId,
                pumpName = prev.pumpName,
                detail = PumpFillDetail(
                    entryId = prev.entry.id,
                    date = prev.entry.date,
                    odometer = prev.entry.odometer,
                    fuelVolume = prev.entry.fuelVolume,
                    fuelCost = prev.entry.fuelCost,
                    distanceSinceLastFill = distance,
                    mileage = mileage
                )
            )
        )
    }
    return result
}
