package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Keyboard
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.TerminalAmber
import com.example.ui.theme.TerminalGreen
import com.example.ui.theme.TerminalRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun VirtualKeyboardBar(
    onSendKey: (String) -> Unit,
    onSendSpecialKey: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }
    var textInput by remember { mutableStateOf("") }
    var activeModifierCtrl by remember { mutableStateOf(false) }
    var activeModifierAlt by remember { mutableStateOf(false) }

    Surface(
        color = CyberNavySurface.copy(alpha = 0.95f),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder),
        modifier = modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp)
        ) {
            // Top Quick Keys Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Quick Toggle Full Keyboard
                OutlinedButton(
                    onClick = { isExpanded = !isExpanded },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isExpanded) CyberCyan.copy(alpha = 0.2f) else CyberNavyCard,
                        contentColor = if (isExpanded) CyberCyan else TextPrimary
                    ),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (isExpanded) CyberCyan else CyberNavyBorder),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(36.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.Close else Icons.Default.Keyboard,
                        contentDescription = "Toggle Keyboard",
                        modifier = Modifier.height(16.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(if (isExpanded) "Hide" else "Keyboard", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                }

                SpecialKeyButton("ESC", color = TerminalRed) { onSendSpecialKey("Escape"); onSendKey("Escape") }
                SpecialKeyButton("TAB", color = CyberCyan) { onSendSpecialKey("Tab"); onSendKey("Tab") }
                SpecialKeyButton(
                    "CTRL",
                    isActive = activeModifierCtrl,
                    color = if (activeModifierCtrl) TerminalAmber else CyberCyan
                ) {
                    activeModifierCtrl = !activeModifierCtrl
                    onSendSpecialKey("CTRL")
                }
                SpecialKeyButton(
                    "ALT",
                    isActive = activeModifierAlt,
                    color = if (activeModifierAlt) TerminalAmber else CyberCyan
                ) {
                    activeModifierAlt = !activeModifierAlt
                    onSendSpecialKey("ALT")
                }
                SpecialKeyButton("C-A-DEL", color = TerminalRed) { onSendSpecialKey("CTRL_ALT_DEL") }
                SpecialKeyButton("GUI/DESKTOP", color = TerminalGreen) { onSendSpecialKey("GUI_TOGGLE") }
                SpecialKeyButton("↑") { onSendSpecialKey("ArrowUp") }
                SpecialKeyButton("↓") { onSendSpecialKey("ArrowDown") }
                SpecialKeyButton("←") { onSendSpecialKey("ArrowLeft") }
                SpecialKeyButton("→") { onSendSpecialKey("ArrowRight") }
                SpecialKeyButton("ENTER", color = TerminalGreen) { onSendKey("Enter") }
            }

            // Expanded Soft Keyboard & Direct String Sender
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    // String Input Line for Fast Command Typing
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = textInput,
                            onValueChange = { textInput = it },
                            placeholder = { Text("Type command (e.g. ls, top, reboot, gui)", fontSize = 12.sp, color = TextSecondary) },
                            singleLine = true,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = CyberCyan,
                                unfocusedBorderColor = CyberNavyBorder,
                                focusedTextColor = TextPrimary,
                                unfocusedTextColor = TextPrimary,
                                focusedContainerColor = DarkSurface,
                                unfocusedContainerColor = DarkSurface
                            ),
                            textStyle = androidx.compose.ui.text.TextStyle(
                                fontFamily = FontFamily.Monospace,
                                fontSize = 13.sp
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                        )
                        Spacer(Modifier.width(6.dp))
                        IconButton(
                            onClick = {
                                if (textInput.isNotEmpty()) {
                                    for (ch in textInput) {
                                        onSendKey(ch.toString())
                                    }
                                    onSendKey("Enter")
                                    textInput = ""
                                }
                            },
                            modifier = Modifier
                                .background(CyberCyan, RoundedCornerShape(8.dp))
                                .height(48.dp)
                                .width(48.dp)
                        ) {
                            Icon(Icons.Default.Send, contentDescription = "Send Command", tint = Color.Black)
                        }
                    }

                    Spacer(Modifier.height(8.dp))

                    // Function Keys Row
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (i in 1..12) {
                            SpecialKeyButton("F$i", color = TextSecondary) { onSendSpecialKey("F$i") }
                        }
                        SpecialKeyButton("DEL", color = TerminalRed) { onSendKey("Backspace") }
                        SpecialKeyButton("HOME") { onSendSpecialKey("Home") }
                        SpecialKeyButton("END") { onSendSpecialKey("End") }
                        SpecialKeyButton("PGUP") { onSendSpecialKey("PageUp") }
                        SpecialKeyButton("PGDN") { onSendSpecialKey("PageDown") }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialKeyButton(
    label: String,
    isActive: Boolean = false,
    color: Color = TextPrimary,
    onClick: () -> Unit
) {
    OutlinedButton(
        onClick = onClick,
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = if (isActive) color.copy(alpha = 0.25f) else CyberNavyCard,
            contentColor = color
        ),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) color else CyberNavyBorder),
        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
        modifier = Modifier.height(34.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            fontFamily = FontFamily.Monospace
        )
    }
}
