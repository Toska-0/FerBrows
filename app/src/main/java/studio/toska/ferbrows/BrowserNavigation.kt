package studio.toska.ferbrows

import java.net.URLEncoder
import java.util.Locale
import java.net.URI

object BrowserNavigation {
    fun resolveInput(raw: String): String? {
        val input = raw.trim()
        if (input.isEmpty()) return null
        if (input.any { it == '\n' || it == '\r' }) return null

        val explicit = Regex("^[a-zA-Z][a-zA-Z0-9+.-]*:")
        if (explicit.containsMatchIn(input)) {
            val scheme = input.substringBefore(':').lowercase(Locale.ROOT)
            if (scheme != "http" && scheme != "https") return null
            return normalizeWebUrl(input)
        }

        val looksLikeHost = input.contains('.') && !input.contains(' ') && !input.startsWith('.') && !input.endsWith('.')
        return if (looksLikeHost) normalizeWebUrl("https://$input")
        else "https://www.google.com/search?q=" + URLEncoder.encode(input, "UTF-8")
    }

    private fun normalizeWebUrl(value: String): String? = runCatching {
        val uri = URI(value.trim())
        val scheme = uri.scheme?.lowercase(Locale.ROOT)
        val host = uri.host
        if ((scheme != "http" && scheme != "https") || host.isNullOrBlank()) null
        else URI(scheme, uri.userInfo, host, uri.port, uri.rawPath, uri.rawQuery, uri.rawFragment).toASCIIString()
    }.getOrNull()
}
