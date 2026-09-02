package com.example.ui.screens

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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VirtualMachine
import com.example.data.model.VmStatus
import com.example.system.DeviceHardwareInfo
import com.example.ui.theme.CyberBlue
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalPurple
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.viewmodel.VmViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VmViewModel,
    onCreateVmClick: () -> Unit,
    onImportIsoClick: () -> Unit,
    onStartVmClick: (VirtualMachine) -> Unit,
    onViewSnapshotsClick: (VirtualMachine) -> Unit,
    modifier: Modifier = Modifier
) {
    val vms by viewModel.vms.collectAsState()
    val hardwareInfo by viewModel.hardwareInfo.collectAsState()
    val activeEngine by viewModel.activeEngine.collectAsState()

    var vmToDelete by remember { mutableStateOf<VirtualMachine?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .background(
                                    Brush.linearGradient(listOf(CyberCyan, CyberBlue)),
                                    RoundedCornerShape(8.dp)
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("💻", fontSize = 18.sp)
                        }
                        Spacer(Modifier.width(10.dp))
                        Column {
                            Text(
                                "Mobile Virtual OS",
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = TextPrimary
                            )
                            Text(
                                "Native Local Virtual Machine",
                                fontSize = 11.sp,
                                color = CyberCyan
                            )
                        }
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.refreshHardware() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh Hardware Status", tint = CyberCyan)
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = DarkSurface
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = onCreateVmClick,
                containerColor = CyberCyan,
                contentColor = Color.Black,
                modifier = Modifier.testTag("create_vm_fab")
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create New VM")
            }
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
            // Hardware Status Banner
            item {
                HardwareStatusCard(hardwareInfo)
            }

            // Quick Actions Row
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onCreateVmClick,
                        colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_create_new_vm")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Create VM", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }

                    OutlinedButton(
                        onClick = onImportIsoClick,
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = CyberNavyCard,
                            contentColor = CyberCyan
                        ),
                        border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.6f)),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("btn_import_iso")
                    ) {
                        Icon(Icons.Default.Folder, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(6.dp))
                        Text("Import ISO", fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
                    }
                }
            }

            // Virtual Machines Section Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Virtual Machines Library (${vms.size})",
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        color = TextPrimary
                    )
                    if (activeEngine != null) {
                        Text(
                            "● 1 VM Active",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = TerminalGreen
                        )
                    }
                }
            }

            // VM List
            if (vms.isEmpty()) {
                item {
                    EmptyVmState(onCreateVmClick)
                }
            } else {
                items(vms, key = { it.id }) { vm ->
                    VmCard(
                        vm = vm,
                        isActive = activeEngine?.vm?.id == vm.id,
                        onStartClick = { onStartVmClick(vm) },
                        onSnapshotsClick = { onViewSnapshotsClick(vm) },
                        onDeleteClick = { vmToDelete = vm }
                    )
                }
            }

            // Architecture Info Card at the bottom
            item {
                ArchitectureInfoCard()
            }
        }
    }

    // Delete Confirmation Dialog
    vmToDelete?.let { vm ->
        AlertDialog(
            onDismissRequest = { vmToDelete = null },
            title = { Text("Delete Virtual Machine?", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Are you sure you want to delete '${vm.name}' and its virtual disk (${vm.diskSizeMb} MB)? This action cannot be undone.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.deleteVirtualMachine(vm)
                        vmToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = TerminalRed)
                ) {
                    Text("Delete Permanently")
                }
            },
            dismissButton = {
                TextButton(onClick = { vmToDelete = null }) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = CyberNavyCard
        )
    }
}

@Composable
private fun HardwareStatusCard(info: DeviceHardwareInfo) {
    Card(
        colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder),
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
                    Icon(Icons.Default.Memory, contentDescription = null, tint = CyberCyan, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text("Phone Hardware Diagnostics", fontSize = 13.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                }
                Text(
                    "Safe Mode OK",
                    fontSize = 11.sp,
                    color = TerminalGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HardwareStatItem("CPU Cores", "${info.cpuCores} Cores", "Safe: ${info.maxSafeVmCores} vCPU")
                HardwareStatItem("RAM Available", info.availableRamFormatted, "Total: ${info.totalRamFormatted}")
                HardwareStatItem("Free Storage", info.freeStorageFormatted, "Internal disk")
            }
        }
    }
}

@Composable
private fun HardwareStatItem(label: String, value: String, subtext: String) {
    Column {
        Text(label, fontSize = 11.sp, color = TextSecondary)
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Bold, color = CyberCyan)
        Text(subtext, fontSize = 10.sp, color = TextSecondary)
    }
}

