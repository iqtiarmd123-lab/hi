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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import com.example.ui.theme.TerminalPurple
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.VmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IsoManagerScreen(
    viewModel: VmViewModel,
    onBackClick: () -> Unit,
    onQuickBootIso: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedIso by viewModel.selectedIso.collectAsState()

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.selectIsoUri(it) }
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "ISO Operating Systems",
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Import File Section
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "📂 Import Custom ISO from Phone",
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp,
                            color = TextPrimary
                        )
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "Select any Linux, DOS, Windows, or custom OS .iso file stored in your internal storage or SD card.",
                            fontSize = 12.sp,
                            color = TextSecondary
                        )
                        Spacer(Modifier.height(14.dp))

                        Button(
                            onClick = {
                                filePicker.launch(arrayOf("*/*", "application/x-iso9660-image", "application/octet-stream"))
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(46.dp)
                                .testTag("btn_import_custom_iso")
                        ) {
                            Icon(Icons.Default.FolderOpen, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("Open Phone Storage Browser", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Currently Selected ISO Card
            selectedIso?.let { iso ->
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalGreen),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = TerminalGreen)
                                    Spacer(Modifier.width(8.dp))
                                    Text("Active Selected ISO", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                                }
                                IconButton(onClick = { viewModel.clearSelectedIso() }) {
                                    Icon(Icons.Default.Close, contentDescription = "Clear", tint = TerminalRed)
                                }
                            }

                            Spacer(Modifier.height(10.dp))
                            Text(iso.fileName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = CyberCyan)
                            Spacer(Modifier.height(6.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                IsoPill("Size: ${iso.formattedSize}")
                                IsoPill("Arch: ${iso.detectedArch.label}")
                                IsoPill("Boot: ${iso.systemIdentifier}")
                            }

                            Spacer(Modifier.height(8.dp))
                            Text("Volume Label: ${iso.volumeIdentifier}", fontSize = 11.sp, color = TextSecondary)
                            Text("Estimated RAM Needed: ${iso.estimatedRamRequiredMb} MB", fontSize = 11.sp, color = TextSecondary)

                            Spacer(Modifier.height(14.dp))
                            Button(
                                onClick = {
                                    viewModel.createVirtualMachine(
                                        name = iso.fileName.substringBeforeLast("."),
                                        preset = iso.samplePreset ?: OsPreset.CUSTOM,
                                        cpuCores = 1,
                                        ramMb = iso.estimatedRamRequiredMb,
                                        diskMb = 1024,
                                        bootDevice = BootDevice.CD_ROM_ISO,
                                        isoMeta = iso,
                                        networkEnabled = true,
                                        audioEnabled = true,
                                        onCreated = onQuickBootIso
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = TerminalGreen, contentColor = Color.Black),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.PlayArrow, contentDescription = null)
                                Spacer(Modifier.width(6.dp))
                                Text("QUICK BOOT THIS ISO IN NEW VM", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // Built-in Repository Header
            item {
                Text(
                    "Built-in Ready-to-Boot Operating Systems",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = TextPrimary
                )
            }

            // Built-in Sample ISO Items
            items(IsoManager.BUILT_IN_SAMPLE_ISOS) { sample ->
                SampleIsoCard(
                    sample = sample,
                    isSelected = selectedIso?.uri == sample.uri,
                    onSelect = { viewModel.selectBuiltInSampleIso(sample) },
                    onQuickBoot = {
                        viewModel.createVirtualMachine(
                            name = sample.fileName.substringBeforeLast("."),
                            preset = sample.samplePreset ?: OsPreset.LINUX_GENERIC,
                            cpuCores = 1,
                            ramMb = sample.estimatedRamRequiredMb,
                            diskMb = 512,
                            bootDevice = BootDevice.CD_ROM_ISO,
                            isoMeta = sample,
                            networkEnabled = true,
                            audioEnabled = true,
                            onCreated = onQuickBootIso
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun SampleIsoCard(
    sample: IsoMetadata,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onQuickBoot: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) CyberNavyCard else DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isSelected) CyberCyan else DarkCardBorder
        ),
        shape = RoundedCornerShape(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("💿", fontSize = 22.sp)
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text(sample.fileName, fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TextPrimary)
                        Text("${sample.volumeIdentifier} • ${sample.formattedSize}", fontSize = 11.sp, color = CyberCyan)
                    }
                }

                Surface(
                    color = CyberNavySurface,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
                ) {
                    Text(
                        text = sample.detectedArch.label,
                        fontSize = 10.sp,
                        color = TextSecondary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                OutlinedButton(
                    onClick = onSelect,
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected) CyberCyan.copy(alpha = 0.2f) else CyberNavySurface,
                        contentColor = if (isSelected) CyberCyan else TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isSelected) CyberCyan else CyberNavyBorder),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Text(if (isSelected) "Selected ✓" else "Select ISO", fontSize = 12.sp)
                }

                Button(
                    onClick = onQuickBoot,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Quick Boot", fontWeight = FontWeight.Bold, fontSize = 12.sp)
                }
            }
        }
    }
}

@Composable
private fun IsoPill(text: String) {
    Surface(
        color = CyberNavySurface,
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
    ) {
        Text(
            text = text,
            fontSize = 10.sp,
            color = TextPrimary,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
        )
    }
}
