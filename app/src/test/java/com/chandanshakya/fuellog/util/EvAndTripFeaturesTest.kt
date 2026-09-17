package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.backup.MonthlySummary
import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.FuelType
import com.chandanshakya.fuellog.data.model.Vehicle
import com.chandanshakya.fuellog.data.model.VolumeUnit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class EvAndTripFeaturesTest {

    private fun entry(id: Long, odo: Double, vol: Double, cost: Double, full: Boolean = true, day: Int = id.toInt()) =
        FuelEntry(
            id = id, vehicleId = 1, date = LocalDate.of(2024, 1, day),
            odometer = odo, fuelVolume = vol, fuelCost = cost, isFullTank = full
        )

    @Test
    fun volumeUnitLabel_kwh() {
        assertEquals("kWh", UnitConverter.getVolumeUnitLabel(VolumeUnit.KWH))
        assertEquals("km/kWh", UnitConverter.getEfficiencyLabel(DistanceUnit.KM, VolumeUnit.KWH))
    }

    @Test
    fun convertVolume_doesNotConvertEnergy() {
        assertEquals(40.0, UnitConverter.convertVolume(40.0, VolumeUnit.LITERS, VolumeUnit.KWH), 0.0)
        assertEquals(40.0, UnitConverter.convertVolume(40.0, VolumeUnit.KWH, VolumeUnit.LITERS), 0.0)
    }

    @Test
    fun vehicleUsableCapacity_subtractsReserve() {
        val v = Vehicle(name = "EV", fuelType = FuelType.ELECTRIC, volumeUnit = VolumeUnit.KWH, tankCapacity = 60.0, reserveAmount = 10.0)
        assertEquals(50.0, v.usableCapacity()!!, 0.001)
        assertTrue(v.isElectric)
    }

    @Test
    fun predictWithReserve_targetsReserveNotEmpty() {
        val entries = listOf(
            entry(1, 1000.0, 50.0, 100.0, full = true, day = 1),
            entry(2, 1500.0, 50.0, 100.0, full = true, day = 15)
        )
        // capacity 40, reserve 10 → usable 30; mileage 10 → remaining 300
        val result = predictNextFillUp(
            entries = entries,
            odometerReadings = emptyList(),
            tankCapacity = 40.0,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS,
            reserveAmount = 10.0
        )
        assertNotNull(result)
        assertEquals(300.0, result!!.remainingDistance, 0.01)
        assertEquals(10.0, result.reserveAmount, 0.001)
    }

    @Test
    fun estimateTrip_usesMileageAndRate() {
        val t = estimateTrip(
            distance = 100.0,
            recentMileage = 10.0,
            lastRate = 2.5,
            distanceUnit = DistanceUnit.KM,
            volumeUnit = VolumeUnit.LITERS
        )
        assertNotNull(t)
        assertEquals(10.0, t!!.energyNeeded, 0.001)
        assertEquals(25.0, t.cost, 0.001)
    }

    @Test
    fun estimateTrip_requiresMileage() {
        assertNull(
            estimateTrip(100.0, null, 2.0, DistanceUnit.KM, VolumeUnit.LITERS)
        )
    }

    @Test
    fun lastFuelRate_fromLatestFill() {
        val entries = listOf(
            entry(1, 1000.0, 40.0, 80.0, day = 1),   // 2.0
            entry(2, 1500.0, 50.0, 150.0, day = 15)  // 3.0
        )
        assertEquals(3.0, lastFuelRate(entries)!!, 0.001)
    }

    @Test
    fun monthlySummary_groupsByMonth() {
        val entries = listOf(
            entry(1, 1000.0, 40.0, 80.0, day = 5),
            entry(2, 1200.0, 40.0, 80.0, day = 20),
            entry(3, 1500.0, 50.0, 120.0, day = 3).let {
                it.copy(date = LocalDate.of(2024, 2, 3))
            }
        )
        val months = MonthlySummary.byMonth(entries)
        assertEquals(2, months.size)
        assertEquals("2024-01", months[0].yearMonth)
        assertEquals(2, months[0].fillCount)
        assertEquals(200.0, months[0].distance, 0.01)
        assertEquals("2024-02", months[1].yearMonth)
    }
}
