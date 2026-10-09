package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PipelineStage
import com.example.model.Severity
import com.example.ui.theme.CivicBlue
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityCriticalBg
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityHighBg
import com.example.ui.theme.SeverityLow
import com.example.ui.theme.SeverityLowBg
import com.example.ui.theme.SeverityMedium
import com.example.ui.theme.SeverityMediumBg
import com.example.ui.theme.StatusDispatched
import com.example.ui.theme.StatusRepaired

@Composable
fun SeverityBadge(
    severity: Severity,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor, dotColor) = when (severity) {
        Severity.CRITICAL -> Triple(SeverityCriticalBg, SeverityCritical, SeverityCritical)
        Severity.HIGH -> Triple(SeverityHighBg, SeverityHigh, SeverityHigh)
        Severity.MEDIUM -> Triple(SeverityMediumBg, SeverityMedium, SeverityMedium)
        Severity.LOW -> Triple(SeverityLowBg, SeverityLow, SeverityLow)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .border(1.dp, dotColor.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("severity_badge_${severity.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(6.dp)
                    .clip(CircleShape)
                    .background(dotColor)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = severity.label.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                fontSize = 10.sp,
                color = textColor,
                letterSpacing = 0.5.sp
            )
        }
    }
}

@Composable
fun StageStatusChip(
    stage: PipelineStage,
    modifier: Modifier = Modifier
) {
    val (bgColor, textColor) = when (stage) {
        PipelineStage.CAPTURE -> Pair(Color(0xFFF1F5F9), Color(0xFF475569))
        PipelineStage.INGEST -> Pair(Color(0xFFE0E7FF), Color(0xFF4338CA))
        PipelineStage.DETECT -> Pair(Color(0xFFFEF3C7), Color(0xFFB45309))
        PipelineStage.DISPATCH -> Pair(Color(0xFFE0F2FE), StatusDispatched)
        PipelineStage.VERIFY -> Pair(Color(0xFFDCFCE7), StatusRepaired)
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(bgColor)
            .padding(horizontal = 8.dp, vertical = 3.dp)
            .testTag("stage_chip_${stage.name.lowercase()}"),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stage.title,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.SemiBold,
            fontSize = 11.sp,
            color = textColor
        )
    }
}
