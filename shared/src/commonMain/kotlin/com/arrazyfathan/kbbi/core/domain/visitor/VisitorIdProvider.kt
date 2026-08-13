package com.arrazyfathan.kbbi.core.domain.visitor

interface VisitorIdProvider {
    fun getVisitorId(): String
}

interface VisitorIdStorage {
    fun getVisitorId(): String?

    fun saveVisitorId(visitorId: String)
}

class StoredVisitorIdProvider(
    private val storage: VisitorIdStorage,
) : VisitorIdProvider {
    private var cachedVisitorId: String? = null

    override fun getVisitorId(): String {
        cachedVisitorId?.let { return it }

        val visitorId = storage.getVisitorId()?.takeIf { it.isNotBlank() } ?: createVisitorId().also(storage::saveVisitorId)
        cachedVisitorId = visitorId
        return visitorId
    }
}

private fun createVisitorId(): String {
    val bytes = ByteArray(UUID_BYTE_COUNT)
    for (index in bytes.indices) {
        bytes[index] = kotlin.random.Random.nextInt(0, 256).toByte()
    }

    bytes[6] = ((bytes[6].toInt() and 0x0f) or 0x40).toByte()
    bytes[8] = ((bytes[8].toInt() and 0x3f) or 0x80).toByte()

    return buildString(UUID_STRING_LENGTH) {
        bytes.forEachIndexed { index, byte ->
            val value = byte.toInt() and 0xff
            append(value.toString(16).padStart(2, '0'))
            if (index in UUID_DASH_POSITIONS) append('-')
        }
    }
}

private const val UUID_BYTE_COUNT = 16
private const val UUID_STRING_LENGTH = 36
private val UUID_DASH_POSITIONS = setOf(3, 5, 7, 9)
