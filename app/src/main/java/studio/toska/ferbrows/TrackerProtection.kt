package studio.toska.ferbrows

import java.util.Locale

object TrackerProtection {
    private val blockedDomains = setOf(
        "google-analytics.com", "googletagmanager.com", "doubleclick.net",
        "googlesyndication.com", "adservice.google.com", "pagead2.googlesyndication.com",
        "connect.facebook.net", "pixel.facebook.com", "analytics.twitter.com",
        "static.ads-twitter.com", "scorecardresearch.com", "hotjar.com",
        "segment.io", "api.segment.io", "mixpanel.com", "amplitude.com",
        "clarity.ms", "bat.bing.com", "taboola.com", "outbrain.com",
        "criteo.com", "adsrvr.org", "quantserve.com", "adnxs.com", "adform.net",\n        "sentry.io", "browser.sentry-cdn.com", "ingest.sentry.io", "o0.ingest.sentry.io",\n        "bugsnag.com", "notify.bugsnag.com", "sessions.bugsnag.com", "api.bugsnag.com",\n        "app.bugsnag.com", "fullstory.com", "mouseflow.com", "inspectlet.com"
    )

    fun shouldBlock(host: String?): Boolean {
        val normalized = host?.trim()?.trimEnd('.')?.lowercase(Locale.ROOT) ?: return false
        if (normalized.isEmpty()) return false
        return blockedDomains.any { normalized == it || normalized.endsWith(".$it") }
    }
}


/**
 * Lightweight host-based ad filtering. This is not a full EasyList/uBlock filter engine:
 * it blocks known ad-network request hosts and intentionally avoids cosmetic page changes.
 */
object AdBlocker {
    private val lowDomains = setOf(
        "doubleclick.net", "googlesyndication.com", "adservice.google.com",
        "pagead2.googlesyndication.com", "adnxs.com", "adform.net",
        "criteo.com", "taboola.com", "outbrain.com", "ads.yahoo.com"
    )
    private val mediumDomains = lowDomains + setOf(
        "googletagservices.com", "amazon-adsystem.com", "pubmatic.com",
        "rubiconproject.com", "openx.net", "casalemedia.com", "smartadserver.com",
        "serving-sys.com", "adsafeprotected.com", "moatads.com", "3lift.com",
        "sharethrough.com", "bidswitch.net", "contextweb.com", "advertising.com",
        "yieldmo.com", "inmobi.com", "applovin.com", "unity3d.com",
        "lijit.com", "media.net", "undertone.com"
    )
    private val maximumDomains = mediumDomains + setOf(
        "google-analytics.com", "googletagmanager.com", "connect.facebook.net",
        "pixel.facebook.com", "analytics.twitter.com", "static.ads-twitter.com",
        "scorecardresearch.com", "hotjar.com", "segment.io", "mixpanel.com",
        "amplitude.com", "clarity.ms", "bat.bing.com", "quantserve.com",
        "newrelic.com", "nr-data.net", "fullstory.com", "mouseflow.com",
        "inspectlet.com", "heap.io", "optimizely.com"
    )

    fun shouldBlock(host: String?, level: AdBlockLevel): Boolean {
        if (level == AdBlockLevel.LOW && host.isNullOrBlank()) return false
        val normalized = host?.trim()?.trimEnd('.')?.lowercase(Locale.ROOT) ?: return false
        if (normalized.isEmpty()) return false
        val domains = when (level) {
            AdBlockLevel.LOW -> lowDomains
            AdBlockLevel.MEDIUM -> mediumDomains
            AdBlockLevel.MAXIMUM -> maximumDomains
        }
        return domains.any { normalized == it || normalized.endsWith(".$it") }
    }
}
