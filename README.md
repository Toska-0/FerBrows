# FerBrows

FerBrows is an Android browser prototype with a dark AMOLED interface and lime accents. It uses the Chromium/Blink engine provided by Android System WebView.

## Current functionality

- Multiple tabs with a separate WebView instance per tab
- Explicit Compose identity for the active WebView and deterministic tab state transitions
- Restores regular open tabs and the selected tab after process restart; pages are reloaded by URL (the full in-page session, scroll position and WebView back stack are not yet restored)
- Address bar that distinguishes HTTP(S) URLs from search queries and rejects unsupported schemes
- Back, forward, reload and home navigation
- History with page titles and visit timestamps, limited to the latest 100 entries; repeat visits update the existing entry
- Persistent bookmarks with remove and reorder controls
- System download manager integration and website file chooser
- Main-frame loading errors shown in the browser UI
- Basic tracker blocking for a local list of known analytics and advertising hosts
- Safe Browsing where supported by the Android WebView provider
- Third-party cookies disabled for the default WebView profile and mixed HTTP content blocked

## Private browsing

The menu can create a private tab using the AndroidX WebKit multi-profile API when the installed WebView provider supports it. Private tabs use a separate WebView profile, are excluded from saved tabs, bookmarks and history, and the private profile is deleted when the last private tab is closed or the app is started again after an unclean exit. If the installed WebView does not support multi-profile, FerBrows reports that private tabs are unavailable rather than silently falling back to a shared profile.

## Privacy and security limits

FerBrows is **not yet a fully hardened privacy browser**. Tracker blocking is a small built-in host list, not a comprehensive or automatically updated filter list; some trackers may pass and some site features may be affected. Safe Browsing helps warn about known dangerous sites; it does not block trackers and does not make an HTTP connection secure.

JavaScript remains enabled for website compatibility. File URL access is disabled, mixed content is blocked, and third-party cookies are disabled for the default profile. The address bar distinguishes HTTPS from HTTP; HTTPS protects the connection in transit but is not a guarantee that a site itself is trustworthy. No VPN/proxy, sync, or extension support is provided.

## Architecture and tests

- `MainActivity.kt`: activity, browser orchestration and WebView lifecycle
- `BrowserScreens.kt`: Compose UI
- `BrowserStore.kt`: persistent tabs, bookmarks and history
- `BrowserNavigation.kt`: URL/search input validation
- `BrowserTabReducer.kt`: deterministic tab state transitions
- `TrackerProtection.kt`: known tracker host matching

Unit tests cover address resolution, tracker matching and tab add/close/select transitions. The GitHub Actions workflow runs these tests before building the debug APK. Automated device-level UI/WebView tests are still needed.

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

Early development build: 0.1.0. The browser now has reliable tab identity and cleanup, persistent metadata, basic private profiles and a starter tracker blocklist. Full WebView session restoration, a maintained filter list, robust site-permission management, sync, extension support, and automated UI/instrumentation tests remain future work.
