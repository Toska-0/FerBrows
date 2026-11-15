# FerBrows

FerBrows is an Android browser prototype with a dark AMOLED interface and lime accents. It uses the Chromium/Blink engine supplied by Android System WebView.

## Features

- Multiple tabs with one WebView instance per tab and explicit Compose identity.
- Ordinary tab metadata and selected tab restored after process restart; WebView state is saved where supported and falls back to the last URL.
- Back, forward, reload, home and HTTP(S) address/search input.
- Search provider selection: Google, DuckDuckGo or Bing.
- Editable/reorderable/removable bookmarks.
- Browsing history with title, visit time, search and clear controls.
- Download list with live progress, open and remove actions, backed by Android DownloadManager; website file chooser.
- Main-frame navigation errors and a validated offline-network warning shown in the browser.
- Private tabs backed by AndroidX WebKit multi-profile when supported.
- Optional blocking of a small built-in list of known advertising and analytics hosts.
- Safe Browsing where supported by the installed WebView provider.
- JavaScript, homepage, search provider, tracker protection and reduced-motion settings. Site camera/microphone and geolocation requests are denied by default.
- Controls to clear history, WebView cache, cookies and website storage.

## Private browsing

Private tabs use a separate AndroidX WebKit profile when the installed WebView provider supports multi-profile. Private tabs are excluded from saved tabs, bookmarks and history. The private profile is deleted when the last private tab is closed or when FerBrows starts after an unclean termination. If multi-profile is unavailable, FerBrows reports that private tabs are unavailable rather than silently using the ordinary profile.

Private mode is not anonymity: websites, network operators, DNS providers, Android, the WebView provider and downloaded files may still expose or retain information. See [PRIVACY.md](PRIVACY.md).

## Privacy and security limits

FerBrows is not yet a fully hardened privacy browser. Tracker blocking is a small built-in hostname list, not a comprehensive or automatically updated filter list. Some trackers may pass and some site features may be affected. Safe Browsing is provider-dependent; it does not block trackers and does not make HTTP secure.

JavaScript is enabled by default for website compatibility and can be disabled in settings. File URL access is disabled, mixed content is blocked, and third-party cookies are disabled for the default profile. HTTPS encrypts traffic in transit but does not prove that a site itself is trustworthy. FerBrows provides no VPN, proxy, sync or extension support.

## Architecture

- `MainActivity.kt`: browser orchestration and WebView lifecycle.
- `BrowserScreens.kt`: Compose browser interface.
- `BrowserStore.kt`: persisted tabs, bookmarks and history.
- `BrowserSettings.kt` / `BrowserSettingsDialog.kt`: browser preferences and settings UI.
- `BookmarkEditDialog.kt`: bookmark editing.
- `BrowserNavigation.kt`: URL/search input validation.
- `BrowserTabReducer.kt`: deterministic tab transitions.
- `TrackerProtection.kt`: built-in tracker hostname matching.

## Build and test

Requirements: JDK 17, Gradle 8.9 and Android SDK 35.

```bash
gradle --no-daemon testDebugUnitTest
gradle --no-daemon assembleDebug
gradle --no-daemon assembleRelease
```

The debug APK is created at `app/build/outputs/apk/debug/app-debug.apk`. Release builds are not yet signed by the repository workflow; configure a protected signing key before distributing a release APK.

## Continuous integration

GitHub Actions runs unit tests and builds the debug APK on pushes and pull requests to `main`. It also supports manual runs. Download the `FerBrows-android-apks` artifact from a successful workflow run.

## Known gaps before a public release

- Download list with live progress, open and remove actions.
- Full site-permission controls and a clearer live network-status indicator.
- Device-level UI/instrumentation tests and a broader Android/device/network test matrix.
- A full architecture split of the browser orchestration currently in `MainActivity.kt`.
- Production release signing, store listing assets and a first-run onboarding flow.
- Tracker filter maintenance and a review of WebView/provider privacy behavior.

## Engine note

FerBrows uses Android System WebView, whose rendering engine is Chromium/Blink. It is not a separately bundled Chromium build; the installed WebView provider supplies engine updates.

## Feedback

Report bugs and feature requests at https://github.com/Toska-0/FerBrows/issues.
