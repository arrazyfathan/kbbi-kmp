package com.arrazyfathan.kbbi.feature.wordstudy.data

import web.storage.sessionStorage

internal actual object AiConfigurationPlatformStore {
    actual fun credentialsAreSessionOnly() = true

    actual fun isSecretStoreAvailable() = true

    private val transientSecrets = mutableMapOf<String, String>()

    private fun metadataKey(key: String) = "kbbi.ai.$key"

    actual fun read(key: String): String? = runCatching { sessionStorage.getItem(metadataKey(key)) }.getOrNull()

    actual fun write(
        key: String,
        value: String?,
    ) {
        runCatching {
            if (value == null) {
                sessionStorage.removeItem(metadataKey(key))
            } else {
                sessionStorage.setItem(
                    metadataKey(key),
                    value,
                )
            }
        }
    }

    actual fun readSecret(key: String): String? = transientSecrets[key]

    actual fun writeSecret(
        key: String,
        value: String?,
    ): Boolean {
        if (value == null) transientSecrets.remove(key) else transientSecrets[key] = value
        return true
    }
}
