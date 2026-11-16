# FerBrows Privacy Policy

Last updated: 15 November 2026

FerBrows is an early-stage Android browser. This policy describes the data handled by the application.

## Data stored on the device

FerBrows stores browser preferences, ordinary open-tab metadata, bookmarks and a limited browsing history locally in Android application storage. Website cookies, cache and site storage are handled by Android System WebView. The application does not provide account sign-in, cloud synchronization, or its own analytics service. If you choose to save a website login, its origin, username and password are stored locally in an AES-GCM encrypted vault. The encryption key is generated and held by Android Keystore and is not exported. FerBrows never uploads saved credentials. Password saving and autofill are disabled in private tabs. Autofill is limited to a matching website origin; saved credentials can be reviewed and deleted from the Saved passwords menu. On launch, FerBrows requests the public-IP country from ipapi.co to display a country flag and name; this exposes the device's current public IP address to that provider. The country lookup is not used to identify the user and is not needed for browsing. Android application backup is disabled for FerBrows.

Downloaded files are handed to Android DownloadManager and saved in the app-specific external Downloads directory. Android removes app-specific external files when FerBrows is uninstalled. Files may be opened from the in-app downloads list. Page screenshots are saved locally to the Pictures/FerBrows media folder on Android 10 and newer.

## Websites and search providers

When you open a website, the browser connects directly to that website using Android System WebView. On launch, the updater requests public release metadata from the GitHub API. If a newer stable release with an APK asset exists, FerBrows offers to download that APK from GitHub. Download progress is read from Android DownloadManager. Installation is handed to Android's package installer and requires the user's confirmation. Search queries are sent to the search provider selected in settings (Google, DuckDuckGo or Bing). Those services process requests under their own privacy policies. If you explicitly choose Translate page, FerBrows opens Google Translate with the current page URL; Google may receive that URL and process the page under its own privacy policy.

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

You can clear FerBrows' browsing history from settings. The settings screen also offers clearing WebView cache, cookies and website storage. Bookmarks can be imported from or exported to a file you select; the transfer is performed locally. Some device-level records and downloaded files are outside the browser's control.

## Contact

For privacy questions or bug reports, open an issue in the public project repository:
https://github.com/Toska-0/FerBrows/issues
