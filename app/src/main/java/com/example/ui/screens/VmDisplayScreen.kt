package com.example.ui.screens

import android.view.ViewGroup
import android.webkit.WebView
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.data.model.VirtualMachine
import com.example.data.model.VmSnapshot
import com.example.data.model.VmStatus
import com.example.engine.VirtualMachineEngine
import com.example.ui.components.TouchTrackpad
import com.example.ui.components.VirtualKeyboardBar
import com.example.ui.components.VmControlBar
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
fun VmDisplayScreen(
    viewModel: VmViewModel,
    vm: VirtualMachine,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val activeEngine by viewModel.activeEngine.collectAsState()
    val snapshots by viewModel.currentSnapshots.collectAsState()

    var isFullscreen by remember { mutableStateOf(false) }
    var showTrackpad by remember { mutableStateOf(false) }
    var showKeyboardBar by remember { mutableStateOf(true) }
    var showSnapshotDialog by remember { mutableStateOf(false) }
    var showShutdownDialog by remember { mutableStateOf(false) }

    // Start VM engine if not started
    LaunchedEffect(vm.id) {
        if (activeEngine == null || activeEngine?.vm?.id != vm.id) {
            viewModel.startVirtualMachine(vm)
        }
    }

    val engine = activeEngine
    val status = engine?.status?.collectAsState()?.value ?: VmStatus.BOOTING
    val telemetry = engine?.telemetry?.collectAsState()?.value ?: com.example.engine.VmTelemetry()

    Scaffold(
        topBar = {
            if (!isFullscreen) {
                CenterAlignedTopAppBar(
                    title = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = vm.name,
                                fontWeight = FontWeight.Bold,
                                fontSize = 16.sp,
                                color = TextPrimary
                            )
                            Text(
                                text = "${vm.osPreset.displayName} • ${telemetry.cpuUsagePercent}% CPU • ${telemetry.ramUsedMb}/${vm.ramMb} MB RAM • ${telemetry.fps} FPS",
                                fontSize = 11.sp,
                                color = CyberCyan,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = onBackClick) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back to Library",
                                tint = CyberCyan
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showShutdownDialog = true }) {
                            Icon(Icons.Default.PowerSettingsNew, contentDescription = "Shutdown VM", tint = TerminalRed)
                        }
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = DarkSurface
                    )
                )
            }
        },
        containerColor = Color.Black,
        modifier = modifier
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(if (isFullscreen) androidx.compose.foundation.layout.PaddingValues(0.dp) else innerPadding)
        ) {
            // Top Telemetry Bar when fullscreen
            if (isFullscreen) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(CyberNavySurface.copy(alpha = 0.85f))
                        .padding(horizontal = 12.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${vm.name} • ${telemetry.cpuUsagePercent}% CPU • ${telemetry.ramUsedMb}MB RAM • ${telemetry.fps} FPS • ${formatUptime(telemetry.uptimeSeconds)}",
                        fontSize = 11.sp,
                        color = CyberCyan,
                        fontFamily = FontFamily.Monospace
                    )
                    IconButton(
                        onClick = { isFullscreen = false },
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Exit Fullscreen", tint = TextPrimary)
                    }
                }
            }

            // Virtual Display Area (Hardware WebView)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(Color.Black)
                    .testTag("virtual_display_canvas"),
                contentAlignment = Alignment.Center
            ) {
                AndroidView(
                    factory = { ctx ->
                        WebView(ctx).apply {
                            layoutParams = ViewGroup.LayoutParams(
                                ViewGroup.LayoutParams.MATCH_PARENT,
                                ViewGroup.LayoutParams.MATCH_PARENT
                            )
                            try {
                                setLayerType(android.view.View.LAYER_TYPE_SOFTWARE, null)
                            } catch (e: Exception) {
                                // Fallback
                            }
                            setBackgroundColor(android.graphics.Color.BLACK)
                            engine?.attachWebView(this)
                            engine?.start()
                        }
                    },
                    update = { view ->
                        if (engine != null && engine.status.value == VmStatus.STOPPED) {
                            // idle
                        }
                    },
                    onRelease = { view ->
                        view.stopLoading()
                        view.loadUrl("about:blank")
                    },
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Optional Mouse Trackpad Overlay
            AnimatedVisibility(
                visible = showTrackpad,
                enter = slideInVertically(initialOffsetY = { it }),
                exit = slideOutVertically(targetOffsetY = { it })
            ) {
                TouchTrackpad(
                    onMouseDelta = { dx, dy -> engine?.sendMouseDelta(dx, dy) },
                    onMouseClick = { button -> engine?.sendMouseClick(button) }
                )
            }

            // Floating Controls Bar
            VmControlBar(
                status = status,
                telemetry = telemetry,
                isFullscreen = isFullscreen,
                showTrackpad = showTrackpad,
                onToggleFullscreen = { isFullscreen = !isFullscreen },
                onToggleTrackpad = { showTrackpad = !showTrackpad },
                onToggleAudio = { engine?.toggleAudio() },
                onPauseResume = {
                    if (status == VmStatus.PAUSED) engine?.resume() else engine?.pause()
                },
                onRestart = { engine?.restart() },
                onShutdown = { showShutdownDialog = true },
                onTakeSnapshot = { showSnapshotDialog = true }
            )

            Spacer(Modifier.height(4.dp))

            // Virtual Keyboard Bar
            if (showKeyboardBar) {
                VirtualKeyboardBar(
                    onSendKey = { key -> engine?.sendKey(key) },
                    onSendSpecialKey = { special -> engine?.sendSpecialKey(special) }
                )
            }
        }
    }

    // Snapshot Management Dialog
    if (showSnapshotDialog) {
        SnapshotDialog(
            snapshots = snapshots,
            onTakeSnapshot = { title -> viewModel.takeSnapshot(title) },
            onRestoreSnapshot = { snap -> viewModel.restoreSnapshot(snap) },
            onDeleteSnapshot = { snap -> viewModel.deleteSnapshot(snap) },
            onDismiss = { showSnapshotDialog = false }
        )
    }

    // Shutdown / Reset Confirmation Dialog
    if (showShutdownDialog) {
        AlertDialog(
            onDismissRequest = { showShutdownDialog = false },
            title = { Text("Virtual Machine Power Menu", color = TextPrimary, fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "Choose an ACPI power action for '${vm.name}'. Clean shutdown ensures all virtual disk write caches are flushed safely.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            viewModel.stopActiveVm()
                            showShutdownDialog = false
                            onBackClick()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalRed)
                    ) {
                        Text("ACPI Shutdown")
                    }

                    OutlinedButton(
                        onClick = {
                            viewModel.forceStopActiveVm()
                            showShutdownDialog = false
                            onBackClick()
                        },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = TerminalRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, TerminalRed)
                    ) {
                        Text("Force Kill")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { showShutdownDialog = false }) {
                    Text("Cancel", color = TextPrimary)
                }
            },
            containerColor = CyberNavyCard
        )
    }
}

