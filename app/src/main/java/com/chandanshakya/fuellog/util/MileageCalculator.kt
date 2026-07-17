package com.chandanshakya.fuellog.util

import com.chandanshakya.fuellog.data.model.DistanceUnit
import com.chandanshakya.fuellog.data.model.FuelEntry
import com.chandanshakya.fuellog.data.model.VolumeUnit

/**
 * Utility for calculating mileage/fuel efficiency.
 * 
 * All calculations normalize through KM/LITERS internally, then convert back for display.
 */
object MileageCalculator {
    /**
     * Calculate mileage for a fuel entry given the previous entry.
     * 
     * @param current The current fuel entry
     * @param previous The previous fuel entry (by odometer order)
     * @param distanceUnit Display unit for the vehicle
     * @param volumeUnit Display unit for the vehicle
     * @return Mileage in display units, or null if previous entry doesn't exist
     */
    fun calculateMileage(
        current: FuelEntry,
        previous: FuelEntry?,
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): Double? {
        if (previous == null) return null
        
        val distanceKm = current.odometer - previous.odometer
        val fuelLiters = current.fuelVolume
        
        if (fuelLiters <= 0 || distanceKm <= 0) return null
        
        val mileageKmPerLiter = distanceKm / fuelLiters
        
        return UnitConverter.convertEfficiency(
            mileageKmPerLiter,
            distanceUnit,
            volumeUnit
        )
    }

    /**
     * Calculate mileage in base units (km/liter) for a fuel entry.
     * 
     * @param current The current fuel entry
     * @param previous The previous fuel entry
     * @return Mileage in km/liter, or null if previous entry doesn't exist
     */
    fun calculateMileageBase(
        current: FuelEntry,
        previous: FuelEntry?
    ): Double? {
        if (previous == null) return null
        
        val distanceKm = current.odometer - previous.odometer
        val fuelLiters = current.fuelVolume
        
        if (fuelLiters <= 0 || distanceKm <= 0) return null
        
        return distanceKm / fuelLiters
    }

    /**
     * Calculate average mileage for a list of fuel entries.
     * 
     * @param entries Sorted list of fuel entries (by odometer ascending)
     * @param distanceUnit Display unit for the vehicle
     * @param volumeUnit Display unit for the vehicle
     * @return Average mileage in display units, or null if insufficient data
     */
    fun calculateAverageMileage(
        entries: List<FuelEntry>,
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): Double? {
        if (entries.size < 2) return null
        
        val mileages = mutableListOf<Double>()
        for (i in 1 until entries.size) {
            val mileage = calculateMileage(
                entries[i],
                entries[i - 1],
                distanceUnit,
                volumeUnit
            )
            mileage?.let { mileages.add(it) }
        }
        
        return if (mileages.isNotEmpty()) mileages.average() else null
    }

    /**
     * Calculate total distance traveled for a vehicle.
     * 
     * @param entries Sorted list of fuel entries
     * @param distanceUnit Display unit for the vehicle
     * @return Total distance in display units
     */
    fun calculateTotalDistance(
        entries: List<FuelEntry>,
        distanceUnit: DistanceUnit
    ): Double {
        if (entries.isEmpty()) return 0.0
        
        val first = entries.first()
        val last = entries.last()
        
        val distanceKm = last.odometer - first.odometer
        return UnitConverter.fromKilometers(distanceKm, distanceUnit)
    }

    /**
     * Calculate total fuel consumed for a vehicle.
     * 
     * @param entries List of fuel entries
     * @param volumeUnit Display unit for the vehicle
     * @return Total fuel in display units
     */
    fun calculateTotalFuel(
        entries: List<FuelEntry>,
        volumeUnit: VolumeUnit
    ): Double {
        val totalLiters = entries.sumOf { it.fuelVolume }
        return UnitConverter.fromLiters(totalLiters, volumeUnit)
    }

    /**
     * Calculate total cost for a vehicle.
     * 
     * @param entries List of fuel entries
     * @return Total cost
     */
    fun calculateTotalCost(entries: List<FuelEntry>): Double = entries.sumOf { it.fuelCost }

    /**
     * Calculate cost per distance unit.
     * 
     * @param entries Sorted list of fuel entries
     * @param distanceUnit Display unit for the vehicle
     * @param volumeUnit Display unit for the vehicle
     * @return Cost per distance unit in display units, or null if insufficient data
     */
    fun calculateCostPerDistance(
        entries: List<FuelEntry>,
        distanceUnit: DistanceUnit,
        volumeUnit: VolumeUnit
    ): Double? {
        if (entries.isEmpty()) return null
        
        val totalCost = calculateTotalCost(entries)
        val totalDistanceKm = calculateTotalDistance(entries, DistanceUnit.KM)
        
        if (totalDistanceKm <= 0) return null
        
        val distanceInDisplay = UnitConverter.fromKilometers(totalDistanceKm, distanceUnit)
        return if (distanceInDisplay > 0) totalCost / distanceInDisplay else null
    }
}
