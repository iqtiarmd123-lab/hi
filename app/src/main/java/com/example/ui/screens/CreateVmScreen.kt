package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BootDevice
import com.example.data.model.IsoMetadata
import com.example.data.model.OsPreset
import com.example.storage.IsoManager
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.VmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateVmScreen(
    viewModel: VmViewModel,
    onBackClick: () -> Unit,
    onVmCreated: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val hardwareInfo by viewModel.hardwareInfo.collectAsState()
    val selectedIso by viewModel.selectedIso.collectAsState()

    var vmName by remember { mutableStateOf("Linux VM " + (10..99).random()) }
    var selectedPreset by remember { mutableStateOf(OsPreset.ALPINE_LINUX) }
    var cpuCores by remember { mutableIntStateOf(1) }
    var ramMb by remember { mutableIntStateOf(256) }
    var diskMb by remember { mutableIntStateOf(1024) }
    var bootDevice by remember { mutableStateOf(BootDevice.CD_ROM_ISO) }
    var networkEnabled by remember { mutableStateOf(true) }
    var audioEnabled by remember { mutableStateOf(true) }

    // File picker launcher for ISO files
    val isoFilePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectIsoUri(it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Create Virtual Machine",
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = TextPrimary
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = CyberCyan
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        containerColor = DarkBackground,
        modifier = modifier
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // 1. VM Name and OS Preset Selection
            item {
                SectionCard(title = "1. General Information & OS Preset") {
                    OutlinedTextField(
                        value = vmName,
                        onValueChange = { vmName = it },
                        label = { Text("Virtual Machine Name", color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberNavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_vm_name")
                    )

                    Spacer(Modifier.height(14.dp))

                    Text("Select OS Preset Template:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(Modifier.height(8.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(OsPreset.values()) { preset ->
                            PresetChip(
                                preset = preset,
                                isSelected = preset == selectedPreset,
                                onClick = {
                                    selectedPreset = preset
                                    cpuCores = preset.recommendedCores.coerceAtMost(hardwareInfo.maxSafeVmCores)
                                    ramMb = preset.recommendedRamMb.coerceAtMost(hardwareInfo.maxSafeVmRamMb)
                                    diskMb = preset.recommendedDiskMb
                                    // auto match sample if available
                                    IsoManager.BUILT_IN_SAMPLE_ISOS.firstOrNull { it.samplePreset == preset }?.let {
                                        viewModel.selectBuiltInSampleIso(it)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // 2. ISO File Selection (Storage or Built-in)
            item {
                SectionCard(title = "2. Boot ISO File Selection") {
                    Text(
                        "Select an ISO image from your phone's storage, or use a lightweight built-in sample OS to boot.",
                        fontSize = 12.sp,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(10.dp))

                    // Selected ISO Display Card
                    if (selectedIso != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = CyberNavyCard),
                            border = androidx.compose.foundation.BorderStroke(1.dp, TerminalGreen),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text("💿", fontSize = 20.sp)
                                        Spacer(Modifier.width(8.dp))
                                        Column {
                                            Text(
                                                selectedIso!!.fileName,
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 13.sp,
                                                color = TextPrimary
                                            )
                                            Text(
                                                "Size: ${selectedIso!!.formattedSize} • Arch: ${selectedIso!!.detectedArch.label}",
                                                fontSize = 11.sp,
                                                color = CyberCyan
                                            )
                                        }
                                    }
                                    IconButton(onClick = { viewModel.clearSelectedIso() }) {
                                        Icon(Icons.Default.Close, contentDescription = "Remove ISO", tint = TerminalRed)
                                    }
                                }

                                Spacer(Modifier.height(6.dp))
                                Text(
                                    "Path / Uri: ${selectedIso!!.uri}",
                                    fontSize = 10.sp,
                                    color = TextSecondary,
                                    maxLines = 1
                                )
                            }
                        }
                    } else {
                        // Empty ISO Picker CTA
                        OutlinedButton(
                            onClick = {
                                isoFilePicker.launch(arrayOf("*/*", "application/x-iso9660-image", "application/octet-stream"))
                            },
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = CyberNavyCard,
                                contentColor = CyberCyan
                            ),
                            border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_select_iso_file")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(Modifier.width(8.dp))
                            Text("Browse Phone Storage for .ISO", fontWeight = FontWeight.SemiBold)
                        }
                    }

                    Spacer(Modifier.height(12.dp))

                    Text("Or choose a pre-loaded lightweight sample ISO:", fontSize = 12.sp, color = TextSecondary)
                    Spacer(Modifier.height(6.dp))

                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        IsoManager.BUILT_IN_SAMPLE_ISOS.forEach { sample ->
                            SampleIsoRow(
                                sample = sample,
                                isSelected = selectedIso?.uri == sample.uri,
                                onClick = { viewModel.selectBuiltInSampleIso(sample) }
                            )
                        }
                    }
                }
            }

            // 3. Virtual Hardware Setup (CPU & RAM)
            item {
                SectionCard(title = "3. Virtual CPU & Memory (RAM)") {
                    // CPU Cores Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Virtual CPU Cores:", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("$cpuCores vCPU", fontSize = 14.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                    }
                    Slider(
                        value = cpuCores.toFloat(),
                        onValueChange = { cpuCores = it.toInt() },
                        valueRange = 1f..hardwareInfo.maxSafeVmCores.toFloat().coerceAtLeast(1f),
                        steps = (hardwareInfo.maxSafeVmCores - 2).coerceAtLeast(0),
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = CyberNavyBorder
                        ),
                        modifier = Modifier.testTag("slider_cpu_cores")
                    )
                    Text(
                        "Safe recommendation: Up to ${hardwareInfo.maxSafeVmCores} vCPU (${hardwareInfo.cpuCores} physical cores available)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(16.dp))

                    // RAM Slider
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Virtual RAM Allocation:", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text("$ramMb MB", fontSize = 14.sp, color = CyberCyan, fontWeight = FontWeight.Bold)
                    }

                    val ramOptions = listOf(64, 128, 256, 512, 1024, 2048).filter { it <= hardwareInfo.maxSafeVmRamMb }
                    val currentRamIndex = ramOptions.indexOf(ramMb).coerceAtLeast(0)

                    Slider(
                        value = currentRamIndex.toFloat(),
                        onValueChange = { ramMb = ramOptions[it.toInt().coerceIn(0, ramOptions.size - 1)] },
                        valueRange = 0f..(ramOptions.size - 1).toFloat(),
                        steps = (ramOptions.size - 2).coerceAtLeast(0),
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = CyberNavyBorder
                        ),
                        modifier = Modifier.testTag("slider_ram_mb")
                    )
                    Text(
                        "Device has ${hardwareInfo.availableRamFormatted} available RAM (Safe ceiling: ${hardwareInfo.maxSafeVmRamMb} MB)",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )
                }
            }

            // 4. Virtual Disk Storage & Boot Device
            item {
                SectionCard(title = "4. Virtual Disk & Boot Sequence") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Virtual Hard Disk Size:", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                        Text(
                            if (diskMb >= 1024) "${diskMb / 1024} GB" else "$diskMb MB",
                            fontSize = 14.sp,
                            color = CyberCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    val diskOptions = listOf(256, 512, 1024, 2048, 4096, 8192)
                    var diskIndex by remember { mutableIntStateOf(2) }

                    Slider(
                        value = diskIndex.toFloat(),
                        onValueChange = {
                            diskIndex = it.toInt()
                            diskMb = diskOptions[diskIndex]
                        },
                        valueRange = 0f..(diskOptions.size - 1).toFloat(),
                        steps = diskOptions.size - 2,
                        colors = SliderDefaults.colors(
                            thumbColor = CyberCyan,
                            activeTrackColor = CyberCyan,
                            inactiveTrackColor = CyberNavyBorder
                        ),
                        modifier = Modifier.testTag("slider_disk_size")
                    )
                    Text(
                        "Created as a sparse raw disk image. Persists user files across reboots safely.",
                        fontSize = 11.sp,
                        color = TextSecondary
                    )

                    Spacer(Modifier.height(14.dp))

                    Text("Primary Boot Device:", fontSize = 13.sp, color = TextSecondary)
                    Spacer(Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        BootDeviceOption(
                            label = "CD-ROM (ISO)",
                            isSelected = bootDevice == BootDevice.CD_ROM_ISO,
                            onClick = { bootDevice = BootDevice.CD_ROM_ISO }
                        )
                        BootDeviceOption(
                            label = "Virtual Hard Disk",
                            isSelected = bootDevice == BootDevice.HARD_DISK,
                            onClick = { bootDevice = BootDevice.HARD_DISK }
                        )
                    }
                }
            }

            // 5. Networking & Audio Options
            item {
                SectionCard(title = "5. Peripheral & Network Options") {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Virtual Network (NAT)", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Provide internet connectivity inside the VM", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = networkEnabled,
                            onCheckedChange = { networkEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = CyberNavyCard
                            )
                        )
                    }

                    Spacer(Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text("Virtual PC Speaker Audio", fontSize = 13.sp, color = TextPrimary, fontWeight = FontWeight.SemiBold)
                            Text("Synthesize AC97/BIOS audio tones", fontSize = 11.sp, color = TextSecondary)
                        }
                        Switch(
                            checked = audioEnabled,
                            onCheckedChange = { audioEnabled = it },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = CyberCyan,
                                checkedTrackColor = CyberNavyCard
                            )
                        )
                    }
                }
            }

            // 6. Submit Button
            item {
                Button(
                    onClick = {
                        viewModel.createVirtualMachine(
                            name = vmName,
                            preset = selectedPreset,
                            cpuCores = cpuCores,
                            ramMb = ramMb,
                            diskMb = diskMb,
                            bootDevice = bootDevice,
                            isoMeta = selectedIso,
                            networkEnabled = networkEnabled,
                            audioEnabled = audioEnabled,
                            onCreated = onVmCreated
                        )
                    },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .testTag("btn_submit_create_vm")
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        "CREATE & START VIRTUAL MACHINE",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp
                    )
                }
                Spacer(Modifier.height(20.dp))
            }
        }
    }
}

