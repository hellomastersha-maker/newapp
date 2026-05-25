package com.example.util

import android.app.ActivityManager
import android.content.Context
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class ThemingMode {
    AUTO,        // Automatically detect based on RAM / CPU
    FORCE_FULL,  // Override to Full Visual Mode
    FORCE_LITE   // Override to Lite Mode
}

object JarvisThemeEngine {
    private const val PREFS_NAME = "jarvis_theme_engine_prefs"
    private const val KEY_MODE = "theming_override_mode"

    private val _currentOverrideMode = MutableStateFlow(ThemingMode.AUTO)
    val currentOverrideMode: StateFlow<ThemingMode> = _currentOverrideMode.asStateFlow()

    private val _isLiteActive = MutableStateFlow(false)
    val isLiteActive: StateFlow<Boolean> = _isLiteActive.asStateFlow()

    // Decoded hardware diagnostics
    var totalRamGb: Double = 2.0
        private set
    var processorCores: Int = 4
        private set
    var performanceScore: Int = 50
        private set
    var isDeviceLowSpec: Boolean = true
        private set

    fun init(context: Context) {
        try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
            val memInfo = ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            totalRamGb = memInfo.totalMem / (1024.0 * 1024.0 * 1024.0)
            processorCores = Runtime.getRuntime().availableProcessors()
            
            // Dynamic Scoring: 12pts per GB RAM + 8pts per processor core
            performanceScore = (totalRamGb * 12 + processorCores * 8).toInt()
            
            // Devices under 4GB or with < 55 resource score default to Lite mode to keep animations perfectly smooth
            isDeviceLowSpec = totalRamGb < 3.8 || performanceScore < 55
        } catch (e: Exception) {
            Log.e("JarvisThemeEngine", "Unable to read processor registries: ${e.message}")
            isDeviceLowSpec = true
        }

        // Retrieve cached override preference
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val savedModeStr = prefs.getString(KEY_MODE, ThemingMode.AUTO.name) ?: ThemingMode.AUTO.name
        val mode = try { ThemingMode.valueOf(savedModeStr) } catch (e: Exception) { ThemingMode.AUTO }
        _currentOverrideMode.value = mode

        updateActiveState()
    }

    private fun updateActiveState() {
        _isLiteActive.value = when (_currentOverrideMode.value) {
            ThemingMode.AUTO -> isDeviceLowSpec
            ThemingMode.FORCE_FULL -> false
            ThemingMode.FORCE_LITE -> true
        }
    }

    fun setThemingMode(context: Context, mode: ThemingMode) {
        _currentOverrideMode.value = mode
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        updateActiveState()
    }
}
