package com.example.diwali.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.diwali.ui.theme.FestiveOrange
import com.example.diwali.ui.theme.FestiveRed
import com.example.diwali.ui.theme.GoldDark
import com.example.diwali.ui.theme.GoldLight
import com.example.diwali.ui.theme.GoldYellow

@Composable
fun ExitConfirmDialog(
    onDismiss: () -> Unit,
    onConfirmExit: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = true, dismissOnClickOutside = true)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .clip(RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF1E1438),
                            Color(0xFF0F0B1E)
                        )
                    )
                )
                .border(
                    width = 2.dp,
                    brush = Brush.linearGradient(
                        colors = listOf(GoldYellow, FestiveOrange, GoldDark)
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Diya Vector Graphic
                androidx.compose.foundation.Canvas(modifier = Modifier.size(width = 54.dp, height = 40.dp)) {
                    val w = size.width
                    val h = size.height
                    // Clay lamp base
                    val bowl = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.12f, h * 0.58f)
                        cubicTo(w * 0.12f, h * 0.95f, w * 0.88f, h * 0.95f, w * 0.88f, h * 0.58f)
                        close()
                    }
                    drawPath(bowl, brush = Brush.verticalGradient(listOf(Color(0xFFE65100), Color(0xFFBF360C))))
                    drawOval(
                        brush = Brush.horizontalGradient(listOf(GoldYellow, GoldLight, GoldYellow)),
                        topLeft = Offset(w * 0.10f, h * 0.50f),
                        size = androidx.compose.ui.geometry.Size(w * 0.80f, h * 0.18f)
                    )
                    // Flame
                    val flame = androidx.compose.ui.graphics.Path().apply {
                        moveTo(w * 0.5f, h * 0.08f)
                        cubicTo(w * 0.70f, h * 0.30f, w * 0.65f, h * 0.54f, w * 0.5f, h * 0.54f)
                        cubicTo(w * 0.35f, h * 0.54f, w * 0.30f, h * 0.30f, w * 0.5f, h * 0.08f)
                        close()
                    }
                    drawPath(flame, brush = Brush.verticalGradient(listOf(Color.White, GoldYellow, FestiveOrange)))
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Exit Dhamaka?",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Are you sure you want to leave the celebration?",
                    fontSize = 14.sp,
                    color = GoldLight.copy(alpha = 0.85f),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Actions: Keep Playing vs Exit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Keep Playing (Primary action)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(Color(0xFF00C853), Color(0xFF64DD17))
                                )
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onDismiss
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Keep Playing",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }

                    // Exit (Secondary action)
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(
                                brush = Brush.horizontalGradient(
                                    colors = listOf(FestiveRed, FestiveOrange)
                                )
                            )
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = onConfirmExit
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Exit Game",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                    }
                }
            }
        }
    }
}
