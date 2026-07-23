package com.chandanshakya.fuellog

import androidx.lifecycle.AbstractSavedStateViewModelFactory
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.savedstate.SavedStateRegistryOwner
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.viewmodel.FuelLogViewModel
import com.chandanshakya.fuellog.viewmodel.InsightsViewModel
import com.chandanshakya.fuellog.viewmodel.SettingsViewModel
import com.chandanshakya.fuellog.viewmodel.VehiclesViewModel

object AppViewModelFactory {
    fun vehicles(
        vehicleDao: VehicleDao,
        userSettingsDao: UserSettingsDao,
        fuelEntryDao: FuelEntryDao
    ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return VehiclesViewModel(vehicleDao, userSettingsDao, fuelEntryDao) as T
        }
    }

    fun fuelLog(
        vehicleDao: VehicleDao,
        fuelEntryDao: FuelEntryDao,
        userSettingsDao: UserSettingsDao,
        owner: SavedStateRegistryOwner
    ): AbstractSavedStateViewModelFactory = object : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return FuelLogViewModel(fuelEntryDao, vehicleDao, userSettingsDao, handle) as T
        }
    }

    fun insights(
        vehicleDao: VehicleDao,
        fuelEntryDao: FuelEntryDao,
        userSettingsDao: UserSettingsDao,
        owner: SavedStateRegistryOwner
    ): AbstractSavedStateViewModelFactory = object : AbstractSavedStateViewModelFactory(owner, null) {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(key: String, modelClass: Class<T>, handle: SavedStateHandle): T {
            return InsightsViewModel(fuelEntryDao, vehicleDao, userSettingsDao, handle) as T
        }
    }

    fun settings(
        userSettingsDao: UserSettingsDao
    ): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(userSettingsDao) as T
        }
    }
}
