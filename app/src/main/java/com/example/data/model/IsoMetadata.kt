package com.example.data.model

data class IsoMetadata(
    val uri: String,
    val fileName: String,
    val fileSizeBytes: Long,
    val volumeIdentifier: String = "LINUX_BOOT",
    val systemIdentifier: String = "EL_TORITO_VBIOS",
    val isBootable: Boolean = true,
    val detectedArch: OsArchitecture = OsArchitecture.X86,
    val estimatedRamRequiredMb: Int = 256,
    val isBuiltInSample: Boolean = false,
    val samplePreset: OsPreset? = null
) {
    val formattedSize: String
        get() {
            if (fileSizeBytes <= 0) return "0 MB"
            val mb = fileSizeBytes / (1024.0 * 1024.0)
            return if (mb >= 1024) {
                String.format("%.2f GB", mb / 1024.0)
            } else {
                String.format("%.1f MB", mb)
            }
        }
}
