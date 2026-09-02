package com.example.storage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.RandomAccessFile

data class VirtualDiskInfo(
    val fileName: String,
    val path: String,
    val sizeMb: Int,
    val actualSizeBytes: Long,
    val exists: Boolean,
    val isFormatted: Boolean
) {
    val formattedSize: String
        get() = if (sizeMb >= 1024) String.format("%.1f GB", sizeMb / 1024.0) else "$sizeMb MB"
}

object VirtualDiskManager {

    private fun getDisksDir(context: Context): File {
        return File(context.filesDir, "disks").apply { mkdirs() }
    }

    suspend fun createOrEnsureDisk(context: Context, diskFileName: String, sizeMb: Int): VirtualDiskInfo = withContext(Dispatchers.IO) {
        val dir = getDisksDir(context)
        val sanitizedName = if (diskFileName.endsWith(".img")) diskFileName else "$diskFileName.img"
        val diskFile = File(dir, sanitizedName)

        val targetSizeBytes = sizeMb.toLong() * 1024L * 1024L

        if (!diskFile.exists() || diskFile.length() < 512) {
            // Create sparse virtual disk file with MBR signature
            RandomAccessFile(diskFile, "rw").use { raf ->
                // Write standard MBR sector (512 bytes) with 0x55AA boot signature
                val mbr = ByteArray(512)
                mbr[510] = 0x55.toByte()
                mbr[511] = 0xAA.toByte()
                raf.write(mbr)

                // Seek to target size to create sparse file without immediately consuming phone storage
                raf.setLength(targetSizeBytes)
            }
        }

        VirtualDiskInfo(
            fileName = sanitizedName,
            path = diskFile.absolutePath,
            sizeMb = sizeMb,
            actualSizeBytes = diskFile.length(),
            exists = true,
            isFormatted = true
        )
    }

    suspend fun listDisks(context: Context): List<VirtualDiskInfo> = withContext(Dispatchers.IO) {
        val dir = getDisksDir(context)
        val files = dir.listFiles { f -> f.extension == "img" || f.extension == "vhd" } ?: emptyArray()
        files.map { file ->
            val sizeMb = (file.length() / (1024 * 1024)).toInt().coerceAtLeast(1)
            VirtualDiskInfo(
                fileName = file.name,
                path = file.absolutePath,
                sizeMb = sizeMb,
                actualSizeBytes = file.length(),
                exists = true,
                isFormatted = file.length() >= 512
            )
        }
    }

    suspend fun deleteDisk(context: Context, diskFileName: String): Boolean = withContext(Dispatchers.IO) {
        val dir = getDisksDir(context)
        val file = File(dir, diskFileName)
        if (file.exists()) {
            file.delete()
        } else {
            false
        }
    }
}
