package studio.toska.ferbrows

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackerProtectionTest {
    @Test fun blocksKnownTrackerHostsAndSubdomains() {
        assertTrue(TrackerProtection.shouldBlock("www.google-analytics.com"))
        assertTrue(TrackerProtection.shouldBlock("pixel.facebook.com"))
        assertTrue(TrackerProtection.shouldBlock("browser.sentry-cdn.com"))
        assertTrue(TrackerProtection.shouldBlock("o0.ingest.sentry.io"))
        assertTrue(TrackerProtection.shouldBlock("notify.bugsnag.com"))
    }

    @Test fun adBlockingStrengthsFilterProgressivelyMoreHosts() {
        assertTrue(AdBlocker.shouldBlock("ads.doubleclick.net", AdBlockLevel.LOW))
        assertTrue(AdBlocker.shouldBlock("ads.pubmatic.com", AdBlockLevel.MEDIUM))
        assertTrue(AdBlocker.shouldBlock("browser.sentry-cdn.com", AdBlockLevel.MEDIUM))
        assertTrue(AdBlocker.shouldBlock("notify.bugsnag.com", AdBlockLevel.MEDIUM))
        assertFalse(AdBlocker.shouldBlock("ads.pubmatic.com", AdBlockLevel.LOW))
        assertTrue(AdBlocker.shouldBlock("www.google-analytics.com", AdBlockLevel.MAXIMUM))
        assertFalse(AdBlocker.shouldBlock("www.google-analytics.com", AdBlockLevel.MEDIUM))
    }

    @Test fun doesNotBlockOrdinaryOrLookalikeHosts() {
        assertFalse(TrackerProtection.shouldBlock("example.com"))
        assertFalse(TrackerProtection.shouldBlock("notgoogle-analytics.com"))
        assertFalse(TrackerProtection.shouldBlock(null))
    }
}
