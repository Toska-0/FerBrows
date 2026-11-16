package studio.toska.ferbrows

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale
import java.util.concurrent.atomic.AtomicBoolean

/** Lightweight ABP-compatible network and cosmetic filter-list engine for WebView. */
internal object AdBlockFilterEngine {
    private const val CACHE_FILE = "ferbrows-filter-lists.txt"
    private const val UPDATED_KEY = "last_updated"
    private const val UPDATE_INTERVAL_MS = 24L * 60L * 60L * 1000L
    private const val MAX_FILTER_BYTES = 8 * 1024 * 1024
    private val initialized = AtomicBoolean(false)
    @Volatile private var networkRules: List<NetworkRule> = emptyList()
    @Volatile private var cosmeticRules: List<CosmeticRule> = emptyList()
    @Volatile private var updating = false

    private val subscriptions = listOf(
        "https://easylist.to/easylist/easylist.txt",
        "https://easylist.to/easylist/easyprivacy.txt",
        "https://filters.adtidy.org/extension/chromium/filters/11.txt"
    )

    private data class NetworkRule(
        val hostSuffix: String?,
        val regex: Regex?,
        val includeTypes: Set<String>,
        val excludeTypes: Set<String>,
        val includeDomains: Set<String>,
        val excludeDomains: Set<String>,
        val thirdParty: Boolean?,
        val exception: Boolean
    )
    private data class CosmeticRule(val domains: Set<String>, val selector: String)

    fun initialize(context: Context) {
        if (!initialized.compareAndSet(false, true)) return
        val cached = runCatching { File(context.filesDir, CACHE_FILE).takeIf { it.isFile }?.readText() }.getOrNull()
        if (!cached.isNullOrBlank()) parseLists(cached)
    }

    suspend fun refreshIfNeeded(context: Context) = withContext(Dispatchers.IO) {
        initialize(context)
        if (updating) return@withContext
        val prefs = context.getSharedPreferences("ferbrows_filter_engine", Context.MODE_PRIVATE)
        if (System.currentTimeMillis() - prefs.getLong(UPDATED_KEY, 0L) < UPDATE_INTERVAL_MS && networkRules.isNotEmpty()) return@withContext
        synchronized(this@AdBlockFilterEngine) {
            if (updating) return@withContext
            updating = true
        }
        try {
            val downloaded = StringBuilder()
            for (subscription in subscriptions) {
                val part = download(subscription) ?: continue
                if (downloaded.length + part.length > MAX_FILTER_BYTES) break
                downloaded.append("\n").append(part)
            }
            if (downloaded.isNotEmpty()) {
                val text = downloaded.toString()
                runCatching {
                    File(context.filesDir, CACHE_FILE).writeText(text)
                    prefs.edit().putLong(UPDATED_KEY, System.currentTimeMillis()).apply()
                }
                parseLists(text)
            }
        } finally { updating = false }
    }

    private fun download(address: String): String? {
        val connection = (URL(address).openConnection() as HttpURLConnection).apply {
            connectTimeout = 12_000
            readTimeout = 18_000
            instanceFollowRedirects = true
            setRequestProperty("User-Agent", "FerBrows/1.1 (Android WebView; filter-list updater)")
            setRequestProperty("Accept", "text/plain,*/*;q=0.8")
        }
        return try {
            if (connection.responseCode !in 200..299) return null
            connection.inputStream.bufferedReader(Charsets.UTF_8).use { reader ->
                val output = StringBuilder()
                val buffer = CharArray(8192)
                var total = 0
                while (true) {
                    val count = reader.read(buffer)
                    if (count < 0) break
                    total += count
                    if (total > MAX_FILTER_BYTES) break
                    output.append(buffer, 0, count)
                }
                output.toString()
            }
        } catch (_: Exception) { null } finally { connection.disconnect() }
    }

