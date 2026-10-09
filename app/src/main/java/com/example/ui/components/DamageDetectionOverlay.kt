package com.example.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.AiAnalysis
import com.example.model.Severity
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityLow
import com.example.ui.theme.SeverityMedium

@Composable
fun DamageDetectionOverlay(
    aiAnalysis: AiAnalysis,
    modifier: Modifier = Modifier
) {
    val bbox = aiAnalysis.boundingBox ?: return

    val infiniteTransition = rememberInfiniteTransition(label = "pulseLaser")
    val alphaAnim by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    val boxColor = when (aiAnalysis.severity) {
        Severity.CRITICAL -> SeverityCritical
        Severity.HIGH -> SeverityHigh
        Severity.MEDIUM -> SeverityMedium
        Severity.LOW -> SeverityLow
    }

    Box(modifier = modifier.fillMaxSize()) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val leftPx = bbox.left * size.width
            val topPx = bbox.top * size.height
            val widthPx = (bbox.right - bbox.left) * size.width
            val heightPx = (bbox.bottom - bbox.top) * size.height

            // Corner reticle accents
            val cornerLen = 24.dp.toPx()
            val strokeW = 3.dp.toPx()

            // Main bounding rect (semi-translucent with dashed stroke)
            drawRect(
                color = boxColor.copy(alpha = 0.15f),
                topLeft = Offset(leftPx, topPx),
                size = Size(widthPx, heightPx)
            )

            drawRect(
                color = boxColor.copy(alpha = alphaAnim),
                topLeft = Offset(leftPx, topPx),
                size = Size(widthPx, heightPx),
                style = Stroke(
                    width = strokeW,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 10f), 0f)
                )
            )

            // Top-left bracket
            drawLine(boxColor, Offset(leftPx, topPx), Offset(leftPx + cornerLen, topPx), strokeW * 1.5f)
            drawLine(boxColor, Offset(leftPx, topPx), Offset(leftPx, topPx + cornerLen), strokeW * 1.5f)

            // Top-right bracket
            drawLine(boxColor, Offset(leftPx + widthPx, topPx), Offset(leftPx + widthPx - cornerLen, topPx), strokeW * 1.5f)
            drawLine(boxColor, Offset(leftPx + widthPx, topPx), Offset(leftPx + widthPx, topPx + cornerLen), strokeW * 1.5f)

            // Bottom-left bracket
            drawLine(boxColor, Offset(leftPx, topPx + heightPx), Offset(leftPx + cornerLen, topPx + heightPx), strokeW * 1.5f)
            drawLine(boxColor, Offset(leftPx, topPx + heightPx), Offset(leftPx, topPx + heightPx - cornerLen), strokeW * 1.5f)

            // Bottom-right bracket
            drawLine(boxColor, Offset(leftPx + widthPx, topPx + heightPx), Offset(leftPx + widthPx - cornerLen, topPx + heightPx), strokeW * 1.5f)
            drawLine(boxColor, Offset(leftPx + widthPx, topPx + heightPx), Offset(leftPx + widthPx, topPx + heightPx - cornerLen), strokeW * 1.5f)
        }

        // Floating Tag at top-left of box
        Box(
            modifier = Modifier
                .padding(start = 12.dp, top = 12.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(boxColor)
                .padding(horizontal = 6.dp, vertical = 2.dp)
        ) {
            val confPct = (aiAnalysis.confidence * 100).toInt()
            Text(
                text = "${aiAnalysis.damageType.take(28)} • ${confPct}% CONF",
                color = Color.White,
                fontSize = 10.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
