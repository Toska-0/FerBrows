package studio.toska.ferbrows

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TrackerProtectionTest {
    @Test fun blocksKnownTrackerHostsAndSubdomains() {
        assertTrue(TrackerProtection.shouldBlock("www.google-analytics.com"))
        assertTrue(TrackerProtection.shouldBlock("pixel.facebook.com"))
    }

    @Test fun doesNotBlockOrdinaryOrLookalikeHosts() {
        assertFalse(TrackerProtection.shouldBlock("example.com"))
        assertFalse(TrackerProtection.shouldBlock("notgoogle-analytics.com"))
        assertFalse(TrackerProtection.shouldBlock(null))
    }
}
