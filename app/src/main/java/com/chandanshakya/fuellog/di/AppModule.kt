package com.chandanshakya.fuellog.di

import android.content.Context
import androidx.room.Room
import com.chandanshakya.fuellog.data.db.AppDatabase
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import com.chandanshakya.fuellog.data.repository.FuelRepository
import com.chandanshakya.fuellog.data.repository.FuelRepositoryImpl
import com.chandanshakya.fuellog.data.repository.SettingsRepository
import com.chandanshakya.fuellog.data.repository.SettingsRepositoryImpl
import com.chandanshakya.fuellog.data.repository.VehicleRepository
import com.chandanshakya.fuellog.data.repository.VehicleRepositoryImpl
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module providing database, DAOs, and repositories.
 * 
 * Single module for all data layer dependencies as per requirements.
 */
@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "fuellog-db"
        ).build()
    }

    @Provides
    @Singleton
    fun provideVehicleDao(database: AppDatabase): VehicleDao = database.vehicleDao()

    @Provides
    @Singleton
    fun provideFuelEntryDao(database: AppDatabase): FuelEntryDao = database.fuelEntryDao()

    @Provides
    @Singleton
    fun provideUserSettingsDao(database: AppDatabase): UserSettingsDao = database.userSettingsDao()

    @Provides
    @Singleton
    fun provideVehicleRepository(vehicleDao: VehicleDao): VehicleRepository = 
        VehicleRepositoryImpl(vehicleDao)

    @Provides
    @Singleton
    fun provideFuelRepository(fuelEntryDao: FuelEntryDao): FuelRepository = 
        FuelRepositoryImpl(fuelEntryDao)

    @Provides
    @Singleton
    fun provideSettingsRepository(userSettingsDao: UserSettingsDao): SettingsRepository = 
        SettingsRepositoryImpl(userSettingsDao)
}
