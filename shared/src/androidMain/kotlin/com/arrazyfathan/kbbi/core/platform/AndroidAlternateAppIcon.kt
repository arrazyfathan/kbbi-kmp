package com.arrazyfathan.kbbi.core.platform

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build

internal class AndroidAlternateAppIcon(
    context: Context,
) : AlternateAppIcon {
    private val packageManager = context.packageManager
    private val packageName = context.packageName

    override val target: AppIconTarget = AppIconTarget.LAUNCHER

    override suspend fun currentIcon(): CapabilityResult<AppIconVariant> =
        runCatching {
            val states =
                AppIconVariant.entries.associateWith {
                    packageManager.getComponentEnabledSetting(component(it))
                }
            val active =
                AppIconVariant.entries.filter { variant ->
                    val state = states.getValue(variant)
                    state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                        (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && variant == AppIconVariant.DEFAULT)
                }
            active.singleOrNull() ?: run {
                apply(AppIconVariant.DEFAULT)
                AppIconVariant.DEFAULT
            }
        }.fold(
            onSuccess = { CapabilityResult.Success(it) },
            onFailure = { CapabilityResult.Failed(FailureReason.PLATFORM_ERROR) },
        )

    override suspend fun setIcon(variant: AppIconVariant): CapabilityResult<AppIconVariant> {
        var previous: Map<AppIconVariant, Int> = emptyMap()
        val result =
            runCatching {
                previous =
                    AppIconVariant.entries.associateWith {
                        packageManager.getComponentEnabledSetting(component(it))
                    }
                apply(variant)
                check(currentEnabled().singleOrNull() == variant) { "Launcher icon did not apply" }
                variant
            }
        return result.fold(
            onSuccess = { CapabilityResult.Success(it) },
            onFailure = {
                if (previous.isNotEmpty()) runCatching { restore(previous) }
                CapabilityResult.Failed(FailureReason.PLATFORM_ERROR)
            },
        )
    }

    private fun apply(variant: AppIconVariant) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.setComponentEnabledSettings(
                AppIconVariant.entries.map { candidate ->
                    PackageManager.ComponentEnabledSetting(
                        component(candidate),
                        if (candidate ==
                            variant
                        ) {
                            PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                        } else {
                            PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                        },
                        PackageManager.DONT_KILL_APP,
                    )
                },
            )
        } else {
            packageManager.setComponentEnabledSetting(
                component(variant),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                PackageManager.DONT_KILL_APP,
            )
            AppIconVariant.entries.filterNot { it == variant }.forEach {
                packageManager.setComponentEnabledSetting(
                    component(it),
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
                    PackageManager.DONT_KILL_APP,
                )
            }
        }
    }

    private fun currentEnabled(): List<AppIconVariant> =
        AppIconVariant.entries.filter { variant ->
            val state = packageManager.getComponentEnabledSetting(component(variant))
            state == PackageManager.COMPONENT_ENABLED_STATE_ENABLED ||
                (state == PackageManager.COMPONENT_ENABLED_STATE_DEFAULT && variant == AppIconVariant.DEFAULT)
        }

    private fun restore(states: Map<AppIconVariant, Int>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.setComponentEnabledSettings(
                states.map { (variant, state) ->
                    PackageManager.ComponentEnabledSetting(
                        component(variant),
                        state,
                        PackageManager.DONT_KILL_APP,
                    )
                },
            )
        } else {
            AppIconVariant.entries.forEach { variant ->
                if (states.getValue(variant) == PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    packageManager.setComponentEnabledSetting(
                        component(variant),
                        PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                        PackageManager.DONT_KILL_APP,
                    )
                }
            }
            AppIconVariant.entries.forEach { variant ->
                if (states.getValue(variant) != PackageManager.COMPONENT_ENABLED_STATE_ENABLED) {
                    packageManager.setComponentEnabledSetting(
                        component(variant),
                        states.getValue(variant),
                        PackageManager.DONT_KILL_APP,
                    )
                }
            }
        }
    }

    private fun component(variant: AppIconVariant): ComponentName = ComponentName(packageName, "$packageName.launcher.${variant.aliasName}")

    private val AppIconVariant.aliasName: String
        get() =
            when (this) {
                AppIconVariant.DEFAULT -> "DefaultIconAlias"
                AppIconVariant.ROYAL_OCEAN -> "RoyalOceanIconAlias"
                AppIconVariant.GOLDEN_SUNSET -> "GoldenSunsetIconAlias"
                AppIconVariant.GOLDEN_CORAL_ENERGY -> "GoldenCoralEnergyIconAlias"
                AppIconVariant.DEEP_FOREST_ENERGY -> "DeepForestEnergyIconAlias"
                AppIconVariant.NEON_VIOLET -> "NeonVioletIconAlias"
                AppIconVariant.BLAZE_ORANGE -> "BlazeOrangeIconAlias"
            }
}
