# FuelLog

**[Website](https://fuellog.chandanshakya.com.np)** · **[Download latest APK](https://github.com/ChandanShakya/FuelLog/releases/latest/download/app-release.apk)** · **[Add to Obtainium](obtainium://app/https://github.com/ChandanShakya/FuelLog)** · **[Repository](https://github.com/ChandanShakya/FuelLog)**

A fuel tracking app for Android. Log fill-ups, track mileage across multiple vehicles, and get predictions for your next refuel.

Built with Jetpack Compose, Room, and manual dependency injection. Ships at ~1.2MB.

Landing page source: [`website/`](website/) (Astro, black-and-white neubrutalism). Obtainium URL: `obtainium://app/https://github.com/ChandanShakya/FuelLog`

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
- Standalone odometer check-ins — a new reading reduces remaining range and updates the predicted date/odometer
- Each fuel log card shows the efficiency earned by *that* refuel (distance since previous fill ÷ volume added at this fill)

**Practical tools**
- Auto-calculation: enter any two of volume, rate, cost and the third is computed
- Unit support: km/miles, liters/gallons/kWh; changing a vehicle's units converts fill history, odometer readings, and tank capacity
- EV support: fuel type (petrol/diesel/CNG/electric/hybrid), kWh volume, battery capacity, charge wording
- Reserve threshold: prediction stops at your reserve level, not theoretical empty
- Trip cost calculator: planned distance → energy and cost from recent mileage + last rate
- Nearby fuel pumps: OSM list within ~5 km (location + internet); tap **Go** to open your maps app — no map SDK in the APK
- Global currency setting applied across all vehicles
- Backup/restore via JSON (transactional) and CSV export for spreadsheets
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
./gradlew test               # unit tests
```

### Release signing

Passwords live in `app/build.gradle.kts` (alias `fuellog`). The keystore file itself is gitignored.

Place `release.keystore` in the project root, then:

```bash
./gradlew assembleRelease
```

**CI:** only `RELEASE_KEYSTORE_BASE64` is required (base64 of `release.keystore`). Workflows decode it to `release.keystore` before `assembleRelease`. Codeberg also needs `CODEBERG_TOKEN` to publish the release.

Release builds use R8 minification, resource shrinking, and locale stripping (English only).

## Testing

```bash
./gradlew test                           # all unit tests
./gradlew testDebugUnitTest              # debug unit tests only
./gradlew connectedDebugAndroidTest      # instrumentation tests (device required)
```

Test coverage:
- Unit tests: mileage calculators and per-entry attribution, tank capacity learner, fill-up predictor (including odometer-driven remaining range), money rounding, currency formatting (incl. concurrency), unit conversion, validation, ViewModel logic
- Instrumentation tests: Room operations, Compose UI interactions, navigation flows

## Project structure

```
app/src/main/java/com/chandanshakya/fuellog/
  data/
    backup/       JSON export/import (validated, transactional)
    db/           Room database, DAOs, type converters
    model/        Entity classes and enums
  ui/
    chart/        Canvas-based line charts
    components/   Reusable composables (dialogs, text fields, badges, cards)
    navigation/   Screen sealed class, manual nav host with AnimatedContent
    screens/      Screen composables
  viewmodel/      ViewModels with factory-based instantiation
  util/           Unit converter, currency/money helpers, mileage calculator,
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

### Migrations

Schema version is 11. **Do not bump the version without adding a real `Migration`.** The app uses `fallbackToDestructiveMigrationOnDowngrade()` only — upgrades with a missing migration fail loudly instead of silently wiping user data. Export Room schemas when adding migrations (`exportSchema` is currently false; enable it and commit schemas before the next version bump).

## How capacity learning works

When you mark a fill-up as "full tank", the app records the fuel volume added. Between two consecutive full-tank fill-ups, the volume added approximates the actual tank capacity. The algorithm computes a median-based suggestion with confidence levels (low/medium/high) based on how many full-tank pairs are available. Suggestions appear on the Insights screen and are applied manually — the app never overwrites your value.

## How next-fill prediction works

1. Recency-weighted mileage (EWMA) from adjacent fill pairs.
2. Estimated fuel in the tank at the last fill (full tank → capacity; partial → prior + volume, capped).
3. Latest odometer point = max(last fill, standalone readings).
4. Remaining distance = (fuel at last fill × mileage) − distance driven since that fill.
5. Predicted date uses average daily distance over the last 30 days of points.

Logging a new odometer reading without buying fuel immediately shortens remaining range and moves the predicted date earlier.

## Money handling

Costs are stored as `Double` for Room/JSON compatibility but are always rounded to 2 decimal places on save and when summing (`Money.roundToCents` / `Money.sumCents`). Display uses a per-thread `DecimalFormat` (thread-safe under concurrent Flow collection).

## Backup

Export writes versioned JSON. Import validates the entire payload first, then clears and inserts inside a single Room transaction — a parse/validation error leaves existing data untouched. Android Auto Backup is disabled; use in-app export for backups.

## License

MIT