    private fun parseLists(raw: String) {
        val networks = ArrayList<NetworkRule>(12_000)
        val cosmetics = ArrayList<CosmeticRule>(4_000)
        raw.lineSequence().forEach { original ->
            val line = original.trim()
            if (line.isEmpty() || line.startsWith("!") || line.startsWith("[") || line.length > 2_000) return@forEach
            val marker = when {
                "##" in line -> "##"
                "#@#" in line -> "#@#"
                else -> null
            }
            if (marker != null) {
                if (marker == "##") {
                    val split = line.split("##", limit = 2)
                    val selector = split.getOrNull(1)?.trim().orEmpty()
                    if (selector.isNotEmpty() && selector.length <= 500 && "{" !in selector && "}" !in selector) {
                        val domains = split[0].split(',').mapNotNull { it.trim().lowercase(Locale.ROOT).takeIf(String::isNotEmpty) }.toSet()
                        cosmetics.add(CosmeticRule(domains, selector))
                    }
                }
                return@forEach
            }
            if (line.startsWith("@@")) parseNetworkRule(line.removePrefix("@@"), true)?.let(networks::add)
            else if (!line.startsWith("/") && !line.startsWith("#")) parseNetworkRule(line, false)?.let(networks::add)
        }
        networkRules = networks
        cosmeticRules = cosmetics
    }

    private fun parseNetworkRule(raw: String, exception: Boolean): NetworkRule? {
        val parts = raw.split('$', limit = 2)
        val pattern = parts[0].trim()
        if (pattern.isEmpty() || pattern.startsWith("!") || pattern.startsWith("#")) return null
        val options = parts.getOrNull(1)?.split(',')?.map { it.trim().lowercase(Locale.ROOT) }.orEmpty()
        val types = setOf("script", "image", "stylesheet", "xmlhttprequest", "subdocument", "font", "media", "object", "other", "websocket", "ping")
        val includeTypes = options.filter { it in types }.toSet()
        val excludeTypes = options.filter { it.startsWith("~") && it.drop(1) in types }.map { it.drop(1) }.toSet()
        val domainValue = options.firstOrNull { it.startsWith("domain=") }?.removePrefix("domain=")
        val domainParts = domainValue?.split('|').orEmpty()
        val includeDomains = domainParts.filter { it.isNotBlank() && !it.startsWith("~") }.map { it.lowercase(Locale.ROOT) }.toSet()
        val excludeDomains = domainParts.filter { it.startsWith("~") }.map { it.removePrefix("~").lowercase(Locale.ROOT) }.toSet()
        val thirdParty = when { "third-party" in options -> true; "~third-party" in options -> false; else -> null }
        val candidateHost = if (pattern.startsWith("||")) {
            pattern.removePrefix("||").takeWhile { it != '^' && it != '/' && it != '*' && it != '|' }.lowercase(Locale.ROOT)
        } else ""
        val hostSuffix = candidateHost.takeIf { it.contains('.') && it.all { ch -> ch.isLetterOrDigit() || ch == '.' || ch == '-' } && (pattern == "||$it^" || pattern == "||$it") }
        val regex = if (hostSuffix == null) compilePattern(pattern) else null
        if (hostSuffix == null && regex == null) return null
        return NetworkRule(hostSuffix, regex, includeTypes, excludeTypes, includeDomains, excludeDomains, thirdParty, exception)
    }

    private fun compilePattern(pattern: String): Regex? {
        if (pattern.length > 350) return null
        return runCatching {
            val out = StringBuilder("^")
            pattern.forEachIndexed { index, ch ->
                when (ch) {
                    '*' -> out.append(".*")
                    '^' -> out.append("(?:[^A-Za-z0-9_.%-]|$)")
                    '|' -> if (index == 0) out.append("^") else if (index == pattern.lastIndex) out.append("$") else out.append("\\|")
                    else -> { if (ch in ".+?()[]{}\\\\") out.append('\\'); out.append(ch) }
                }
            }
            Regex(out.toString(), RegexOption.IGNORE_CASE)
        }.getOrNull()
    }

    private fun resourceType(accept: String?): String {
        val value = accept.orEmpty().lowercase(Locale.ROOT)
        return when {
            "image/" in value -> "image"
            "text/css" in value -> "stylesheet"
            "javascript" in value || "ecmascript" in value -> "script"
            "font/" in value || "application/font" in value -> "font"
            "video/" in value || "audio/" in value -> "media"
            "text/html" in value -> "subdocument"
            else -> "xmlhttprequest"
        }
    }

