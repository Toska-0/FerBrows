# FerBrows Privacy Policy

Last updated: 15 November 2026

FerBrows is an early-stage Android browser. This policy describes the data handled by the application.

## Data stored on the device

FerBrows stores browser preferences, ordinary open-tab metadata, bookmarks and a limited browsing history locally in Android application storage. Website cookies, cache and site storage are handled by Android System WebView. The application does not provide account sign-in, cloud synchronization, or its own analytics service. Android application backup is disabled for FerBrows.

Downloaded files are handed to Android DownloadManager and saved in the app-specific external Downloads directory. Android removes app-specific external files when FerBrows is uninstalled. Files may be opened from the in-app downloads list.

## Websites and search providers

When you open a website, the browser connects directly to that website using Android System WebView. Search queries are sent to the search provider selected in settings (Google, DuckDuckGo or Bing). Those services process requests under their own privacy policies.

Safe Browsing is enabled where supported by the installed WebView provider. The provider may process site reputation checks under its own terms and privacy policy.

## Private tabs

Private tabs use a separate AndroidX WebKit profile when the installed WebView supports multi-profile. They are excluded from FerBrows' saved tab list, bookmarks and browsing history. The private profile is deleted when the last private tab is closed or when FerBrows starts after an unclean termination.

Private mode is not a guarantee of anonymity. Websites, network operators, DNS providers, Android, the WebView provider and downloaded files may still expose or retain information. Downloads initiated in a private tab are still handled by Android DownloadManager and may remain on the device.

If multi-profile is unavailable, FerBrows refuses to create a private tab rather than silently using the ordinary profile.

## Tracker protection and connection security

The optional tracker protection feature blocks requests to a small built-in list of known advertising and analytics hostnames. It is not a comprehensive filter, is not automatically updated, and may miss trackers or affect some website features.

HTTP pages are allowed and are marked as not secure in the address bar; HTTP traffic is not encrypted. HTTPS encrypts traffic between the browser and the website, but does not prove that a website is trustworthy. FerBrows does not provide a VPN, proxy, or anonymity service.

Camera, microphone and geolocation permission requests from websites are denied by default in this version. Per-site grant/revoke controls are not yet available.

## Controls

You can clear FerBrows' browsing history from settings. The settings screen also offers clearing WebView cache, cookies and website storage. Some device-level records and downloaded files are outside the browser's control.

## Contact

For privacy questions or bug reports, open an issue in the public project repository:
https://github.com/Toska-0/FerBrows/issues
