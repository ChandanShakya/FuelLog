package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.FuelEntry

data class CapacitySuggestion(
    val learnedCapacity: Double,
    val basedOnFills: Int,
    val confidence: String
)

/**
 * Given consecutive full-tank entries (sorted by odometer ASC), compute what the tank
 * capacity likely is. For each pair of consecutive full-tank fills, the fuel volume added
 * at the second fill is approximately the fuel consumed since the last full fill (since the
 * tank was full at both points). The median of these volumes is the learned capacity.
 */
fun suggestCapacity(fullTankEntries: List<FuelEntry>): CapacitySuggestion? {
    if (fullTankEntries.size < 2) return null

    val volumes = mutableListOf<Double>()
    for (i in 1 until fullTankEntries.size) {
        val prev = fullTankEntries[i - 1]
        val curr = fullTankEntries[i]
        val distance = curr.odometer - prev.odometer
        if (distance <= 0 || curr.fuelVolume <= 0) continue
        volumes.add(curr.fuelVolume)
    }

    if (volumes.isEmpty()) return null

    val sorted = volumes.sorted()
    val median = if (sorted.size % 2 == 0) {
        (sorted[sorted.size / 2 - 1] + sorted[sorted.size / 2]) / 2.0
    } else {
        sorted[sorted.size / 2]
    }

    val confidence = when {
        volumes.size >= 7 -> "High"
        volumes.size >= 4 -> "Medium"
        else -> "Low"
    }

    return CapacitySuggestion(
        learnedCapacity = median,
        basedOnFills = volumes.size,
        confidence = confidence
    )
}

/**
 * Given current stored capacity and full-tank history, return a suggestion if the learned
 * value differs by more than 5% from the stored value. If no stored capacity is set,
 * always suggest.
 */
fun shouldSuggestUpdate(currentCapacity: Double?, fullTankEntries: List<FuelEntry>): CapacitySuggestion? {
    val suggestion = suggestCapacity(fullTankEntries) ?: return null

    if (currentCapacity == null || currentCapacity <= 0) return suggestion

    val diff = kotlin.math.abs(suggestion.learnedCapacity - currentCapacity) / currentCapacity
    return if (diff > 0.05) suggestion else null
}
