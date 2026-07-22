package com.chandanshakya.fuellog.di

import android.content.Context
import androidx.room.Room
import com.chandanshakya.fuellog.data.db.AppDatabase
import com.chandanshakya.fuellog.data.db.FuelEntryDao
import com.chandanshakya.fuellog.data.db.UserSettingsDao
import com.chandanshakya.fuellog.data.db.VehicleDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(context, AppDatabase::class.java, "fuellog-db")
            .fallbackToDestructiveMigration()
            .build()
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
}