    fun shouldBlock(uri: Uri?, pageUrl: String?, accept: String?, level: AdBlockLevel): Boolean {
        if (uri == null || level == AdBlockLevel.LOW) return false
        val requestUrl = uri.toString()
        val host = uri.host?.lowercase(Locale.ROOT) ?: return false
        val pageHost = runCatching { Uri.parse(pageUrl).host?.lowercase(Locale.ROOT) }.getOrNull().orEmpty()
        val type = resourceType(accept)
        if (type == "image" && isLikelyAdImage(requestUrl)) return true
        val thirdParty = pageHost.isNotEmpty() && !isSameSite(host, pageHost)
        var exceptionMatched = false
        var blockMatched = false
        for (rule in networkRules) {
            if (rule.includeTypes.isNotEmpty() && type !in rule.includeTypes) continue
            if (type in rule.excludeTypes) continue
            if (rule.thirdParty != null && rule.thirdParty != thirdParty) continue
            if (rule.includeDomains.isNotEmpty() && rule.includeDomains.none { domainMatches(pageHost, it) }) continue
            if (rule.excludeDomains.any { domainMatches(pageHost, it) }) continue
            val matches = if (rule.hostSuffix != null) domainMatches(host, rule.hostSuffix)
                else rule.regex?.containsMatchIn(requestUrl) == true
            if (matches) {
                if (rule.exception) exceptionMatched = true else blockMatched = true
            }
        }
        if (exceptionMatched) return false
        if (blockMatched) return true
        return level == AdBlockLevel.MAXIMUM && TrackerProtection.shouldBlock(host)
    }

    fun cosmeticScript(pageUrl: String?, level: AdBlockLevel): String? {
        if (level == AdBlockLevel.LOW || pageUrl.isNullOrBlank()) return null
        val host = runCatching { Uri.parse(pageUrl).host?.lowercase(Locale.ROOT) }.getOrNull() ?: return null
        val selectors = (cosmeticRules.asSequence()
            .filter { it.domains.isEmpty() || it.domains.any { domain -> domainMatches(host, domain) } }
            .map { it.selector }
            .filterNot { selector -> listOf(":has-text(", ":matches-css(", ":xpath(", ":remove(", ":upward(", ":contains(").any(selector::contains) }
            .take(580).toList() +
            listOf("img[src*='/banners/']", "img[src*='advertising_ads_banner']", "img[src*='/ads/']", "iframe[src*='/ads/']"))
            .distinct()
        if (selectors.isEmpty()) return null
        val css = selectors.joinToString(",") { it.replace("</style", "<\\/style", ignoreCase = true) }
        val escaped = css.replace("\\", "\\\\").replace("'", "\\'").replace("\n", " ")
        return """(function(){try{var s='\${escaped}';var id='ferbrows-filter-css';var add=function(){var old=document.getElementById(id);if(old)return;var st=document.createElement('style');st.id=id;st.textContent=s+'{display:none!important;visibility:hidden!important;max-height:0!important;min-height:0!important;overflow:hidden!important}';(document.head||document.documentElement).appendChild(st);};add();if(!window.__ferBrowsCosmetic){window.__ferBrowsCosmetic=1;new MutationObserver(function(){add();}).observe(document.documentElement,{childList:true,subtree:true});}}catch(e){}})();"""
    }

    private fun isLikelyAdImage(url: String): Boolean {
        val path = runCatching { Uri.parse(url).encodedPath.orEmpty().lowercase(Locale.ROOT) }.getOrDefault("")
        return path.contains("/banners/") || path.contains("advertising_ads_banner") ||
            path.contains("/ads/") || path.contains("/ad/") || path.contains("/advert/")
    }

    private fun isSameSite(first: String, second: String): Boolean =
        first.split('.').takeLast(2).joinToString(".") == second.split('.').takeLast(2).joinToString(".")

    private fun domainMatches(host: String, domain: String): Boolean {
        val normalized = domain.removePrefix("~").trimStart('.').lowercase(Locale.ROOT)
        return normalized.isNotEmpty() && (host == normalized || host.endsWith(".$normalized"))
    }
}
