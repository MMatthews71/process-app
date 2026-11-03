# Process

A tiny Android app for after something social goes badly: breathe, split what happened from the
story, decide what you'll do next time, close.

Kotlin, Jetpack Compose (Material 3), Room. minSdk 26, targetSdk 35. No internet permission, and
backups are off, so everything stays on the phone.

## Screens

1. **Breathe**: five breaths (In 2.5 s, Sip 1 s, Out 6 s). Long-press the circle to skip.
   "Rules" in the corner opens the Rules screen.
2. **Split it**: what happened (camera view) + the story → Next.
3. **Next time**: optional "Next time I'll…" → Done saves and closes the app.

If an entry from 20+ hours ago has no answer yet, the app first asks "Was it true?" about it
(oldest first, one per open). Rules lists your saved "Next time I'll…" lines; long-press one to delete it.

## Build and install

1. Open this folder in Android Studio (*File → Open…*) and let Gradle sync finish.
   It uses the JDK bundled with Android Studio.
2. On the phone: *Settings → About phone →* tap *Build number* 7 times, then turn on
   *Developer options → USB debugging*.
3. Connect the phone by USB, accept the prompt, pick it in the device list and press **Run ▶**.

From the command line, with the phone connected:

```bash
./gradlew installDebug
```

Unit tests:

```bash
./gradlew testDebugUnitTest
```

## Versions

Pinned in `gradle/libs.versions.toml`: Gradle 8.11.1, Android Gradle Plugin 8.7.3, Kotlin 2.0.21,
Compose BOM 2024.12.01, Room 2.6.1. They don't change unless you ask, so pulling new code
doesn't trigger new downloads.
