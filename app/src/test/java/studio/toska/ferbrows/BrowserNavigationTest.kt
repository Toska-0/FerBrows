package studio.toska.ferbrows

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class BrowserNavigationTest {
    @Test fun domainGetsHttpsScheme() {
        assertEquals("https://example.com", BrowserNavigation.resolveInput("example.com"))
    }

    @Test fun explicitHttpIsPreserved() {
        assertEquals("http://example.com/path", BrowserNavigation.resolveInput("http://example.com/path"))
    }

    @Test fun textBecomesSearchQuery() {
        assertEquals("https://www.google.com/search?q=hello+world", BrowserNavigation.resolveInput("hello world"))
    }

    @Test fun unsupportedSchemesAreRejected() {
        assertNull(BrowserNavigation.resolveInput("javascript:alert(1)"))
        assertNull(BrowserNavigation.resolveInput("file:///etc/passwd"))
    }

    @Test fun malformedHostsAreRejected() {
        assertNull(BrowserNavigation.resolveInput("https://"))
        assertNull(BrowserNavigation.resolveInput("https://bad host"))
    }
}
