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
    val distanceSinceLastFill: Double?,
    val mileage: Double?
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
            val mileages = pairs.mapNotNull { it.detail.mileage }
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
    if (entries.size < 2) return emptyList()

    return entries.mapIndexedNotNull { i, entryWithPump ->
        // Mileage: forward-looking — how efficient was this fill-up?
        val mileage = if (i < entries.size - 1) {
            val dist = entries[i + 1].entry.odometer - entries[i].entry.odometer
            if (dist > 0 && entries[i + 1].entry.fuelVolume > 0) dist / entries[i + 1].entry.fuelVolume else null
        } else null

        // Distance since last: backward-looking
        val distanceSinceLast = if (i > 0) {
            val dist = entries[i].entry.odometer - entries[i - 1].entry.odometer
            if (dist > 0) dist else null
        } else null

        if (mileage == null && distanceSinceLast == null) return@mapIndexedNotNull null

        AttributedPair(
            pumpId = entryWithPump.entry.fuelPumpId,
            pumpName = entryWithPump.pumpName,
            detail = PumpFillDetail(
                entryId = entryWithPump.entry.id,
                date = entryWithPump.entry.date,
                odometer = entryWithPump.entry.odometer,
                fuelVolume = entryWithPump.entry.fuelVolume,
                fuelCost = entryWithPump.entry.fuelCost,
                distanceSinceLastFill = distanceSinceLast,
                mileage = mileage
            )
        )
    }
}