@Composable
private fun SnapshotDialog(
    snapshots: List<VmSnapshot>,
    onTakeSnapshot: (String) -> Unit,
    onRestoreSnapshot: (VmSnapshot) -> Unit,
    onDeleteSnapshot: (VmSnapshot) -> Unit,
    onDismiss: () -> Unit
) {
    var snapshotTitle by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.CameraAlt, contentDescription = null, tint = TerminalAmber)
                Spacer(Modifier.width(8.dp))
                Text("VM Snapshots & State", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text("Save current CPU, RAM, and display state to restore anytime:", fontSize = 12.sp, color = TextSecondary)
                Spacer(Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = snapshotTitle,
                        onValueChange = { snapshotTitle = it },
                        placeholder = { Text("Snapshot name (optional)", fontSize = 12.sp, color = TextSecondary) },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CyberCyan,
                            unfocusedBorderColor = CyberNavyBorder,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary,
                            focusedContainerColor = DarkSurface,
                            unfocusedContainerColor = DarkSurface
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(6.dp))
                    Button(
                        onClick = {
                            onTakeSnapshot(snapshotTitle)
                            snapshotTitle = ""
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = TerminalAmber, contentColor = Color.Black)
                    ) {
                        Text("Save")
                    }
                }

                Spacer(Modifier.height(14.dp))
                Text("Saved Snapshots (${snapshots.size}):", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = TextPrimary)
                Spacer(Modifier.height(6.dp))

                if (snapshots.isEmpty()) {
                    Text("No snapshots saved yet.", fontSize = 11.sp, color = TextSecondary)
                } else {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        snapshots.forEach { snap ->
                            Card(
                                colors = CardDefaults.cardColors(containerColor = CyberNavySurface),
                                border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(8.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column {
                                        Text(snap.title, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = TextPrimary)
                                        Text(
                                            java.text.SimpleDateFormat("MMM dd, HH:mm:ss", java.util.Locale.getDefault()).format(java.util.Date(snap.timestamp)),
                                            fontSize = 10.sp,
                                            color = CyberCyan
                                        )
                                    }

                                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                        IconButton(
                                            onClick = { onRestoreSnapshot(snap); onDismiss() },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Restore, contentDescription = "Restore", tint = TerminalGreen, modifier = Modifier.size(18.dp))
                                        }
                                        IconButton(
                                            onClick = { onDeleteSnapshot(snap) },
                                            modifier = Modifier.size(32.dp)
                                        ) {
                                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TerminalRed, modifier = Modifier.size(18.dp))
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = CyberCyan)
            }
        },
        containerColor = CyberNavyCard
    )
}

private fun formatUptime(seconds: Long): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", mins, secs)
}
