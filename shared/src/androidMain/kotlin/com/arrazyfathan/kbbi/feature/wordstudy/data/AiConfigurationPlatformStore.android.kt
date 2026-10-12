package com.arrazyfathan.kbbi.feature.wordstudy.data

import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import com.arrazyfathan.kbbi.feature.home.data.source.local.room.appContext
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

internal actual object AiConfigurationPlatformStore {
    actual fun credentialsAreSessionOnly() = false

    actual fun isSecretStoreAvailable() = true

    private val metadata
        get() =
            appContext.getSharedPreferences(
                "kbbi_ai_configuration",
                android.content.Context.MODE_PRIVATE,
            )
    private val secrets
        get() =
            appContext.getSharedPreferences(
                "kbbi_ai_credentials",
                android.content.Context.MODE_PRIVATE,
            )

    actual fun read(key: String): String? = metadata.getString(key, null)

    actual fun write(
        key: String,
        value: String?,
    ) {
        metadata.edit().apply { if (value == null) remove(key) else putString(key, value) }.apply()
    }

    actual fun readSecret(key: String): String? =
        runCatching {
            val encoded = secrets.getString(key, null) ?: return null
            val split = encoded.split(':', limit = 2)
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(
                Cipher.DECRYPT_MODE,
                keyStore().getKey(KEY_ALIAS, null) as SecretKey,
                GCMParameterSpec(128, Base64.decode(split[0], Base64.NO_WRAP)),
            )
            String(cipher.doFinal(Base64.decode(split[1], Base64.NO_WRAP)), Charsets.UTF_8)
        }.getOrNull()

    actual fun writeSecret(
        key: String,
        value: String?,
    ): Boolean =
        runCatching {
            if (value == null) {
                secrets.edit().remove(key).commit()
            } else {
                val cipher = Cipher.getInstance(TRANSFORMATION)
                cipher.init(Cipher.ENCRYPT_MODE, getOrCreateKey())
                val packed =
                    Base64.encodeToString(cipher.iv, Base64.NO_WRAP) + ":" +
                        Base64.encodeToString(
                            cipher.doFinal(value.toByteArray()),
                            Base64.NO_WRAP,
                        )
                secrets.edit().putString(key, packed).commit()
            }
        }.getOrDefault(false)

    private fun keyStore() = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }

    private fun getOrCreateKey(): SecretKey {
        val store = keyStore()
        (store.getKey(KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec
                .Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT,
                ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build(),
        )
        return generator.generateKey()
    }

    private const val TRANSFORMATION = "AES/GCM/NoPadding"
    private const val KEY_ALIAS = "kbbi_ai_configuration_key"
}
