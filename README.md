# FuelLog

A lightweight Android fuel tracking app built with Jetpack Compose, Room, and Hilt.

## Features

- **Vehicle Management** - Add multiple vehicles with custom types (Car, Bus, Scooter, Bike, Truck, Jeep)
- **Fuel Entry Tracking** - Log fuel fills with odometer, volume, cost, and full-tank marker
- **Fuel Pump Tracking** - Optionally record which pump/station you refueled at, with autocomplete from previously-used pumps (edit/delete pump names)
- **Tank Capacity** - Set estimated tank capacity per vehicle, refined automatically from full-tank fill history
- **Next Fill-Up Prediction** - See remaining distance (~X km left) and predicted date based on recent average mileage and tank capacity
- **Odometer Check-ins** - Log standalone odometer readings between fill-ups to keep predictions fresh
- **Auto-Calculation** - Enter any 2 of {volume, rate, cost} and the third is calculated automatically
- **Unit Conversion** - Supports km/miles and liters/gallons with automatic data conversion when units change
- **Global Currency** - Set your currency once, applies to all vehicles
- **Insights & Charts** - Mileage trends, cost analysis, fuel price tracking, per-pump mileage comparisons, and tank info with capacity suggestions
- **Pump Detail View** - Drill into any pump's full fill-up history and mileage trend chart
- **Minimal Size** - ~1.2MB release APK

## Tech Stack

- **UI:** Jetpack Compose + Material 3
- **Database:** Room (with migrations)
- **DI:** Hilt
- **Architecture:** MVVM with Kotlin Flows
- **Testing:** JUnit 4, Compose UI Testing, Espresso

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

## Testing

```bash
# Run all unit tests
./gradlew testDebugUnitTest

# Run specific test class
./gradlew testDebugUnitTest --tests "com.chandanshakya.fuellog.util.PumpMileageCalculatorTest"

# Build instrumentation test APK (requires device/emulator)
./gradlew assembleDebugAndroidTest

# Run instrumentation tests on connected device
./gradlew connectedDebugAndroidTest
```

### Test Coverage

- **Unit tests** (`src/test/`) - Pure Kotlin logic: mileage calculators, tank capacity learner, fill-up predictor, currency formatting, unit conversion, validation, ViewModel logic
- **DAO integration tests** (`src/androidTest/`) - Room database operations, schema migrations, foreign key behavior
- **Compose UI tests** (`src/androidTest/`) - Dialog interactions, autocomplete dropdowns, button callbacks
- **End-to-end navigation tests** (`src/androidTest/`) - Full user flows through all screens: Vehicles → Fuel Log → Insights → Pump Detail

## Project Structure

```
app/src/main/java/com/chandanshakya/fuellog/
├── data/
│   ├── db/          # Room DAOs, database, migrations
│   └── model/       # Data classes (Vehicle, FuelEntry, FuelPump, OdometerReading, UserSettings)
├── ui/
│   ├── components/  # Reusable composables (AddFuelEntryDialog, AppTextField, etc.)
│   ├── screens/     # Screen composables (FuelLogScreen, InsightsScreen, PumpDetailScreen, etc.)
│   ├── chart/       # Canvas-based chart composables (LineChart, MileageChart, FuelPriceChart)
│   └── navigation/  # NavRoutes, AppNavHost
├── viewmodel/       # ViewModels (FuelLogViewModel, InsightsViewModel, PumpInsightsViewModel, etc.)
├── util/            # Helpers (UnitConverter, CurrencyFormatter, MileageCalculator, PumpMileageCalculator, TankCapacityLearner, NextFillUpPredictor)
└── di/              # Hilt modules (AppModule)
```

## Database

Room database with 5 tables and version 9:

| Table | Description |
|-------|-------------|
| `vehicles` | Vehicle profiles (name, type, distance/volume units, tank capacity) |
| `fuel_entries` | Fuel fill records (odometer, volume, cost, date, full-tank flag, FK to vehicle + pump) |
| `fuel_pumps` | Fuel pump/station names |
| `odometer_readings` | Standalone odometer check-ins (no fuel purchase) |
| `user_settings` | Global settings (currency, default units) |

Migrations are handled explicitly (see `AppDatabase.MIGRATION_7_8`, `MIGRATION_8_9`). `fallbackToDestructiveMigration()` is retained as a safety net for versions without an explicit migration path.

## Capacity Learning

The app learns your tank capacity from full-tank fill history:
- When you mark fill-ups as "Full tank?", the app tracks fuel volumes added between consecutive full fills
- A median-based algorithm computes a suggested capacity with confidence levels (Low/Medium/High)
- Suggestions appear on the Insights screen with a one-tap "Apply" button — never auto-overwrites your number

## License

MIT
