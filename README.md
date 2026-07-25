# FuelLog

A fuel tracking app for Android. Log fill-ups, track mileage across multiple vehicles, and get predictions for your next refuel.

Built with Jetpack Compose, Room, and manual dependency injection. Ships at 1.2MB.

## Features

**Vehicle and fuel management**
- Multiple vehicles with type classification (car, bus, scooter, bike, truck, jeep)
- Fuel entry logging with odometer, volume, cost, and full-tank marker
- Per-pump tracking with autocomplete from previously used stations
- Edit and delete pump names

**Predictions and analysis**
- Tank capacity learning from full-tank fill history (median-based, with confidence levels)
- Next fill-up prediction: remaining distance and estimated date
- Mileage trends, cost analysis, and fuel price tracking over time
- Per-pump mileage comparisons with drill-down detail view
- Standalone odometer check-ins to keep predictions accurate between fill-ups

**Practical tools**
- Auto-calculation: enter any two of volume, rate, cost and the third is computed
- Unit support: km/miles, liters/gallons with automatic conversion on unit change
- Global currency setting applied across all vehicles
- Backup and restore via JSON export/import (Storage Access Framework)
- Clear all data option with confirmation

## Tech stack

| Layer | Technology |
|-------|-----------|
| UI | Jetpack Compose + Material 3 |
| Database | Room (5 tables, version 11) |
| DI | Manual (AppContainer pattern) |
| Navigation | Sealed class + AnimatedContent (no Navigation Compose) |
| Architecture | MVVM with Kotlin Flows |
| Build | Kotlin, KSP, R8 full mode |

## Getting started

Requires [Android Studio](https://developer.android.com/studio) and minSdk 26.

```bash
git clone <repo-url>
cd FuelLog
./gradlew assembleDebug
```

Install the debug APK on a device or emulator.

## Building

```bash
./gradlew assembleDebug      # debug build
./gradlew assembleRelease    # release build (requires release.keystore)
```

Release builds use R8 minification, resource shrinking, and locale stripping (English only).

## Testing

```bash
./gradlew test                           # all unit tests
./gradlew testDebugUnitTest              # debug unit tests only
./gradlew connectedDebugAndroidTest      # instrumentation tests (device required)
```

Test coverage:
- Unit tests: mileage calculators, tank capacity learner, fill-up predictor, currency formatting, unit conversion, validation, ViewModel logic
- Instrumentation tests: Room operations, Compose UI interactions, full navigation flows across all screens

## Project structure

```
app/src/main/java/com/chandanshakya/fuellog/
  data/
    backup/       JSON export/import for backup/restore
    db/           Room database, DAOs, type converters
    model/        Entity classes and enums
  ui/
    chart/        Canvas-based line charts
    components/   Reusable composables (dialogs, text fields, badges, cards)
    navigation/   Screen sealed class, manual nav host with AnimatedContent
    screens/      Screen composables
  viewmodel/      ViewModels with factory-based instantiation
  util/           Unit converter, currency formatter, mileage calculator,
                  tank capacity learner, fill-up predictor
  di/             AppContainer (manual dependency injection)
```

## Database

Five tables with foreign key constraints:

| Table | Purpose |
|-------|---------|
| `vehicles` | Vehicle profiles: name, type, distance/volume units, tank capacity |
| `fuel_entries` | Fill records: odometer, volume, cost, date, full-tank flag, FK to vehicle and pump |
| `fuel_pumps` | Named fuel pumps/stations |
| `odometer_readings` | Standalone odometer check-ins between fill-ups |
| `user_settings` | Global defaults: currency, distance unit, volume unit |

Migrations are handled explicitly. `fallbackToDestructiveMigration()` is kept as a safety net for development builds.

## How capacity learning works

When you mark a fill-up as "full tank", the app records the fuel volume added. Between two consecutive full-tank fill-ups, the volume added approximates the actual tank capacity. The algorithm computes a median-based suggestion with confidence levels (low/medium/high) based on how many full-tank pairs are available. Suggestions appear on the Insights screen and are applied manually -- the app never overwrites your value.

## License

MIT
