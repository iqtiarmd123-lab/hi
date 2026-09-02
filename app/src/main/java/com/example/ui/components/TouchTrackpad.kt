package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CyberCyan
import com.example.ui.theme.CyberNavyBorder
import com.example.ui.theme.CyberNavyCard
import com.example.ui.theme.CyberNavySurface
import com.example.ui.theme.DarkCardBorder
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun TouchTrackpad(
    onMouseDelta: (Float, Float) -> Unit,
    onMouseClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = CyberNavySurface.copy(alpha = 0.9f),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, CyberNavyBorder),
        modifier = modifier
            .fillMaxWidth()
            .height(130.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(6.dp)
        ) {
            // Touchpad Surface
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(CyberNavyCard, RoundedCornerShape(8.dp))
                    .pointerInput(Unit) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            onMouseDelta(dragAmount.x * 2.2f, dragAmount.y * 2.2f)
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { onMouseClick("left") },
                            onDoubleTap = {
                                onMouseClick("left")
                                onMouseClick("left")
                            },
                            onLongPress = { onMouseClick("right") }
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "🖱️ Virtual Touchpad (Swipe to move cursor • Tap to Click)",
                    fontSize = 11.sp,
                    color = TextSecondary.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.height(6.dp))

            // Left and Right Physical Click Buttons
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = { onMouseClick("left") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberNavyCard,
                        contentColor = CyberCyan
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("LEFT CLICK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = { onMouseClick("right") },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = CyberNavyCard,
                        contentColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, DarkCardBorder),
                    modifier = Modifier.weight(1f)
                ) {
                    Text("RIGHT CLICK", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
