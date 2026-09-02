package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Mouse
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.VmStatus
import com.example.engine.MouseInputMode
import com.example.engine.VmTelemetry
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VmControlBar(
    status: VmStatus,
    telemetry: VmTelemetry,
    isFullscreen: Boolean,
    showTrackpad: Boolean,
    onToggleFullscreen: () -> Unit,
    onToggleTrackpad: () -> Unit,
    onToggleAudio: () -> Unit,
    onPauseResume: () -> Unit,
    onRestart: () -> Unit,
    onShutdown: () -> Unit,
    onTakeSnapshot: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberNavySurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .padding(horizontal = 8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // Status Indicator Badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .background(
                        color = when (status) {
                            VmStatus.RUNNING -> TerminalGreen.copy(alpha = 0.15f)
                            VmStatus.PAUSED -> TerminalAmber.copy(alpha = 0.15f)
                            VmStatus.BOOTING -> CyberCyan.copy(alpha = 0.15f)
                            else -> TerminalRed.copy(alpha = 0.15f)
                        },
                        shape = RoundedCornerShape(4.dp)
                    )
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = when (status) {
                        VmStatus.RUNNING -> "● RUNNING"
                        VmStatus.PAUSED -> "❚❚ PAUSED"
                        VmStatus.BOOTING -> "⏳ BOOTING"
                        else -> "■ STOPPED"
                    },
                    fontSize = 11.sp,
                    color = when (status) {
                        VmStatus.RUNNING -> TerminalGreen
                        VmStatus.PAUSED -> TerminalAmber
                        VmStatus.BOOTING -> CyberCyan
                        else -> TerminalRed
                    }
                )
            }

            Spacer(Modifier.width(4.dp))

            // Pause / Resume Button
            IconButton(
                onClick = onPauseResume,
                enabled = status == VmStatus.RUNNING || status == VmStatus.PAUSED
            ) {
                Icon(
                    imageVector = if (status == VmStatus.PAUSED) Icons.Default.PlayArrow else Icons.Default.Pause,
                    contentDescription = if (status == VmStatus.PAUSED) "Resume VM" else "Pause VM",
                    tint = if (status == VmStatus.PAUSED) TerminalGreen else TerminalAmber
                )
            }

            // Restart Button
            IconButton(
                onClick = onRestart,
                enabled = status == VmStatus.RUNNING || status == VmStatus.PAUSED
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Restart VM",
                    tint = CyberCyan
                )
            }

            // Shutdown / Force Stop Button
            IconButton(
                onClick = onShutdown,
                enabled = status != VmStatus.STOPPED
            ) {
                Icon(
                    imageVector = Icons.Default.PowerSettingsNew,
                    contentDescription = "Shutdown VM",
                    tint = TerminalRed
                )
            }

            // Snapshot Button
            IconButton(onClick = onTakeSnapshot) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = "Take Snapshot",
                    tint = TerminalAmber
                )
            }

            // Mouse Trackpad Toggle
            IconButton(onClick = onToggleTrackpad) {
                Icon(
                    imageVector = Icons.Default.Mouse,
                    contentDescription = "Toggle Trackpad",
                    tint = if (showTrackpad) CyberCyan else TextSecondary
                )
            }

            // Audio Toggle
            IconButton(onClick = onToggleAudio) {
                Icon(
                    imageVector = if (telemetry.isAudioMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                    contentDescription = "Toggle Audio",
                    tint = if (telemetry.isAudioMuted) TextSecondary else CyberCyan
                )
            }

            // Fullscreen Toggle
            IconButton(onClick = onToggleFullscreen) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Toggle Fullscreen",
                    tint = TextPrimary
                )
            }
        }
    }
}