@Composable
private fun SectionCard(title: String, content: @Composable () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = CyberCyan)
            Spacer(Modifier.height(12.dp))
            content()
        }
    }
}

@Composable
private fun PresetChip(
    preset: OsPreset,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberNavySurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CyberCyan else CyberNavyBorder
        ),
        modifier = Modifier.clickable { onClick() }
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Text(
                when (preset.iconCategory) {
                    "windows" -> "🪟"
                    "dos" -> "💾"
                    "kolibri" -> "🐦"
                    else -> "🐧"
                },
                fontSize = 14.sp
            )
            Spacer(Modifier.width(6.dp))
            Text(
                preset.displayName,
                fontSize = 12.sp,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                color = if (isSelected) CyberCyan else TextPrimary
            )
        }
    }
}

@Composable
private fun SampleIsoRow(
    sample: IsoMetadata,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) CyberNavyCard else DarkBackground.copy(alpha = 0.6f),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CyberCyan else DarkCardBorder
        ),
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("💿", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(sample.fileName, fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                    Text("${sample.formattedSize} • ${sample.volumeIdentifier}", fontSize = 10.sp, color = TextSecondary)
                }
            }
            if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = "Selected", tint = CyberCyan, modifier = Modifier.size(18.dp))
            }
        }
    }
}

@Composable
private fun BootDeviceOption(
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        color = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberNavySurface,
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyberCyan else CyberNavyBorder),
        modifier = Modifier.clickable { onClick() }
    ) {
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) CyberCyan else TextPrimary,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )
    }
}
