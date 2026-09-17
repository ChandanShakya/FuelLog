package com.chandanshakya.fuellog.data.backup

import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.util.UnitConverter
import kotlinx.coroutines.flow.first
import java.io.OutputStream
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Flat CSV of fuel entries for spreadsheets. One row per fill, newest vehicles first by name.
 */
class CsvExporter(
    private val vehicleDao: VehicleDao,
    private val fuelEntryDao: FuelEntryDao
) {
    suspend fun exportToStream(output: OutputStream) {
        val vehicles = vehicleDao.getAll().first().associateBy { it.id }
        val entries = fuelEntryDao.getAll().sortedWith(
            compareBy({ it.vehicleId }, { it.date }, { it.odometer })
        )

        val header = listOf(
            "vehicle_name",
            "date",
            "odometer",
            "distance_unit",
            "volume_unit",
            "fuel_volume",
            "fuel_cost",
            "rate_per_unit",
            "is_full_tank",
            "fuel_type"
        )

        val rows = entries.map { e ->
            val v = vehicles[e.vehicleId]
            val rateStr = if (e.fuelVolume > 0) formatNum(e.fuelCost / e.fuelVolume) else ""
            listOf(
                escape(v?.name ?: "Unknown"),
                e.date.format(DateTimeFormatter.ISO_LOCAL_DATE),
                formatNum(e.odometer),
                v?.distanceUnit?.name ?: "KM",
                v?.volumeUnit?.name ?: "LITERS",
                formatNum(e.fuelVolume),
                formatNum(e.fuelCost),
                rateStr,
                if (e.isFullTank) "1" else "0",
                v?.fuelType?.name ?: "PETROL"
            ).joinToString(",")
        }

        output.bufferedWriter().use { w ->
            w.write(header.joinToString(","))
            w.newLine()
            rows.forEach {
                w.write(it)
                w.newLine()
            }
        }
    }

    private fun formatNum(value: Double): String {
        val rounded = Math.round(value * 100.0) / 100.0
        return if (rounded == Math.floor(rounded) && !rounded.isInfinite()) {
            rounded.toLong().toString()
        } else {
            rounded.toString()
        }
    }

    private fun escape(value: String): String =
        if (value.contains(',') || value.contains('"') || value.contains('\n')) {
            "\"" + value.replace("\"", "\"\"") + "\""
        } else value
}

object MonthlySummary {
    data class MonthBucket(
        val yearMonth: String,
        val cost: Double,
        val volume: Double,
        val distance: Double,
        val fillCount: Int,
        val mileage: Double?
    )

    /**
     * Aggregate fills by calendar month (date field). Distance is sum of
     * adjacent positive odometer gaps whose end entry falls in that month.
     */
    fun byMonth(
        entries: List<com.chandanshakya.fuellog.data.model.FuelEntry>
    ): List<MonthBucket> {
        if (entries.isEmpty()) return emptyList()
        val sorted = entries.sortedWith(compareBy({ it.date }, { it.odometer }))

        data class Acc(var cost: Double, var volume: Double, var distance: Double, var fills: Int, val mileages: MutableList<Double>)

        val byKey = LinkedHashMap<String, Acc>()
        for (i in sorted.indices) {
            val e = sorted[i]
            val key = "${e.date.year}-${String.format("%02d", e.date.monthValue)}"
            val acc = byKey.getOrPut(key) { Acc(0.0, 0.0, 0.0, 0, mutableListOf()) }
            acc.cost += e.fuelCost
            acc.volume += e.fuelVolume
            acc.fills += 1
            if (i > 0) {
                val prev = sorted[i - 1]
                val dist = e.odometer - prev.odometer
                if (dist > 0 && e.fuelVolume > 0) {
                    acc.distance += dist
                    acc.mileages.add(dist / e.fuelVolume)
                }
            }
        }

        return byKey.entries.sortedBy { it.key }.map { (key, acc) ->
            MonthBucket(
                yearMonth = key,
                cost = com.chandanshakya.fuellog.util.Money.sumCents(listOf(acc.cost)),
                volume = acc.volume,
                distance = acc.distance,
                fillCount = acc.fills,
                mileage = acc.mileages.takeIf { it.isNotEmpty() }?.average()
            )
        }
    }
}
