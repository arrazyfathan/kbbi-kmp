package com.arrazyfathan.kbbi.feature.appupdate.data

import com.arrazyfathan.kbbi.feature.appupdate.domain.AppUpdate
import com.arrazyfathan.kbbi.feature.settings.data.readSettingsPreference
import com.arrazyfathan.kbbi.feature.settings.data.writeSettingsPreference
import kotlinx.serialization.SerializationException
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

internal interface AppUpdateStore {
    fun shouldRunAutomaticCheck(nowMillis: Long): Boolean

    fun markAutomaticCheckFinished(nowMillis: Long, successful: Boolean)

    fun readRequiredUpdate(): AppUpdate?

    fun writeRequiredUpdate(update: AppUpdate)

    fun clearRequiredUpdate()
}

internal class PlatformAppUpdateStore(
    private val json: Json,
) : AppUpdateStore {
    override fun shouldRunAutomaticCheck(nowMillis: Long): Boolean {
        val lastAttempt = readSettingsPreference(KEY_LAST_ATTEMPT)?.toLongOrNull() ?: 0L
        val lastSuccess = readSettingsPreference(KEY_LAST_SUCCESS)?.toLongOrNull() ?: 0L
        if (lastAttempt == 0L) return true
        val interval = if (lastSuccess >= lastAttempt) DAY_MILLIS else HOUR_MILLIS
        return nowMillis - lastAttempt >= interval
    }

    override fun markAutomaticCheckFinished(nowMillis: Long, successful: Boolean) {
        writeSettingsPreference(KEY_LAST_ATTEMPT, nowMillis.toString())
        if (successful) writeSettingsPreference(KEY_LAST_SUCCESS, nowMillis.toString())
    }

    override fun readRequiredUpdate(): AppUpdate? {
        val value = readSettingsPreference(KEY_REQUIRED) ?: return null
        return try {
            json.decodeFromString<AppUpdate>(value)
        } catch (_: SerializationException) {
            clearRequiredUpdate()
            null
        } catch (_: IllegalArgumentException) {
            clearRequiredUpdate()
            null
        }
    }

    override fun writeRequiredUpdate(update: AppUpdate) {
        writeSettingsPreference(KEY_REQUIRED, json.encodeToString(update))
    }

    override fun clearRequiredUpdate() {
        writeSettingsPreference(KEY_REQUIRED, "")
    }

    private companion object {
        const val KEY_LAST_ATTEMPT = "app_update_last_attempt"
        const val KEY_LAST_SUCCESS = "app_update_last_success"
        const val KEY_REQUIRED = "app_update_cached_required"
        const val DAY_MILLIS = 24L * 60 * 60 * 1_000
        const val HOUR_MILLIS = 60L * 60 * 1_000
    }
}
