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
        "criteo.com", "adsrvr.org", "quantserve.com", "adnxs.com", "adform.net"
    )

    fun shouldBlock(host: String?): Boolean {
        val normalized = host?.trim()?.trimEnd('.')?.lowercase(Locale.ROOT) ?: return false
        if (normalized.isEmpty()) return false
        return blockedDomains.any { normalized == it || normalized.endsWith(".$it") }
    }
}
