package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.PipelineStage
import com.example.ui.theme.CivicBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusRepaired

@Composable
fun PipelineStrip(
    currentStage: PipelineStage,
    modifier: Modifier = Modifier,
    onStageClick: ((PipelineStage) -> Unit)? = null
) {
    val stages = PipelineStage.values()
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("pipeline_strip_container"),
        shape = RoundedCornerShape(16.dp),
        color = Slate900,
        tonalElevation = 4.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 12.dp)
        ) {
            // Header label
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(CivicBlue)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "INCIDENT PIPELINE",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Slate400,
                        letterSpacing = 1.2.sp
                    )
                }

                Text(
                    text = "Stage ${currentStage.stepNumber} of 5: ${currentStage.title}",
                    style = MaterialTheme.typography.labelSmall,
                    color = CivicBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 5 nodes with connecting lines
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                stages.forEachIndexed { index, stage ->
                    val isPassed = stage.stepNumber < currentStage.stepNumber
                    val isCurrent = stage == currentStage
                    val isFuture = stage.stepNumber > currentStage.stepNumber

                    // Node
                    StageNode(
                        stage = stage,
                        isPassed = isPassed,
                        isCurrent = isCurrent,
                        pulseScale = if (isCurrent) pulseScale else 1f,
                        onClick = { onStageClick?.invoke(stage) }
                    )

                    // Connecting Line (except after last node)
                    if (index < stages.size - 1) {
                        val linePassed = stage.stepNumber < currentStage.stepNumber
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(3.dp)
                                .padding(horizontal = 4.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(
                                    if (linePassed) {
                                        Brush.horizontalGradient(
                                            listOf(StatusRepaired, CivicBlue)
                                        )
                                    } else {
                                        Brush.horizontalGradient(
                                            listOf(Slate700, Slate800)
                                        )
                                    }
                                )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Subtitle of active step
            Text(
                text = currentStage.description,
                style = MaterialTheme.typography.bodySmall,
                color = Slate200,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun StageNode(
    stage: PipelineStage,
    isPassed: Boolean,
    isCurrent: Boolean,
    pulseScale: Float,
    onClick: () -> Unit
) {
    val nodeBgColor by animateColorAsState(
        targetValue = when {
            isPassed -> StatusRepaired
            isCurrent -> CivicBlue
            else -> Slate800
        },
        label = "nodeBgColor"
    )

    val icon: ImageVector = when (stage) {
        PipelineStage.CAPTURE -> Icons.Default.CameraAlt
        PipelineStage.INGEST -> Icons.Default.CloudDone
        PipelineStage.DETECT -> Icons.Default.Psychology
        PipelineStage.DISPATCH -> Icons.Default.Engineering
        PipelineStage.VERIFY -> Icons.Default.Verified
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable(onClick = onClick)
            .testTag("pipeline_node_${stage.name.lowercase()}")
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(36.dp)
                .scale(pulseScale)
                .clip(CircleShape)
                .background(nodeBgColor)
                .border(
                    width = if (isCurrent) 2.dp else 1.dp,
                    color = if (isCurrent) Color.White else if (isPassed) StatusRepaired else Slate700,
                    shape = CircleShape
                )
        ) {
            if (isPassed) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "${stage.title} completed",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = stage.title,
                    tint = if (isCurrent) Color.White else Slate400,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = stage.title,
            style = MaterialTheme.typography.labelSmall,
            fontSize = 10.sp,
            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
            color = if (isCurrent) Color.White else if (isPassed) StatusRepaired else Slate400,
            textAlign = TextAlign.Center
        )
    }
}
