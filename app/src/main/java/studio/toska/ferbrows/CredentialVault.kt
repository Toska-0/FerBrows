package studio.toska.ferbrows

import android.content.Context
import android.net.Uri
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

data class SavedCredential(
    val origin: String,
    val username: String,
    val password: String,
    val updatedAt: Long
)

class CredentialVault(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences("ferbrows_credential_vault", Context.MODE_PRIVATE)

    @Synchronized
    fun all(): List<SavedCredential> = readAll()

    @Synchronized
    fun forOrigin(origin: String): SavedCredential? =
        readAll().filter { it.origin == origin }.maxByOrNull { it.updatedAt }

    @Synchronized
    fun save(origin: String, username: String, password: String) {
        require(origin.startsWith("https://") || origin.startsWith("http://"))
        require(username.isNotBlank() && password.isNotBlank())
        val entries = readAll().filterNot { it.origin == origin && it.username == username }.toMutableList()
        entries += SavedCredential(origin, username, password, System.currentTimeMillis())
        writeAll(entries)
    }

    @Synchronized
    fun remove(origin: String, username: String) {
        writeAll(readAll().filterNot { it.origin == origin && it.username == username })
    }

    private fun readAll(): List<SavedCredential> {
        val encoded = prefs.getString("encrypted_entries", null) ?: return emptyList()
        return runCatching {
            val packed = Base64.decode(encoded, Base64.NO_WRAP)
            require(packed.size > 12)
            val iv = packed.copyOfRange(0, 12)
            val ciphertext = packed.copyOfRange(12, packed.size)
            val cipher = Cipher.getInstance("AES/GCM/NoPadding")
            cipher.init(Cipher.DECRYPT_MODE, getOrCreateKey(), GCMParameterSpec(128, iv))
            val json = JSONArray(String(cipher.doFinal(ciphertext), Charsets.UTF_8))
            (0 until json.length()).mapNotNull { index ->
                val item = json.optJSONObject(index) ?: return@mapNotNull null
                val origin = item.optString("origin")
                val username = item.optString("username")
                val password = item.optString("password")
                if (origin.isBlank() || username.isBlank() || password.isBlank()) null
                else SavedCredential(origin, username, password, item.optLong("updatedAt"))
            }
        }.getOrElse { emptyList() }
    }

    private fun writeAll(entries: List<SavedCredential>) {
        val json = JSONArray()
        entries.forEach { entry ->
            json.put(JSONObject().apply {
                put("origin", entry.origin)
                put("username", entry.username)
                put("password", entry.password)
                put("updatedAt", entry.updatedAt)
            })
        }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
        val encrypted = cipher.doFinal(json.toString().toByteArray(Charsets.UTF_8))
        val packed = cipher.iv + encrypted
        prefs.edit().putString("encrypted_entries", Base64.encodeToString(packed, Base64.NO_WRAP)).apply()
    }

    private fun getOrCreateKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

    companion object {
        private const val KEY_ALIAS = "ferbrows.password.vault.v1"

        fun originOf(rawUrl: String): String? = runCatching {
            val uri = Uri.parse(rawUrl)
            val scheme = uri.scheme?.lowercase()
            val host = uri.host?.lowercase()
            if (scheme !in setOf("http", "https") || host.isNullOrBlank()) null
            else buildString {
                append(scheme).append("://").append(host)
                if (uri.port != -1) append(':').append(uri.port)
            }
        }.getOrNull()
    }
}
