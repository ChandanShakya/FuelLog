package com.chandanshakya.fuellog.data.model

import android.os.Parcel
import android.os.Parcelable
import java.time.LocalDate

data class FuelEntry(
    val id: Long = 0,
    var vehicleId: Long,
    var date: LocalDate,
    var odometer: Double,
    var fuelVolume: Double,
    var fuelCost: Double
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readLong(),
        vehicleId = parcel.readLong(),
        date = LocalDate.parse(parcel.readString() ?: LocalDate.now().toString()),
        odometer = parcel.readDouble(),
        fuelVolume = parcel.readDouble(),
        fuelCost = parcel.readDouble()
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeLong(id)
        dest.writeLong(vehicleId)
        dest.writeString(date.toString())
        dest.writeDouble(odometer)
        dest.writeDouble(fuelVolume)
        dest.writeDouble(fuelCost)
    }

    override fun describeContents() = 0

    companion object CREATOR : Parcelable.Creator<FuelEntry> {
        override fun createFromParcel(parcel: Parcel) = FuelEntry(parcel)
        override fun newArray(size: Int) = arrayOfNulls<FuelEntry>(size)
    }
}
