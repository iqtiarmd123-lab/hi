package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "virtual_machines")
data class VirtualMachine(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val osPreset: OsPreset = OsPreset.LINUX_GENERIC,
    val cpuCores: Int = 1,
    val ramMb: Int = 256,
    val diskSizeMb: Int = 1024,
    val diskFileName: String = "vdisk_default.img",
    val isoUri: String? = null,
    val isoName: String? = null,
    val isoSizeBytes: Long = 0L,
    val bootDevice: BootDevice = BootDevice.CD_ROM_ISO,
    val vgaResolution: String = "800x600",
    val networkEnabled: Boolean = true,
    val audioEnabled: Boolean = true,
    val acpiEnabled: Boolean = true,
    val status: VmStatus = VmStatus.STOPPED,
    val createdAt: Long = System.currentTimeMillis(),
    val lastBootedAt: Long? = null,
    val description: String = ""
)
