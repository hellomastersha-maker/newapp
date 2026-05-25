package com.example.util

import android.app.AlarmManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.AlarmClock
import android.provider.Settings
import android.widget.Toast
import java.io.File
import java.util.Locale

object SystemCommandHelper {

    // Toggle Flashlight/Torch (API 23+)
    fun toggleFlashlight(context: Context, enable: Boolean): String {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val cameraId = cameraManager.cameraIdList.getOrNull(0)
            if (cameraId != null) {
                cameraManager.setTorchMode(cameraId, enable)
                if (enable) "Sir, primary illumination beam is fully activated."
                else "Sir, primary illumination grid has been deactivated."
            } else {
                "System failure: Optical illumination array not found."
            }
        } catch (e: Exception) {
            "Sir, unable to adjust flashlight. Permission or hardware state failure: ${e.localizedMessage}"
        }
    }

    // Dial a phone number
    fun dialNumber(context: Context, number: String): String {
        return try {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:$number")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Sir, opening communication frequencies to dial $number."
        } catch (e: Exception) {
            "Sir, communication relay could not be established."
        }
    }

    // Set alarm clock
    fun setAlarm(context: Context, hour: Int, minutes: Int, message: String): String {
        return try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour)
                putExtra(AlarmClock.EXTRA_MINUTES, minutes)
                putExtra(AlarmClock.EXTRA_MESSAGE, message.ifEmpty() { "J.A.R.V.I.S. Uplink Warning" })
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Sir, a priority alert has been anchored for exactly %02d:%02d.".format(hour, minutes)
        } catch (e: Exception) {
            "Sir, alarm grid synchronization failed."
        }
    }

    // Search or navigate on maps
    fun launchMaps(context: Context, query: String): String {
        return try {
            val encodedQuery = Uri.encode(query)
            val uri = Uri.parse("geo:0,0?q=$encodedQuery")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Sir, navigational arrays aligned to find: $query."
        } catch (e: Exception) {
            "Sir, navigational link disrupted."
        }
    }

    // WhatsApp Message Launcher
    fun sendWhatsApp(context: Context, query: String): String {
        return try {
            val intent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://api.whatsapp.com/send?text=" + Uri.encode(query))
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
            "Sir, compiling WhatsApp data uplink for transit: \"$query\""
        } catch (e: Exception) {
            "Sir, WhatsApp uplink rejected."
        }
    }

    // Open System Settings (Brightness or WiFi)
    fun openSettings(context: Context, type: String): String {
        return try {
            val action = when (type.lowercase(Locale.ROOT)) {
                "wifi" -> Settings.ACTION_WIFI_SETTINGS
                "bluetooth" -> Settings.ACTION_BLUETOOTH_SETTINGS
                "display", "brightness" -> Settings.ACTION_DISPLAY_SETTINGS
                else -> Settings.ACTION_SETTINGS
            }
            val intent = Intent(action).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK }
            context.startActivity(intent)
            "Sir, redirecting your console to $type configuration."
        } catch (e: Exception) {
            "Sir, local matrix link is unreachable."
        }
    }

    // Battery State Reader
    fun getBatteryDiagnostics(context: Context): String {
        return try {
            val batteryStatus: Intent? = context.registerReceiver(null, android.content.IntentFilter(Intent.ACTION_BATTERY_CHANGED))
            val level = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
            val scale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
            val pct = (level.toFloat() / scale.toFloat() * 100).toInt()

            val chargePlug = batteryStatus?.getIntExtra(BatteryManager.EXTRA_PLUGGED, -1) ?: -1
            val isCharging = chargePlug == BatteryManager.BATTERY_PLUGGED_AC || chargePlug == BatteryManager.BATTERY_PLUGGED_USB

            val chargingStatus = if (isCharging) "Charging (AC/USB Power core on-grid)" else "Discharging (Operating on internal arc reactor reserves)"
            "Sir, battery cells at $pct%. Current state: $chargingStatus."
        } catch (e: Exception) {
            "Sir, power-level sensors are non-responsive."
        }
    }

    // Read Storage info
    fun getStorageDiagnostics(): String {
        return try {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            val availableBlocks = stat.availableBlocksLong

            val totalSizeGB = (totalBlocks * blockSize) / (1024 * 1024 * 1024)
            val freeSizeGB = (availableBlocks * blockSize) / (1024 * 1024 * 1024)
            val usedSizeGB = totalSizeGB - freeSizeGB

            "Sir, total main storage: $totalSizeGB GB. Used: $usedSizeGB GB. Available headroom: $freeSizeGB GB."
        } catch (e: Exception) {
            "Sir, storage sector analysis is compromised."
        }
    }

    // Diagnose general device memory
    fun getSystemMetrics(context: Context): String {
        return try {
            val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val memInfo = android.app.ActivityManager.MemoryInfo()
            actManager.getMemoryInfo(memInfo)
            val totalMemGB = memInfo.totalMem / (1024 * 1024 * 1024.0)
            val availMemGB = memInfo.availMem / (1024 * 1024 * 1024.0)
            val percentUsed = ((memInfo.totalMem - memInfo.availMem).toDouble() / memInfo.totalMem * 100).toInt()

            "Sir, system diagnostics indicate a total of %.2f GB RAM. Free memory is %.2f GB (We are load-balanced with %d%% memory utilization).".format(totalMemGB, availMemGB, percentUsed)
        } catch (e: Exception) {
            "Sir, micro-processor sub-registers are unreadable."
        }
    }

    // Automatic intent router based on keyword matching
    fun tryParseAndExecute(context: Context, text: String): String? {
        val cmd = text.lowercase(Locale.ROOT)
        return when {
            cmd.contains("flashlight on") || cmd.contains("torch on") || cmd.contains("light on") -> {
                toggleFlashlight(context, true)
            }
            cmd.contains("flashlight off") || cmd.contains("torch off") || cmd.contains("light off") -> {
                toggleFlashlight(context, false)
            }
            cmd.contains("battery") || cmd.contains("power level") -> {
                getBatteryDiagnostics(context)
            }
            cmd.contains("storage") || cmd.contains("disk space") -> {
                getStorageDiagnostics()
            }
            cmd.contains("system diagnostics") || cmd.contains("ram status") || cmd.contains("system status") -> {
                getSystemMetrics(context)
            }
            cmd.contains("settings for") || cmd.contains("open settings") -> {
                val settingsType = when {
                    cmd.contains("wifi") -> "wifi"
                    cmd.contains("bluetooth") -> "bluetooth"
                    cmd.contains("display") || cmd.contains("brightness") -> "display"
                    else -> "general"
                }
                openSettings(context, settingsType)
            }
            cmd.contains("map to") || cmd.contains("where is") -> {
                val query = text.substringAfter("map to").substringAfter("where is").trim()
                if (query.isNotEmpty()) launchMaps(context, query) else null
            }
            cmd.contains("set alarm to") || cmd.contains("set alarm for") -> {
                // simple parser for e.g. "set alarm to 7 30" or "set alarm for 14 0"
                val numbers = "\\d+".toRegex().findAll(text).map { it.value.toInt() }.toList()
                val hr = numbers.getOrNull(0) ?: 8
                val min = numbers.getOrNull(1) ?: 0
                setAlarm(context, hr, min, "J.A.R.V.I.S. Wake Alarm")
            }
            cmd.contains("dial") || cmd.contains("call number") -> {
                val rawNumber = text.replace(Regex("[^0-9]"), "")
                if (rawNumber.isNotEmpty()) dialNumber(context, rawNumber) else null
            }
            cmd.contains("whatsapp message") || cmd.contains("whatsapp message") || cmd.contains("whatsapp") -> {
                val query = text.replace("whatsapp message", "").replace("whatsapp", "").trim()
                if (query.isNotEmpty()) sendWhatsApp(context, query) else null
            }
            else -> null
        }
    }
}
