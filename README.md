# DOOMLY (by BYREX)

**DOOMLY** is a 100% local, offline, privacy-first Android digital wellbeing application that makes people aware of how much they scroll short-form video feeds on **Instagram Reels**, **YouTube Shorts**, and **TikTok** (when installed).

---

## Core Principles & Honest Tracking

1. **100% Local & Offline**:
   - No login, no signup, no backend, no cloud sync, no ads, and no paid services.
   - `android.permission.INTERNET` is **not** requested in `AndroidManifest.xml`.
2. **App Usage Time vs. Estimated Scrolls**:
   - **App usage time**: Measured accurately using Android's `UsageStatsManager` (`PACKAGE_USAGE_STATS`).
   - **Estimated scrolls**: Because Android does not provide a direct API to count individual Reels or Shorts watched, DOOMLY estimates scrolls via an optional `AccessibilityService` (`DoomlyAccessibilityService`) that observes vertical swipe events (`TYPE_VIEW_SCROLLED`) inside short-video pager containers.
   - Every scroll count is explicitly labeled **"Estimated scrolls"** alongside an `(i)` info button explaining that the count is an estimate and may vary when host apps update.
   - If the optional Accessibility permission is off, DOOMLY continues working with **App usage time** only, and scroll counts honestly display **"Tracking unavailable"** with an enable button (never a fake `0`).
   - Uninstalled apps (such as TikTok when absent from the device) are detected via `PackageManager` and honestly display **"Not installed"**.
3. **Zero Screen Content Access**:
   - `android:canRetrieveWindowContent="false"` is enforced in `res/xml/accessibility_service_config.xml`.
   - DOOMLY never reads, stores, or transmits screen text, captions, usernames, comments, or messages. Only swipe counts and timestamps are stored in local SQLite (`Room`).
4. **Playful Brain Status & Daily Milestones**:
   - `0–99`: **Fresh Brain**
   - `100–199`: **A Bit Bad**
   - `200–299`: **Worse**
   - `300+`: **Bad**
   - Includes a one-time-per-day system notification when crossing **200**, **300**, and **500** combined daily estimated scrolls, and displays the disclaimer: *"These labels are playful awareness indicators, not medical or scientific assessments."*

---

## Project Architecture

```
app/src/main/
├── AndroidManifest.xml                  # Offline manifest (UsageStats, Notifications, Queries, AccessibilityService)
├── java/com/example/
│   ├── MainActivity.kt                  # Edge-to-edge host, bottom nav, midnight rollover BroadcastReceiver
│   ├── data/
│   │   ├── PlatformType.kt              # PlatformType (Instagram, YouTube, TikTok), BrainStatus, TrackingStatus
│   │   ├── DoomlyEntities.kt            # Room entities (DailyScrollEntity, HourlyScrollEntity, DailyMilestoneEntity)
│   │   ├── DoomlyDao.kt                 # Reactive Flow queries and suspend mutations
│   │   ├── DoomlyDatabase.kt            # Local Room SQLite singleton (doomly_local.db)
│   │   ├── DoomlyPreferences.kt         # Local SharedPreferences state store
│   │   └── DoomlyRepository.kt          # Midnight-aware scroll recording, 200/300/500 milestone detection, resets
│   ├── tracking/
│   │   ├── DoomlyAccessibilityService.kt # Conservative vertical feed swipe estimator (canRetrieveWindowContent=false)
│   │   ├── PlatformSystemMonitor.kt     # Real PackageManager & UsageStatsManager queries
│   │   └── MilestoneNotificationManager.kt # Local notification channel & alerts at 200, 300, and 500 scrolls
│   └── ui/
│       ├── DoomlyScreen.kt              # All 14 screen routes and UI state models
│       ├── DoomlyViewModel.kt           # MVVM state holder, 7/30-day aggregation, midnight watcher
│       ├── components/
│       │   ├── DoomlyMascot3D.kt        # Interactive 3D-styled Brain Mascot with mood & counter-up animations
│       │   └── DoomlyCommonComponents.kt # Logo mark, Estimated scrolls + (i) dialog, custom Canvas bar charts
│       ├── screens/
│       │   ├── IntroAndPermissionsScreens.kt   # Screens 1–3: Splash, Onboarding, Permission Explanation
│       │   ├── DashboardAndPlatformScreens.kt  # Screens 4–7: Dashboard, Instagram, YouTube, TikTok details
│       │   ├── AnalyticsAndMascotScreens.kt    # Screens 8–11: Daily Statistics, 7/30d History, Brain Status, Mascot
│       │   └── SettingsPrivacyResetScreens.kt  # Screens 12–14: Settings, Privacy Info, Reset & Data Management
│       └── theme/
│           ├── Color.kt                 # Near-black (#0A0A0C), charcoal (#131318), controlled crimson (#E53935)
│           ├── Type.kt                  # Bundled Space Grotesk & Plus Jakarta Sans typography
│           └── Theme.kt                 # Material 3 dark color scheme
└── res/
    ├── font/                            # Bundled space_grotesk.ttf & plus_jakarta_sans.ttf
    ├── values/strings.xml               # String resources & disclaimers
    └── xml/accessibility_service_config.xml
```

