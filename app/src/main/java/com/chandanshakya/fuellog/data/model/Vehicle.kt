package com.chandanshakya.fuellog.data.model

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDate

data class Vehicle(
    val id: Long = 0,
    var name: String,
    var vehicleType: VehicleType = VehicleType.CAR,
    var distanceUnit: DistanceUnit = DistanceUnit.KM,
    var volumeUnit: VolumeUnit = VolumeUnit.LITERS,
    var createdAt: LocalDate = LocalDate.now()
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        name = parcel.readString() ?: "",
        vehicleType = VehicleType.valueOf(parcel.readString() ?: "CAR"),
        distanceUnit = DistanceUnit.valueOf(parcel.readString() ?: "KM"),
        volumeUnit = VolumeUnit.valueOf(parcel.readString() ?: "LITERS"),
        createdAt = LocalDate.parse(parcel.readString() ?: LocalDate.now().toString())
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeString(name)
        dest.writeString(vehicleType.name)
        dest.writeString(distanceUnit.name)
        dest.writeString(volumeUnit.name)
        dest.writeString(createdAt.toString())
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<Vehicle> {
        override fun createFromParcel(parcel: Parcel) = Vehicle(parcel)
        override fun newArray(size: Int) = arrayOfNulls<Vehicle>(size)
    }
}
