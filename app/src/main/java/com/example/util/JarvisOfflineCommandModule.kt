package com.example.util

import android.content.Context
import com.example.ui.JarvisViewModel
import java.util.Locale

object JarvisOfflineCommandModule {

    /**
     * Tries to intercept pre-defined offline command keywords instantly.
     * Returns a vocal description result if matched and executed successfully, or null if it must fallback.
     */
    fun processOfflineCommand(context: Context, text: String, viewModel: JarvisViewModel): String? {
        val cmd = text.lowercase(Locale.ROOT).trim()

        // 1. Tactical Optical Illumination
        if (cmd.contains("open flashlight") || cmd.contains("flashlight on") || cmd.contains("torch on") || cmd.contains("light on")) {
            return SystemCommandHelper.toggleFlashlight(context, true)
        }
        if (cmd.contains("close flashlight") || cmd.contains("turn off flashlight") || cmd.contains("flashlight off") || cmd.contains("torch off") || cmd.contains("light off")) {
            return SystemCommandHelper.toggleFlashlight(context, false)
        }

        // 2. Alert Grid Scheduling (Alarm Clock)
        if (cmd.contains("set alarm") || cmd.contains("schedule alarm")) {
            val numbers = "\\d+".toRegex().findAll(text).map { it.value.toInt() }.toList()
            val hr = numbers.getOrNull(0) ?: 8
            val min = numbers.getOrNull(1) ?: 0
            return SystemCommandHelper.setAlarm(context, hr, min, "J.A.R.V.I.S. Uplink Alert")
        }

        // 3. UI Tab Navigation (Seamless HUD coordination)
        if (cmd.contains("show reminders") || cmd.contains("open reminders") || cmd.contains("show tasks") || cmd.contains("view tasks") || cmd.contains("list tasks") || cmd.contains("open tasks")) {
            viewModel.selectTab("tasks")
            val count = viewModel.reminders.value.size
            return if (count > 0) {
                "Sir, displaying schedule nodes. There are $count priority tasks currently recorded on-grid."
            } else {
                "Sir, opening schedule grid. Your active task backlog is empty."
            }
        }

        if (cmd.contains("show chat") || cmd.contains("open chat")) {
            viewModel.selectTab("chat")
            return "Sir, establishing full conversational bandwidth console channel."
        }

        if (cmd.contains("show hud") || cmd.contains("open hud") || cmd.contains("open console") || cmd.contains("show console")) {
            viewModel.selectTab("hud")
            return "Sir, centering primary HUD telemetric instruments."
        }

        if (cmd.contains("show vision") || cmd.contains("open vision") || cmd.contains("open camera") || cmd.contains("show camera") || cmd.contains("optical scanner")) {
            viewModel.selectTab("vision")
            return "Sir, starting optical camera scanning subsystems."
        }

        if (cmd.contains("show settings") || cmd.contains("show performance") || cmd.contains("open settings") || cmd.contains("open configuration")) {
            viewModel.selectTab("settings")
            return "Sir, rendering system pipeline options and resource thresholds panels."
        }

        // 4. Hardware and Power Matrix reporting
        if (cmd.contains("battery") || cmd.contains("power level") || cmd.contains("battery status") || cmd.contains("reactor level")) {
            return SystemCommandHelper.getBatteryDiagnostics(context)
        }
        if (cmd.contains("storage") || cmd.contains("hardware storage") || cmd.contains("disk space")) {
            return SystemCommandHelper.getStorageDiagnostics()
        }
        if (cmd.contains("system diagnostics") || cmd.contains("system status") || cmd.contains("ram status") || cmd.contains("memory diagnostics")) {
            return SystemCommandHelper.getSystemMetrics(context)
        }

        return null
    }
}
