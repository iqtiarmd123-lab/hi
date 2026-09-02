package com.example.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.data.model.IsoMetadata
import com.example.data.model.OsArchitecture
import com.example.data.model.OsPreset
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.InputStream

object IsoManager {

    // Built-in presets that provide immediate testable OS images
    val BUILT_IN_SAMPLE_ISOS = listOf(
        IsoMetadata(
            uri = "builtin://alpine_micro",
            fileName = "alpine-virt-3.19-x86.iso",
            fileSizeBytes = 58 * 1024 * 1024L,
            volumeIdentifier = "ALPINE_VIRT_X86",
            systemIdentifier = "EL_TORITO_VBIOS",
            isBootable = true,
            detectedArch = OsArchitecture.X86,
            estimatedRamRequiredMb = 256,
            isBuiltInSample = true,
            samplePreset = OsPreset.ALPINE_LINUX
        ),
        IsoMetadata(
            uri = "builtin://tinycore_gui",
            fileName = "TinyCore-v15.0-x86.iso",
            fileSizeBytes = 23 * 1024 * 1024L,
            volumeIdentifier = "TINYCORE_DESKTOP",
            systemIdentifier = "ISOLINUX_BOOT",
            isBootable = true,
            detectedArch = OsArchitecture.X86,
            estimatedRamRequiredMb = 128,
            isBuiltInSample = true,
            samplePreset = OsPreset.TINYCORE_LINUX
        ),
        IsoMetadata(
            uri = "builtin://freedos_13",
            fileName = "FreeDOS-1.3-LiveCD.iso",
            fileSizeBytes = 45 * 1024 * 1024L,
            volumeIdentifier = "FREEDOS_130",
            systemIdentifier = "SYSLINUX_BOOT",
            isBootable = true,
            detectedArch = OsArchitecture.X86,
            estimatedRamRequiredMb = 64,
            isBuiltInSample = true,
            samplePreset = OsPreset.FREEDOS
        ),
        IsoMetadata(
            uri = "builtin://kolibri_os",
            fileName = "kolibri-0.7.7.0-live.iso",
            fileSizeBytes = 6 * 1024 * 1024L,
            volumeIdentifier = "KOLIBRI_OS_ASM",
            systemIdentifier = "RAW_BOOT_MBR",
            isBootable = true,
            detectedArch = OsArchitecture.X86,
            estimatedRamRequiredMb = 64,
            isBuiltInSample = true,
            samplePreset = OsPreset.KOLIBRI_OS
        ),
        IsoMetadata(
            uri = "builtin://minimal_linux_live",
            fileName = "minimal-linux-livecd.iso",
            fileSizeBytes = 12 * 1024 * 1024L,
            volumeIdentifier = "MINIMAL_LINUX",
            systemIdentifier = "GRUB2_EFI_BIOS",
            isBootable = true,
            detectedArch = OsArchitecture.X86,
            estimatedRamRequiredMb = 128,
            isBuiltInSample = true,
            samplePreset = OsPreset.LINUX_GENERIC
        )
    )

    suspend fun inspectIsoUri(context: Context, uri: Uri): IsoMetadata = withContext(Dispatchers.IO) {
        var fileName = "unknown_disk.iso"
        var fileSize = 0L

        // Query metadata from ContentResolver
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                if (nameIndex >= 0) {
                    fileName = cursor.getString(nameIndex) ?: fileName
                }
                if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                    fileSize = cursor.getLong(sizeIndex)
                }
            }
        }

        // Parse ISO 9660 Volume Descriptor if accessible
        var volId = "ISO_VOLUME"
        var sysId = "EL_TORITO_CD"
        var isBootable = true

        try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                // Sector size is 2048 bytes. Sector 16 (offset 32768) is the Primary Volume Descriptor
                val skipped = stream.skip(32768)
                if (skipped == 32768L) {
                    val pvdHeader = ByteArray(2048)
                    val read = stream.read(pvdHeader)
                    if (read >= 2048) {
                        // Check standard identifier "CD001" at offset 1..5
                        val identifier = String(pvdHeader, 1, 5, Charsets.US_ASCII)
                        if (identifier == "CD001") {
                            sysId = String(pvdHeader, 8, 32, Charsets.US_ASCII).trim()
                            volId = String(pvdHeader, 40, 32, Charsets.US_ASCII).trim()
                        }
                    }
                }
            }
        } catch (e: Exception) {
            // Keep default volume IDs if raw stream skip fails
        }

        val detectedArch = when {
            fileName.contains("64", ignoreCase = true) || fileName.contains("x86_64", ignoreCase = true) || fileName.contains("amd64", ignoreCase = true) -> OsArchitecture.X86_64
            fileName.contains("arm", ignoreCase = true) || fileName.contains("aarch64", ignoreCase = true) -> OsArchitecture.ARM64
            else -> OsArchitecture.X86
        }

        val estimatedRam = when {
            fileSize > 2L * 1024 * 1024 * 1024 -> 2048
            fileSize > 800L * 1024 * 1024 -> 1024
            fileSize > 300L * 1024 * 1024 -> 512
            else -> 256
        }

        IsoMetadata(
            uri = uri.toString(),
            fileName = fileName,
            fileSizeBytes = fileSize,
            volumeIdentifier = if (volId.isNotBlank()) volId else "GENERIC_ISO",
            systemIdentifier = if (sysId.isNotBlank()) sysId else "STANDARD_PC",
            isBootable = isBootable,
            detectedArch = detectedArch,
            estimatedRamRequiredMb = estimatedRam,
            isBuiltInSample = false
        )
    }

    /**
     * Copy an ISO into internal storage if requested or caching is needed for faster block access
     */
    suspend fun cacheIsoLocally(context: Context, uri: Uri, targetName: String): File = withContext(Dispatchers.IO) {
        val isoDir = File(context.filesDir, "isos").apply { mkdirs() }
        val targetFile = File(isoDir, targetName)
        if (targetFile.exists() && targetFile.length() > 0) {
            return@withContext targetFile
        }

        context.contentResolver.openInputStream(uri)?.use { input ->
            targetFile.outputStream().use { output ->
                input.copyTo(output, bufferSize = 64 * 1024)
            }
        }
        targetFile
    }
}
