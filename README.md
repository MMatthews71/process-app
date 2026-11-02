# Process

A ~2-minute Android tool to use **after** something social goes badly or a painful memory replays.
Name the feeling, settle the body, check the story, pick one next step, then get back to your day.

- Kotlin, Jetpack Compose (Material 3), Room, DataStore, Hilt, MVVM with StateFlow
- minSdk 26, targetSdk 35
- **No INTERNET permission** (it's removed in the manifest, even if a library asks for it),
  no analytics, no AI. Cloud backup and device-to-device transfer are turned off as well, so
  all data stays on the phone.

## Status: build step 1 of 7

| Step | What | State |
|---|---|---|
| 1 | Core PROCESS flow + Room | **done, ready to test on the phone** |
| 2 | Quick notice + guardrails (recurrence, repeat groups, crisis card, heavy use, 24 h read-only) | next |
| 3 | Morning check and reminders | |
| 4 | Lessons, Therapy list, week card | |
| 5 | Obsidian export | |
| 6 | Remaining settings | |
| 7 | Compare mode | |

What works in step 1:

- **Onboarding** (once): "Use this after something happens…" → notification permission (Android 13+).
- **Home**: "Process it" and "Good thing". The Lessons, Therapy and Settings icons
  are shown but disabled until their steps are built.
- **Launcher shortcut** (long-press the app icon → "Process it") and a **Quick Settings tile**
  ("Process it"). Both open straight into the flow.
- **The PROCESS flow** (4 screens + ending, back allowed, every step skippable):
  1. *Name it*: broad feeling → precise word (optional second) → "How strong is the … right now?" 0–10, default 6.
  2. *Body*: 5 cyclic sighs with an animated circle and a haptic pulse on each phase → "Keep going"
     (up to 5 min total, shows elapsed time) or "Next" → 15 s "Let it be there…" → 10 s
     "Name 3 things you can hear or see." Skip appears after 20 s.
  3. *Story*: one camera-view sentence → story chip → "Truer version" (needs a new fact of at least 3 words)
     or "Friend". Opens on whichever tab you used last. Shows "Last time you wrote: …" for a chip you've used before.
  4. *Act*: Let it go / Repair / Test the prediction / If-then plan / Bring to therapy.
     - "Test the prediction" is greyed out with "next available <date>" for 7 days after a test.
     - 24 h later, the next time you open the app, Home asks "What happened?"
       (Better / About as predicted / Worse / Didn't ask + optional note).
  - Ending: optional re-rate → one helper suggestion ("Done it" / "Not now") → "Done. Back to your day." → the app closes.
- **Draft auto-save**: if you leave mid-flow and reopen within 30 minutes, it resumes where you left off.
- **Good thing**: "What went well?" saved as a GOOD entry.
- **Voice**: every text field has a mic button. It uses the on-device recognizer on Android 12+ when
  available, otherwise the phone's standard recognizer. The microphone permission is only requested
  on the first mic tap.

## Build and install on your phone

### With Android Studio (easiest)

1. Install a recent [Android Studio](https://developer.android.com/studio).
2. *File → Open…* and pick this folder. Let Gradle sync finish (first sync downloads dependencies).
3. On the phone, turn on developer options: *Settings → About phone → tap "Build number" 7 times*.
   Then *Settings → System → Developer options → USB debugging* on.
4. Plug the phone in by USB and accept the "Allow USB debugging?" prompt.
5. Pick the phone in the device dropdown and press **Run ▶**.

Wireless works too: *Developer options → Wireless debugging*, then in Android Studio
*Device Manager → Pair devices using Wi-Fi*.

### From the command line

With the phone connected and `adb` on your PATH:

```bash
./gradlew installDebug
```

Or build an APK to copy over and sideload:

```bash
./gradlew assembleDebug
```

The APK is written to `app/build/outputs/apk/debug/app-debug.apk`.

### Add the Quick Settings tile

Pull down the notification shade twice → pencil/edit icon → drag **Process it** into your tiles.

## Tests

Unit tests (plain JVM, no device needed). They cover flow transitions, skip/back, the 30-minute
resume window, the prediction once-per-event rule and 7-day lock, the 24 h follow-up, the 3-word
new-fact rule, prompts, the helper picker and breath timing:

```bash
./gradlew testDebugUnitTest
```

UI test: the full PROCESS flow completes in 4 screens + ending and saves one entry. It needs a
connected phone or emulator and takes about 30 s, because it waits for the body screen's Skip button:

```bash
./gradlew connectedDebugAndroidTest
```

## Settings

There's no Settings screen until step 6. Until then these defaults apply:

| Setting | Default | What it does |
|---|---|---|
| Breath cycles | 5 | Cyclic sighs before "Keep going / Next" |
| Feeling lists | spec lists | Broad feelings and their precise words on screen 1 |
| Story chips | 6 defaults | Custom chips can be added in step 6 |
| Helpers | Call family, Step into sunlight, Stretch 2 min | One is suggested at the end of each session, never the same one twice in a row when there's a choice |
| Worry slot | off | When on, "Let it go" adds "It can wait for Sunday's slot." |

The rest of the settings (morning check time, evening check, daily practice, export, biometric lock,
Compare mode, "Is it working?", JSON export, delete all) arrive in steps 3–7, and this table will
list them all.

## Project layout

```
app/src/main/java/com/mmatthews/process/
  domain/       Pure Kotlin: models, defaults, the draft, flow transitions (Flow.kt), rules
  data/         Room entities/DAOs/database, DataStore settings, repositories
  di/           Hilt modules
  ui/process/   The four screens + ending, ProcessViewModel
  ui/home, ui/onboarding, ui/good
  ui/components Shared UI: screen scaffold, voice text field, buttons, slider
  ui/platform   Android-only bits: SpeechRecognizer, vibration
  tile/         Quick Settings tile
```

The screens are stateless. The ViewModel applies pure transitions from `domain/Flow.kt` and saves
the result, so the flow logic is unit-tested without a device.

## Data

Room tables match the spec: `entries` (Entry), `story_library` (StoryLibrary), `score_log` (ScoreLog).
App settings and the in-progress draft live in DataStore. Each PROCESS session writes exactly one
entry: it's inserted when you save an action (or skip screen 4) and updated from then on, so going
back and changing your mind never creates duplicates.
