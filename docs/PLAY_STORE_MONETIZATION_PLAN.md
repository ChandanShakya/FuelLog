# FuelLog: Google Play + Monetization Plan

## 1. Will people use it? (honest verdict)

**Demand is real but the niche is crowded.** Fuelio (reported 1M+ users, 4.6 stars, ad-free) and Drivvo (4.7 stars, paid PRO tier with sync/backup/CSV) already own "fuel log + mileage". India/Nepal-style markets also have many "daily petrol price" apps.

FuelLog will not win on features. It can win on a **positioning gap**:

| Angle | Why it matters |
|---|---|
| 100% offline, no account, no ads | Privacy-minded users; Fuelio/Drivvo push accounts and sync |
| ~1.3 MB APK | Low-end phones, limited data (South Asia) |
| Bikes, scooters, EV in one app | Two-wheelers dominate India/Nepal; most Western apps are car-first |
| Nearby pumps with no map SDK | Free, no API key |
| Open source (MIT) | Trust, GitHub/F-Droid/Obtainium audience |

**Expected outcome:** niche tool. Realistic target: 5k-20k installs in year 1, with a ~1-3% paid conversion. Treat revenue as supplementary, not a salary. Validate before investing more (section 5).

## 2. Monetization model: free core + one-time "Pro"

Do **not** use ads. Ads contradict the main selling point and add SDK size and a privacy policy burden.

**Free forever:** unlimited vehicles/entries, mileage, charts, CSV export, nearby pumps.

**Pro (one-time purchase, suggested USD 2.99; use Play regional pricing, e.g. about INR 99-149):**
- Home-screen widget and reminders (service due, next fill)
- Local scheduled backups to Drive/SAF folder
- Extra reports: yearly cost PDF, per-trip cost history, tax/work-trip tagging
- Themes, no "support the dev" nag
- Optional tip jar ("Buy me a chai") as separate in-app product

One-time beats subscription here: no backend, no recurring value to justify a subscription, and users distrust subscriptions for simple utilities.

Keep the Codeberg/GitHub APK **fully unlocked** (build flavor `foss`). Pro only gates the Play build. This keeps the open-source promise and avoids community backlash.

## 3. Work needed before submission

1. **Target API**: app targets 35. Check Play's current target-API deadline in Play Console policy page; raise to the required level (likely 36) and re-test.
2. **Play Billing Library**: add only to the `play` flavor (small size cost). Products: `pro_unlock` (non-consumable), `tip_small` (consumable).
3. **Product flavors**: `play` (billing) and `foss` (no billing, GitHub/Codeberg/Obtainium).
4. **Signing**: enrol in Play App Signing. Upload key = current keystore. Stop hardcoding keystore passwords in `app/build.gradle.kts`; read from env/secrets.
5. **Nearby pumps backend risk**: public Overpass API has fair-use limits. At scale, add caching, a mirror fallback, or a tiny cached proxy. Show a friendly error on 429/504.
6. **Privacy policy** (required: app requests location + internet). Host on the website: location used on-device only for nearby search; coordinates sent to Overpass; no accounts, no analytics.
7. **Data safety form**: location (approximate/precise) collected, not shared with Google; no data stored on a server by you. Be accurate: the Overpass request does leave the device.
8. **Content rating** questionnaire, **ads declaration** (no ads), target audience 18+.
9. **Store listing**: title "FuelLog: Mileage & EV Log", short description, 8 screenshots (already in `website/public/shots`), 1024x500 feature graphic, 512 px icon, 30-s promo video (the 9:16 reel works for YouTube Shorts).
10. **App Bundle**: CI already builds `app-release.aab`.

## 4. Launch path (new personal developer account)

- Pay the one-time USD 25 Play developer fee; verify identity.
- New personal accounts must run a **closed test with at least 12 testers opted in for 14 continuous days** before applying for production access (Google Play Console Help). Recruit 20 to be safe; if testers drop below 12 the clock may reset.
- Where to find testers: r/androidapps, r/developersIndia, r/IndiaTech, Nepali biker/EV Facebook groups, Telegram groups, the GitHub README and website.
- Then apply for production, staged rollout 10% -> 50% -> 100%.

Timeline: week 1 prep and flavors; week 2 closed test starts; week 4 apply for production; week 5 launch.

## 5. Validate demand cheaply (do before heavy investment)

- Post the reel + website link in 3-4 communities; track website visits and APK downloads (GitHub release download counts are public).
- Add a one-line "Would you pay for Pro?" feedback link in the app/website.
- **Go signal:** 300+ APK downloads or 50+ closed-test installs in 30 days with 4+ star feedback. **No-go:** under that, keep it a free open-source project and skip Pro.

## 6. Growth

- Play ASO keywords: "mileage tracker", "petrol log", "bike mileage", "EV charging log", "fuel expense".
- Short videos (Reels, Shorts, TikTok): "How much does your bike really cost per km?"; before/after of a fill-up log.
- Listings on F-Droid / IzzyOnDroid (no cost, fits the open-source audience).
- Regional pricing and localization (Hindi, Nepali) once English is stable; removes the English-only resource filter in `build.gradle.kts`.

## 7. Revenue sketch (illustrative, not a forecast)

| Installs (yr 1) | Pro conversion | Price (net after 15% Play cut) | Revenue |
|---|---|---|---|
| 5,000 | 1% | USD 2.50 | ~USD 125 |
| 20,000 | 2% | USD 2.50 | ~USD 1,000 |
| 100,000 | 2% | USD 2.50 | ~USD 5,000 |

Costs: USD 25 fee, domain, your time. Break-even is easy; meaningful income needs very large reach, so also consider the plan a portfolio and brand asset.

## 8. Risks

- Crowded category; low discoverability for new apps.
- Overpass outages degrade the nearby feature.
- Store policy changes (target API, testing rules) can delay launch.
- Splitting `play`/`foss` adds maintenance; keep the gated code small.
