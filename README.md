# RoutineTrack 📚

> A sleek, focused academic routine & study tracker featuring Bengali-English calendar support, live Google Sheets synchronization, subject strength analysis, and full-screen incoming study call alarms.

---

## 🌟 Key Features

### 📅 87-Day Academic Calendar (5 Oct – 30 Dec 2026)
- **Exact Date Span**: Runs strictly from **5 October 2026** to **30 December 2026** (87 days matching your curriculum).
- **Bengali & English Dates**: Displays Bengali numerals and weekdays alongside standard dates (e.g. `৫ - সোম - Oct`).
- **Friday-to-Thursday Weeks**: 13 discrete academic weeks starting every Friday and concluding on Thursday.
- **Soft Pastel Palettes**: Each week has its own soft, non-deep pastel color theme (Week 1 Sky Blue, Week 2 Mint, Week 3 Lavender, Week 4 Peach, etc.).

### 📱 Adaptive Split-Screen on Phone Rotation
- **Portrait Mode**: Top horizontal date carousel with daily subjects and to-do checklist below.
- **Landscape / Rotation**: Transforms into a side-by-side split screen with scrollable dates on the left and selected subject tasks on the right.

### 📚 11 Academic Subject Categories
- Dedicated 1-tap subject selector chips:
  - Physics 1st & Physics 2nd
  - Higher Math 1st & Higher Math 2nd
  - Chemistry 1st & Chemistry 2nd
  - Biology 1st & Biology 2nd
  - Bangla, English, ICT
- Multi-day routine generator with format: `[Subject]-[Day#]` (e.g. `Physics 1st-1`, `Physics 1st-2` ...).

### 📊 Strengths & Weakness Analysis
- **Studied Most (Strength)**: Identifies subjects with top completion rates and study consistency.
- **Weakness (Needs Attention)**: Highlights subjects with low completion or high pending task volume.
- **11-Subject Scorecard**: Complete progress breakdown with visual indicators and diagnostic performance feedback.

### 📞 Study Call (Phone Call-Style Wake-Up Alarms)
- Wakes up the phone even when the screen is locked or turned off.
- Realistic caller interface (*"Study Supervisor"*), real-time % work remaining, and today pending works list.
- **Direct 1-Tap Completion**: Mark tasks done directly on the incoming call screen without opening menus.
- **Active Schedule**: Operates strictly between **09:00 AM and 11:59 PM** (no night-time disturbances).
- **3 Intensity Presets**:
  - High Study (10 calls/day)
  - Middle Study (5 calls/day)
  - Low Study (3 calls/day)
- Includes a 5-second delayed test mode for lock-screen debugging.

### ☁️ Live Google Sheets Sync
- Real-time cloud sync to Google Spreadsheet: `1fDEbV_HayCxgYNMjC1lopt4KIWFVnUnBFbxeWaDdOE4`.
- Dual support for **Google Apps Script Webhooks** and **Google OAuth 2.0 (Sheets API v4)**.
- In-app 1-click test write verification button.

---

## 🛠️ Architecture & Tech Stack

- **Language**: Kotlin 100%
- **UI Toolkit**: Jetpack Compose (Material Design 3)
- **Local Database**: Room Database (SQLite) + SharedPreferences
- **Networking**: OkHttp 4
- **Concurrency**: Coroutines & Reactive StateFlow
- **System Services**: AlarmManager, Foreground Service, WakeLock & FullScreenIntent

---

## 🚀 Getting Started

1. **Install App**: Transfer and install `RoutineTrack-debug.apk` on your Android device.
2. **Link Google Sheet**: Open **Settings** > copy the provided Apps Script into your sheet > paste the Web App URL > tap **Test Write Row**.
3. **Enable Study Calls**: In **Settings**, toggle **Study Call** ON and grant *"Display Over Other Apps"* to allow full-screen wake-ups.

---

*RoutineTrack • Built with Google AI Studio*
