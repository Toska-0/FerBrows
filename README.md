# FerBrows

FerBrows is an Android browser prototype with a dark AMOLED interface and lime accents. It uses the Chromium/Blink engine provided by Android System WebView.

## Current functionality

- Multiple tabs with a separate WebView instance per tab
- Restores the open tab list and selected tab after process restart; pages are reloaded by URL (the full in-page session/scroll position is not yet restored)
- Address bar that distinguishes HTTP(S) URLs from search queries and rejects unsupported schemes
- Back, forward, reload and home navigation
- History with page titles and visit timestamps, limited to the latest 100 entries
- Persistent bookmarks with remove and reorder controls
- System download manager integration and website file chooser
- Safe Browsing where supported by the Android WebView provider
- Third-party cookies disabled and mixed HTTP content blocked
- Main-frame loading errors shown in the browser UI

## Privacy and security status

FerBrows is **not yet a fully private browser**. It does not currently provide an incognito mode, tracker/ad blocking, VPN/proxy service, sync, or extension support. Safe Browsing helps warn about known dangerous sites; it does not block trackers and does not make an HTTP connection secure.

JavaScript remains enabled for website compatibility. File URL access is disabled, mixed content is blocked, and third-party cookies are disabled. The address bar distinguishes HTTPS from HTTP; HTTPS protects the connection in transit but is not a guarantee that a site itself is trustworthy.

## Build

Requirements: JDK 17, Gradle 8.9 and Android SDK 35.

```bash
gradle testDebugUnitTest
gradle assembleDebug
```

The APK is created at `app/build/outputs/apk/debug/app-debug.apk`.

## GitHub Actions

The workflow runs unit tests and builds a debug APK on pushes and pull requests to `main`, and can also be started manually from the Actions tab. Download the `FerBrows-debug-apk` artifact from a successful run.

## Engine note

This app uses Android System WebView, whose rendering engine is Chromium/Blink. It is not a separately bundled Chromium build; the installed WebView provider supplies engine updates.

## Project status

Early development build: 0.1.0. The current implementation is a prototype. Full WebView session restoration, private browsing, tracker protection, robust permission management, sync, extension support, and automated UI/instrumentation tests remain future work.
