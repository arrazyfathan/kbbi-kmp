package com.arrazyfathan.kbbi.core.presentation.ui

import androidx.compose.runtime.Composable
import org.jetbrains.compose.resources.stringResource

sealed interface UiText {
    data class DynamicString(
        val value: String,
    ) : UiText

    data class StringResource(
        val resource: org.jetbrains.compose.resources.StringResource,
        val args: Array<Any> = emptyArray(),
    ) : UiText {
        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other == null || this::class != other::class) return false

            other as StringResource

            if (resource != other.resource) return false
            if (!args.contentEquals(other.args)) return false

            return true
        }

        override fun hashCode(): Int {
            var result = resource.hashCode()
            result = 31 * result + args.contentHashCode()
            return result
        }
    }

    @Composable
    fun asString(): String =
        when (this) {
            is DynamicString -> value
            is StringResource -> stringResource(resource = resource, formatArgs = args)
        }
}

suspend fun UiText.asStringNonComposable(): String =
    when (this) {
        is UiText.DynamicString -> value
        is UiText.StringResource -> org.jetbrains.compose.resources.getString(resource, *args)
    }