@Composable
private fun VmCard(
    vm: VirtualMachine,
    isActive: Boolean,
    onStartClick: () -> Unit,
    onSnapshotsClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) CyberNavyCard else DarkSurface
        ),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            if (isActive) CyberCyan else DarkCardBorder
        ),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("vm_card_${vm.id}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header: Icon + Name + Status
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .background(
                                color = when (vm.osPreset.iconCategory) {
                                    "windows" -> CyberBlue.copy(alpha = 0.2f)
                                    "dos" -> TerminalAmber.copy(alpha = 0.2f)
                                    "kolibri" -> TerminalPurple.copy(alpha = 0.2f)
                                    else -> CyberCyan.copy(alpha = 0.2f)
                                },
                                shape = RoundedCornerShape(10.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            when (vm.osPreset.iconCategory) {
                                "windows" -> "🪟"
                                "dos" -> "💾"
                                "kolibri" -> "🐦"
                                else -> "🐧"
                            },
                            fontSize = 20.sp
                        )
                    }

                    Spacer(Modifier.width(12.dp))

                    Column {
                        Text(
                            vm.name,
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = TextPrimary
                        )
                        Text(
                            vm.osPreset.displayName,
                            fontSize = 12.sp,
                            color = CyberCyan
                        )
                    }
                }

                // Status Badge
                Surface(
                    color = if (isActive) TerminalGreen.copy(alpha = 0.2f) else CyberNavySurface,
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (isActive) TerminalGreen else CyberNavyBorder
                    )
                ) {
                    Text(
                        text = if (isActive) "● RUNNING" else "STOPPED",
                        color = if (isActive) TerminalGreen else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            // Spec Pills Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                SpecBadge("⚡ ${vm.cpuCores} vCPU")
                SpecBadge("🧠 ${vm.ramMb} MB RAM")
                SpecBadge("💾 ${vm.diskSizeMb} MB Disk")
            }

            Spacer(Modifier.height(10.dp))

            // ISO Source Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DarkBackground.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("💿", fontSize = 13.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    text = vm.isoName ?: "No ISO attached (Clean Disk Boot)",
                    fontSize = 12.sp,
                    color = if (vm.isoName != null) TerminalGreen else TextSecondary,
                    maxLines = 1
                )
            }

            Spacer(Modifier.height(14.dp))

            // Action Buttons
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Button(
                    onClick = onStartClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isActive) TerminalGreen else CyberCyan,
                        contentColor = Color.Black
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp)
                        .testTag("start_vm_${vm.id}")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(Modifier.width(6.dp))
                    Text(
                        if (isActive) "OPEN DISPLAY" else "START VM",
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }

                IconButton(
                    onClick = onSnapshotsClick,
                    modifier = Modifier
                        .background(CyberNavyCard, RoundedCornerShape(8.dp))
                        .size(44.dp)
                ) {
                    Icon(Icons.Default.CameraAlt, contentDescription = "Snapshots", tint = TerminalAmber)
                }

                IconButton(
                    onClick = onDeleteClick,
                    modifier = Modifier
                        .background(CyberNavyCard, RoundedCornerShape(8.dp))
                        .size(44.dp)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete VM", tint = TerminalRed)
                }
            }
        }
    }
}

@Composable
private fun SpecBadge(text: String) {
    Surface(
        color = CyberNavySurface,
        shape = RoundedCornerShape(6.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder)
    ) {
        Text(
            text = text,
            fontSize = 11.sp,
            color = TextPrimary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

@Composable
private fun EmptyVmState(onCreateClick: () -> Unit) {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
        shape = RoundedCornerShape(14.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("💻", fontSize = 42.sp)
            Spacer(Modifier.height(10.dp))
            Text("No Virtual Machines Created", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TextPrimary)
            Spacer(Modifier.height(6.dp))
            Text(
                "Create your first virtual machine or import an ISO operating system from your phone storage to begin.",
                fontSize = 12.sp,
                color = TextSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onCreateClick,
                colors = ButtonDefaults.buttonColors(containerColor = CyberCyan, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Create Virtual Machine Now", fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
private fun ArchitectureInfoCard() {
    Card(
        colors = CardDefaults.cardColors(containerColor = DarkSurfaceElevated.copy(alpha = 0.5f)),
        shape = RoundedCornerShape(10.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                "ℹ️ Virtualization Architecture",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = CyberCyan
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Mobile Virtual OS runs fully local on-device x86 CPU emulation, virtual IDE block storage, and VGA framebuffer without relying on any external cloud server. Data is stored safely in your phone's isolated sandbox.",
                fontSize = 11.sp,
                color = TextSecondary,
                lineHeight = 16.sp
            )
        }
    }
}
