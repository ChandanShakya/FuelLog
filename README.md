# FuelLog

A lightweight Android fuel tracking app built with Jetpack Compose, Room, and Hilt.

## Features

- **Vehicle Management** - Add multiple vehicles with custom types (Car, Bus, Scooter, Bike, Truck, Jeep)
- **Fuel Entry Tracking** - Log fuel fills with odometer, volume, cost, and notes
- **Auto-Calculation** - Enter any 2 of {volume, rate, cost} and the third is calculated automatically
- **Unit Conversion** - Supports km/miles and liters/gallons with automatic data conversion when units change
- **Global Currency** - Set your currency once, applies to all vehicles
- **Insights & Charts** - Mileage trends, cost analysis, and fuel price tracking
- **Minimal Size** - ~1.2MB release APK

## Tech Stack

- **UI:** Jetpack Compose + Material 3
- **Database:** Room
- **DI:** Hilt
- **Architecture:** MVVM with Kotlin Flows

## Run Locally

**Prerequisites:** [Android Studio](https://developer.android.com/studio)

1. Clone the repository
2. Open in Android Studio
3. Run on emulator or physical device (minSdk 26)

## Build

```bash
# Debug
./gradlew assembleDebug

# Release
./gradlew assembleRelease
```

## Project Structure

```
app/src/main/java/com/chandanshakya/fuellog/
├── data/
│   ├── db/          # Room DAOs and database
│   └── model/       # Data classes (Vehicle, FuelEntry, UserSettings)
├── ui/
│   ├── components/  # Reusable composables
│   ├── screens/     # Screen composables
│   └── chart/       # Chart composables
├── viewmodel/       # ViewModels
├── util/            # Helpers (UnitConverter, CurrencyFormatter, etc.)
└── di/              # Hilt modules
```

## License

MIT
