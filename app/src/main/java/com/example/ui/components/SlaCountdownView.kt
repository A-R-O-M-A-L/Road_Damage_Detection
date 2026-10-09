package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityCriticalBg
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityHighBg
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.StatusRepaired
import kotlinx.coroutines.delay

@Composable
fun SlaCountdownView(
    deadlineEpochMs: Long,
    isRepaired: Boolean,
    modifier: Modifier = Modifier,
    compact: Boolean = false
) {
    if (isRepaired) {
        Box(
            modifier = modifier
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFDCFCE7))
                .padding(horizontal = 8.dp, vertical = 3.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "RESOLVED",
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = StatusRepaired,
                fontSize = if (compact) 10.sp else 11.sp
            )
        }
        return
    }

    var currentTimeMs by remember { mutableLongStateOf(System.currentTimeMillis()) }

    LaunchedEffect(deadlineEpochMs) {
        while (true) {
            currentTimeMs = System.currentTimeMillis()
            delay(1000L)
        }
    }

    val diffMs = deadlineEpochMs - currentTimeMs
    val isOverdue = diffMs < 0
    val absDiff = kotlin.math.abs(diffMs)

    val hours = absDiff / (1000 * 3600)
    val minutes = (absDiff % (1000 * 3600)) / (1000 * 60)
    val seconds = (absDiff % (1000 * 60)) / 1000

    val timeString = if (isOverdue) {
        if (hours > 0) "+${hours}h ${minutes}m" else "+${minutes}m ${seconds}s"
    } else {
        if (hours > 24) {
            val days = hours / 24
            val remHours = hours % 24
            "${days}d ${remHours}h"
        } else {
            String.format("%02dh %02dm %02ds", hours, minutes, seconds)
        }
    }

    val (bgColor, textColor, borderColor) = when {
        isOverdue -> Triple(SeverityCriticalBg, SeverityCritical, SeverityCritical)
        diffMs < 2 * 3600 * 1000L -> Triple(SeverityHighBg, SeverityHigh, SeverityHigh)
        else -> Triple(Color(0xFFF1F5F9), Color(0xFF334155), Color(0xFFCBD5E1))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, borderColor.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag("sla_countdown_${if (isOverdue) "overdue" else "active"}"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (isOverdue) Icons.Default.WarningAmber else Icons.Default.HourglassBottom,
                contentDescription = if (isOverdue) "SLA Overdue" else "SLA Deadline",
                tint = textColor,
                modifier = Modifier.size(12.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = if (isOverdue) "OVERDUE $timeString" else timeString,
                style = MaterialTheme.typography.labelSmall,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold,
                fontSize = if (compact) 10.sp else 11.sp,
                color = textColor
            )
        }
    }
}
