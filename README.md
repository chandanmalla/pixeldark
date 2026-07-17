# PixelDarken

Android overlay app that darkens configured screen coordinates **on top of every app**.

## Requirements

- Android Studio (Meerkat+ recommended for Android 17 / API 37)
- Device or emulator with Android 8.0+ (`minSdk 26`)
- “Display over other apps” permission

## Open & run

1. Open `PixelDarken` in Android Studio
2. Let Gradle sync (install SDK Platform 37 if prompted)
3. Run on a device/emulator
4. Tap **Grant overlay permission** → allow PixelDarken
5. Enter **X / Y** (pixels from top-left), optional radius & alpha
6. Tap **Add spot**, then **Start everywhere**

The dark spots stay visible while you switch apps. Touches pass through the overlay.

Long-press a list item to remove a spot. Use the notification **Stop** action or the in-app **Stop** button to tear down the overlay.

## Notes

- Coordinates are absolute screen pixels
- Radius `1` ≈ a single darkened pixel area; use larger radius for a bigger blot
- Alpha `255` = fully opaque black; lower = lighter darkening
