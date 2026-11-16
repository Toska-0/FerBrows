package studio.toska.ferbrows

import android.app.Application
import android.content.Context
import io.github.edsuns.adfilter.AdFilter
import io.github.edsuns.adfilter.Filter
import io.github.edsuns.adfilter.FilterViewModel
import java.util.concurrent.atomic.AtomicReference

/**
 * Initializes the open-source AdblockAndroid engine and its filter subscriptions.
 * Filter rules are downloaded and compiled by the library, not parsed by our host-only fallback.
 */
class FerBrowsApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AdBlockEngine.initialize(this)
    }
}

object AdBlockEngine {
    private data class Subscription(val name: String, val url: String, val minimumLevel: AdBlockLevel)

    private val subscriptions = listOf(
        Subscription("AdGuard Base", "https://filters.adtidy.org/extension/chromium/filters/2.txt", AdBlockLevel.LOW),
        Subscription("EasyList", "https://easylist.to/easylist/easylist.txt", AdBlockLevel.MEDIUM),
        Subscription("EasyPrivacy", "https://easylist.to/easylist/easyprivacy.txt", AdBlockLevel.MEDIUM),
        Subscription("AdGuard Annoyances", "https://filters.adtidy.org/extension/chromium/filters/14.txt", AdBlockLevel.MAXIMUM),
        Subscription("Fanboy Social", "https://easylist.to/easylist/fanboy-social.txt", AdBlockLevel.MAXIMUM)
    )

    private val level = AtomicReference(AdBlockLevel.MEDIUM)
    @Volatile private var viewModel: FilterViewModel? = null

    fun initialize(context: Context) {
        val filter = AdFilter.create(context.applicationContext)
        val prefs = context.getSharedPreferences("ferbrows_settings", Context.MODE_PRIVATE)
        level.set(runCatching {
            AdBlockLevel.valueOf(prefs.getString("ad_block_level", AdBlockLevel.MEDIUM.name) ?: AdBlockLevel.MEDIUM.name)
        }.getOrDefault(AdBlockLevel.MEDIUM))
        val model = filter.viewModel
        viewModel = model

        model.filters.observeForever { loaded ->
            if (loaded != null) syncSubscriptions(model, loaded.values)
        }
        if (!filter.hasInstallation) {
            subscriptions.forEach { subscription ->
                val added = model.addFilter(subscription.name, subscription.url)
                model.setFilterEnabled(added.id, isEnabled(subscription))
                model.download(added.id)
            }
        }
    }

    fun setLevel(newLevel: AdBlockLevel) {
        level.set(newLevel)
        val model = viewModel ?: return
        model.filters.value?.values?.forEach { filter ->
            val subscription = subscriptions.firstOrNull { it.url == filter.url } ?: return@forEach
            model.setFilterEnabled(filter.id, isEnabled(subscription))
        }
    }

    private fun syncSubscriptions(model: FilterViewModel, existing: Collection<Filter>) {
        subscriptions.forEach { subscription ->
            val filter = existing.firstOrNull { it.url == subscription.url }
            if (filter == null) {
                val added = model.addFilter(subscription.name, subscription.url)
                model.setFilterEnabled(added.id, isEnabled(subscription))
                model.download(added.id)
            } else {
                model.setFilterEnabled(filter.id, isEnabled(subscription), post = false)
                if (!filter.hasDownloaded() && !filter.downloadState.isRunning) {
                    model.download(filter.id)
                }
            }
        }
    }

    private fun isEnabled(subscription: Subscription): Boolean =
        level.get().ordinal >= subscription.minimumLevel.ordinal
}
