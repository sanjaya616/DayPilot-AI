package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkNavyCard
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.PastelLavender
import com.example.ui.theme.SunsetAmber
import com.example.ui.theme.TextMuted
import com.example.ui.theme.TextPrimary

@Composable
fun ArcProgressGauge(
    progressPercent: Float, // 0..100
    score: Int,
    label: String = "Progress Score",
    modifier: Modifier = Modifier
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progressPercent.coerceIn(0f, 100f) / 100f,
        animationSpec = tween(durationMillis = 900),
        label = "ArcProgressAnimation"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkNavyCard, shape = RoundedCornerShape(28.dp))
            .padding(top = 20.dp, bottom = 16.dp, start = 20.dp, end = 20.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(width = 240.dp, height = 140.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .size(width = 240.dp, height = 140.dp)
                ) {
                    val strokeWidth = 22.dp.toPx()
                    val arcWidth = size.width - strokeWidth
                    val arcHeight = (size.height * 2) - strokeWidth
                    val topLeft = Offset(strokeWidth / 2, strokeWidth / 2)
                    val arcSize = Size(arcWidth, arcHeight)

                    // Background track arc (180 degrees from 180 to 0)
                    drawArc(
                        color = Color(0xFF26324D),
                        startAngle = 180f,
                        sweepAngle = 180f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Foreground active arc with gradient
                    val sweep = 180f * animatedProgress
                    if (sweep > 0f) {
                        drawArc(
                            brush = Brush.horizontalGradient(
                                colors = listOf(ElectricCyan, SunsetAmber)
                            ),
                            startAngle = 180f,
                            sweepAngle = sweep,
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                        )
                    }
                }

                // Center score & label
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(top = 28.dp)
                ) {
                    Text(
                        text = "$score",
                        color = TextPrimary,
                        fontSize = 38.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = label,
                        color = TextMuted,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
