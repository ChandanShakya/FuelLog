package com.chandanshakya.fuellog.data.model

import androidx.annotation.DrawableRes
import com.chandanshakya.fuellog.R

/**
 * Distance units for fuel efficiency calculations.
 */
enum class DistanceUnit {
    KM,
    MILES
}

/**
 * Volume / energy units for fuel quantity. [KWH] is used for electric vehicles.
 */
enum class VolumeUnit {
    LITERS,
    GALLONS,
    KWH;

    val isEnergy: Boolean get() = this == KWH
}

/**
 * Fuel / energy type for a vehicle.
 */
enum class FuelType(val label: String) {
    PETROL("Petrol"),
    DIESEL("Diesel"),
    CNG("CNG"),
    ELECTRIC("Electric");

    val isElectric: Boolean get() = this == ELECTRIC
}

/**
 * Vehicle type categories with associated icons.
 */
enum class VehicleType(@DrawableRes val iconRes: Int, val label: String) {
    CAR(R.drawable.ic_vehicle_car, "Car"),
    BUS(R.drawable.ic_vehicle_bus, "Bus"),
    SCOOTER(R.drawable.ic_vehicle_scooter, "Scooter"),
    BIKE(R.drawable.ic_vehicle_bike, "Bike"),
    TRUCK(R.drawable.ic_vehicle_truck, "Truck"),
    JEEP(R.drawable.ic_vehicle_jeep, "Jeep")
}

/**
 * Currency codes supported for display formatting.
 */
val SUPPORTED_CURRENCIES = setOf("USD", "EUR", "INR", "NPR")
