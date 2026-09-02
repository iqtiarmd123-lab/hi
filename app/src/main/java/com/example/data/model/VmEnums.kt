package com.example.data.model

enum class OsArchitecture(val label: String, val desc: String) {
    X86("x86 (32-bit)", "Universal x86 emulation compatible with standard PC BIOS"),
    X86_64("x86_64 (64-bit)", "Modern 64-bit emulation for 64-bit Linux distributions"),
    ARM64("ARM64", "Direct native ARM execution for lightweight ARM Linux kernels")
}

enum class OsPreset(
    val displayName: String,
    val arch: OsArchitecture,
    val recommendedRamMb: Int,
    val recommendedCores: Int,
    val recommendedDiskMb: Int,
    val defaultBootDevice: BootDevice,
    val iconCategory: String
) {
    LINUX_GENERIC("Linux x86 (Live CD)", OsArchitecture.X86, 256, 1, 1024, BootDevice.CD_ROM_ISO, "linux"),
    LINUX_64("Modern Linux x86_64", OsArchitecture.X86_64, 512, 2, 2048, BootDevice.CD_ROM_ISO, "linux"),
    ALPINE_LINUX("Alpine Micro-Linux", OsArchitecture.X86, 128, 1, 512, BootDevice.CD_ROM_ISO, "alpine"),
    TINYCORE_LINUX("TinyCore Linux GUI", OsArchitecture.X86, 128, 1, 512, BootDevice.CD_ROM_ISO, "tinycore"),
    KOLIBRI_OS("KolibriOS Fast GUI", OsArchitecture.X86, 64, 1, 256, BootDevice.CD_ROM_ISO, "kolibri"),
    FREEDOS("FreeDOS 1.3 Lite", OsArchitecture.X86, 64, 1, 256, BootDevice.CD_ROM_ISO, "dos"),
    WINDOWS_XP("Windows XP / 2000", OsArchitecture.X86, 512, 1, 4096, BootDevice.CD_ROM_ISO, "windows"),
    WINDOWS_7("Windows 7 / 8.1", OsArchitecture.X86_64, 1024, 2, 8192, BootDevice.CD_ROM_ISO, "windows"),
    CUSTOM("Custom ISO / OS", OsArchitecture.X86, 256, 1, 1024, BootDevice.CD_ROM_ISO, "custom")
}

enum class BootDevice(val label: String) {
    CD_ROM_ISO("CD-ROM (.iso file)"),
    HARD_DISK("Virtual Hard Disk (.img)")
}

enum class VmStatus {
    STOPPED,
    BOOTING,
    RUNNING,
    PAUSED,
    ERROR
}