---

## How to Build a Debug APK

1. Open a terminal in the project root directory.
2. Run the Gradle debug assembly task:
   ```bash
   gradle :app:assembleDebug
   ```
   *(Or `./gradlew :app:assembleDebug` on a local workstation with a Gradle wrapper).*
3. The compiled debug APK will be generated at:
   ```text
   app/build/outputs/apk/debug/app-debug.apk
   ```

---

## How to Build a Signed Release APK

1. Generate an upload/release keystore (if you do not already have one):
   ```bash
   keytool -genkeypair -v \
     -keystore my-upload-key.jks \
     -alias upload \
     -keyalg RSA \
     -keysize 2048 \
     -validity 10000
   ```
2. Provide the keystore path and credentials via environment variables (matching `app/build.gradle.kts` `signingConfigs.getByName("release")`) and run `assembleRelease`:
   ```bash
   export KEYSTORE_PATH="/absolute/path/to/my-upload-key.jks"
   export STORE_PASSWORD="your_keystore_password"
   export KEY_PASSWORD="your_key_password"
   gradle :app:assembleRelease
   ```
3. The signed release APK will be output to:
   ```text
   app/build/outputs/apk/release/app-release.apk
   ```

---

## How to Install the APK on an Android Phone

### Option A: Install via ADB (USB Debugging)
1. Enable **Developer options** and **USB debugging** on your Android phone.
2. Connect the phone via USB and run:
   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```

### Option B: Direct File Transfer (Sideloading)
1. Copy `app-debug.apk` (or `app-release.apk`) to your phone's `Downloads` folder via USB, Nearby Share / Quick Share, or local file transfer.
2. Tap the `.apk` file in your phone's **Files** app.
3. If prompted, allow your Files app to **"Install unknown apps"** and tap **Install**.

---

## How to Enable Permissions (Including Android 13+ "Allow Restricted Settings")

### 1. Enable App Usage Time (`Usage Access`)
1. Open **DOOMLY** and navigate to the **Permissions & Setup** screen.
2. Read the pre-permission explanation and tap **Open Usage Access Settings**.
3. Select **DOOMLY** from the list and toggle **Permit usage access** to **ON**.

### 2. Enable Estimated Scrolls (`Accessibility Service`) & Bypass Android 13+ "Restricted Setting"
When you install an APK outside of the Google Play Store (sideloading) on **Android 13, 14, 15, or newer**, Android blocks Accessibility Services by default with a *"Restricted setting"* dialog until you explicitly authorize the sideloaded app:

1. In **DOOMLY**, tap **Open Accessibility Settings** on the Permission Setup screen.
2. Under **Downloaded apps**, tap **DOOMLY Scroll Estimator**.
3. Tap the toggle once — Android will display the **"Restricted setting"** security dialog. Tap **OK**.
4. Return to DOOMLY and tap **Open DOOMLY App Info (for ⋮ Menu)** *(or open Android **Settings → Apps → See all apps → DOOMLY**)*.
5. Tap the **3-dot menu (`⋮`)** in the **top-right corner** of the DOOMLY App Info screen.
6. Tap **Allow restricted settings** and verify with your device PIN, pattern, or fingerprint.
7. Return to **Settings → Accessibility → Downloaded apps → DOOMLY Scroll Estimator** and toggle **Use DOOMLY Scroll Estimator** to **ON**.
8. Confirm the system prompt. DOOMLY will immediately show **Active** status on your dashboard.
