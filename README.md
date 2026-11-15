# FerBrows

FerBrows is an Android browser prototype with a dark, lime-accented interface inspired by the Sub Market visual style.

## Current features

- Chromium-based page rendering through Android System WebView
- Address bar with Google search and direct URL navigation
- Back, forward, reload and home controls
- Basic multi-tab interface
- Quick links and an in-app bookmarks list
- Dark interface with lime accents and animated Compose elements

## Build

Requirements: JDK 17, Gradle 8.9 and Android SDK 35.

Run:

    gradle assembleDebug

The APK is created at:

    app/build/outputs/apk/debug/app-debug.apk

## GitHub Actions

The workflow at .github/workflows/build.yml runs on pushes and pull requests to main, and can also be started manually from the Actions tab. Download the FerBrows-debug-apk artifact from a successful run.

## Engine note

This first Android version uses Android System WebView, whose rendering engine is Chromium/Blink. It is not a separately bundled or independently updated Chromium build. A custom Chromium-for-Android build would require a much larger native build and maintenance pipeline.

## Status

Early development build: 0.1.0. Bookmarks and tab metadata are currently kept in memory and reset when the app process is restarted. Private browsing, download management, persistent history, sync, extension support and advanced tracking protection are not implemented yet.
