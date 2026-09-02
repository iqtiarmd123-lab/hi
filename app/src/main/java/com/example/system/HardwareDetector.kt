package com.example.system

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File

data class DeviceHardwareInfo(
    val cpuCores: Int,
    val totalRamMb: Long,
    val availableRamMb: Long,
    val internalStorageFreeMb: Long,
    val deviceModel: String,
    val androidVersion: String,
    val supportedAbis: List<String>,
    val maxSafeVmRamMb: Int,
    val recommendedVmRamMb: Int,
    val maxSafeVmCores: Int,
    val hasHardwareAcceleration: Boolean
) {
    val totalRamFormatted: String get() = formatMb(totalRamMb)
    val availableRamFormatted: String get() = formatMb(availableRamMb)
    val freeStorageFormatted: String get() = formatMb(internalStorageFreeMb)

    private fun formatMb(mb: Long): String {
        return if (mb >= 1024) {
            String.format("%.1f GB", mb / 1024.0)
        } else {
            "$mb MB"
        }
    }
}

object HardwareDetector {

    fun detectHardware(context: Context): DeviceHardwareInfo {
        val cores = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

        val actManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        actManager?.getMemoryInfo(memInfo)

        val totalRamMb = (memInfo.totalMem / (1024 * 1024)).coerceAtLeast(512)
        val availRamMb = (memInfo.availMem / (1024 * 1024)).coerceAtLeast(256)

        val freeStorageMb = runCatching {
            val stat = StatFs(context.filesDir.absolutePath)
            (stat.availableBlocksLong * stat.blockSizeLong) / (1024 * 1024)
        }.getOrDefault(2048L)

        val model = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}"
        val androidVer = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})"
        val abis = Build.SUPPORTED_ABIS.toList()

        // Calculate safe allocation for Android VM environment
        // Android apps cannot allocate all physical RAM without LowMemoryKiller killing the app
        val maxSafeRam = (availRamMb * 0.75).toInt().coerceIn(128, 4096)
        val recommendedRam = when {
            availRamMb >= 4096 -> 1024
            availRamMb >= 2048 -> 512
            availRamMb >= 1024 -> 256
            else -> 128
        }
        val maxSafeCores = when {
            cores >= 8 -> 4
            cores >= 4 -> 2
            else -> 1
        }

        val hasAccel = Build.SUPPORTED_ABIS.any { it.contains("arm64") || it.contains("x86") }

        return DeviceHardwareInfo(
            cpuCores = cores,
            totalRamMb = totalRamMb,
            availableRamMb = availRamMb,
            internalStorageFreeMb = freeStorageMb,
            deviceModel = model,
            androidVersion = androidVer,
            supportedAbis = abis,
            maxSafeVmRamMb = maxSafeRam,
            recommendedVmRamMb = recommendedRam,
            maxSafeVmCores = maxSafeCores,
            hasHardwareAcceleration = hasAccel
        )
    }
}
