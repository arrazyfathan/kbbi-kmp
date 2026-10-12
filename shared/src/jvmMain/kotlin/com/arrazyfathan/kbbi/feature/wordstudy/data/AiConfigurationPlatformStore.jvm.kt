package com.arrazyfathan.kbbi.feature.wordstudy.data

import java.util.prefs.Preferences

internal actual object AiConfigurationPlatformStore {
    actual fun credentialsAreSessionOnly() = false

    private val metadata =
        Preferences.userNodeForPackage(AiConfigurationPlatformStore::class.java).node("ai")

    actual fun isSecretStoreAvailable(): Boolean =
        when {
            isMac -> {
                true
            }

            isLinux -> {
                runCatching {
                    ProcessBuilder("sh", "-c", "command -v secret-tool").start().waitFor() == 0
                }.getOrDefault(false)
            }

            else -> {
                false
            }
        }

    actual fun read(key: String): String? = runCatching { metadata.get(key, null) }.getOrNull()

    actual fun write(
        key: String,
        value: String?,
    ) {
        runCatching {
            if (value == null) {
                metadata.remove(key)
            } else {
                metadata.put(
                    key,
                    value,
                )
            }
            metadata.flush()
        }
    }

    actual fun readSecret(key: String): String? =
        when {
            isMac -> {
                runCatching {
                    runCommand(
                        "security",
                        "find-generic-password",
                        "-a",
                        key,
                        "-s",
                        SERVICE,
                        "-w",
                    )
                }.getOrNull()
            }

            isLinux -> {
                runCatching {
                    runCommand(
                        "secret-tool",
                        "lookup",
                        "service",
                        SERVICE,
                        "account",
                        key,
                    )
                }.getOrNull()
            }

            else -> {
                null
            }
        }

    actual fun writeSecret(
        key: String,
        value: String?,
    ): Boolean =
        when {
            isMac && value == null -> {
                runCatching {
                    runCommand(
                        "security",
                        "delete-generic-password",
                        "-a",
                        key,
                        "-s",
                        SERVICE,
                    )
                    true
                }.getOrDefault(false)
            }

            isMac -> {
                runCatching {
                    runCommandWithInput(
                        value!!,
                        "security",
                        "add-generic-password",
                        "-U",
                        "-a",
                        key,
                        "-s",
                        SERVICE,
                        "-w",
                    )
                    true
                }.getOrDefault(false)
            }

            isLinux && value == null -> {
                runCatching {
                    runCommand(
                        "secret-tool",
                        "clear",
                        "service",
                        SERVICE,
                        "account",
                        key,
                    )
                    true
                }.getOrDefault(false)
            }

            isLinux -> {
                runCatching {
                    runCommandWithInput(
                        value!!,
                        "secret-tool",
                        "store",
                        "--label=KBBI AI credential",
                        "service",
                        SERVICE,
                        "account",
                        key,
                    )
                    true
                }.getOrDefault(false)
            }

            else -> {
                false
            }
        }

    private fun runCommand(vararg args: String): String {
        val process = ProcessBuilder(*args).redirectErrorStream(true).start()
        val output =
            process.inputStream
                .bufferedReader()
                .readText()
                .trim()
        check(process.waitFor() == 0) { "Credential store command failed" }
        return output
    }

    private fun runCommandWithInput(
        input: String,
        vararg args: String,
    ) {
        val process = ProcessBuilder(*args).redirectErrorStream(true).start()
        process.outputStream.bufferedWriter().use {
            it.write(input)
            it.newLine()
        }
        process.inputStream.bufferedReader().readText()
        check(process.waitFor() == 0) { "Credential store command failed" }
    }

    private val isMac get() = System.getProperty("os.name").startsWith("Mac", ignoreCase = true)
    private val isLinux get() = System.getProperty("os.name").startsWith("Linux", ignoreCase = true)
    private const val SERVICE = "com.arrazyfathan.kbbi.ai"
}
