package com.arrazyfathan.kbbi.feature.wordstudy.data

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.UByteVar
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.allocArray
import kotlinx.cinterop.get
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.reinterpret
import kotlinx.cinterop.usePinned
import platform.CoreFoundation.CFDataCreate
import platform.CoreFoundation.CFDataGetBytePtr
import platform.CoreFoundation.CFDataGetLength
import platform.CoreFoundation.CFDictionaryAddValue
import platform.CoreFoundation.CFDictionaryCreateMutable
import platform.CoreFoundation.CFDictionaryRef
import platform.CoreFoundation.CFRelease
import platform.CoreFoundation.CFStringCreateWithCString
import platform.CoreFoundation.CFTypeRef
import platform.CoreFoundation.CFTypeRefVar
import platform.CoreFoundation.kCFAllocatorDefault
import platform.CoreFoundation.kCFBooleanTrue
import platform.CoreFoundation.kCFStringEncodingUTF8
import platform.Foundation.NSUserDefaults
import platform.Security.SecItemAdd
import platform.Security.SecItemCopyMatching
import platform.Security.SecItemDelete
import platform.Security.errSecItemNotFound
import platform.Security.errSecSuccess
import platform.Security.kSecAttrAccessible
import platform.Security.kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly
import platform.Security.kSecAttrAccount
import platform.Security.kSecAttrService
import platform.Security.kSecClass
import platform.Security.kSecClassGenericPassword
import platform.Security.kSecMatchLimit
import platform.Security.kSecMatchLimitOne
import platform.Security.kSecReturnData
import platform.Security.kSecValueData

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
internal actual object AiConfigurationPlatformStore {
    actual fun isSecretStoreAvailable() = true

    private fun metadataKey(key: String) = "kbbi.ai.$key"

    actual fun read(key: String): String? = NSUserDefaults.standardUserDefaults.stringForKey(metadataKey(key))

    actual fun write(
        key: String,
        value: String?,
    ) {
        if (value == null) {
            NSUserDefaults.standardUserDefaults.removeObjectForKey(metadataKey(key))
        } else {
            NSUserDefaults.standardUserDefaults.setObject(value, forKey = metadataKey(key))
        }
    }

    actual fun readSecret(key: String): String? {
        val search = query(key, includeData = true)
        return try {
            memScoped {
                val result = allocArray<CFTypeRefVar>(1)
                if (SecItemCopyMatching(search, result) != errSecSuccess) return@memScoped null
                val value = result[0] ?: return@memScoped null
                val pointer = CFDataGetBytePtr(value.reinterpret()) ?: return@memScoped null
                ByteArray(CFDataGetLength(value.reinterpret()).toInt()) { pointer[it].toByte() }
                    .decodeToString()
                    .also { CFRelease(value) }
            }
        } finally {
            CFRelease(search as CFTypeRef)
        }
    }

    actual fun writeSecret(
        key: String,
        value: String?,
    ): Boolean {
        val query = query(key)
        try {
            if (value == null) {
                val status = SecItemDelete(query)
                return status == errSecSuccess || status == errSecItemNotFound
            }
            SecItemDelete(query)
            val data = value.encodeToByteArray().toCFData()
            CFDictionaryAddValue(query, kSecValueData, data)
            CFDictionaryAddValue(
                query,
                kSecAttrAccessible,
                kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly,
            )
            val status = SecItemAdd(query, null)
            CFRelease(data)
            return status == errSecSuccess
        } finally {
            CFRelease(query as CFTypeRef)
        }
    }

    private fun query(
        key: String,
        includeData: Boolean = false,
    ): CFDictionaryRef =
        requireNotNull(
            CFDictionaryCreateMutable(
                kCFAllocatorDefault,
                7,
                null,
                null,
            ),
        ).also { dictionary ->
            CFDictionaryAddValue(dictionary, kSecClass, kSecClassGenericPassword)
            addString(dictionary, kSecAttrService as CFTypeRef, "com.arrazyfathan.kbbi.ai")
            addString(dictionary, kSecAttrAccount as CFTypeRef, key)
            if (includeData) {
                CFDictionaryAddValue(dictionary, kSecReturnData, kCFBooleanTrue)
                CFDictionaryAddValue(dictionary, kSecMatchLimit, kSecMatchLimitOne)
            }
        }

    private fun addString(
        dictionary: CFDictionaryRef,
        key: CFTypeRef,
        value: String,
    ) {
        val string =
            requireNotNull(
                CFStringCreateWithCString(
                    kCFAllocatorDefault,
                    value,
                    kCFStringEncodingUTF8,
                ),
            )
        CFDictionaryAddValue(dictionary, key, string as CFTypeRef)
        CFRelease(string)
    }

    private fun ByteArray.toCFData() =
        usePinned {
            requireNotNull(
                CFDataCreate(
                    kCFAllocatorDefault,
                    it.addressOf(0).reinterpret<UByteVar>(),
                    size.toLong(),
                ),
            )
        }
}
