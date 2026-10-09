package com.example.ui.citizen

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.example.model.Complaint
import com.example.ui.components.CitizenPinnedGpsMapView
import com.example.ui.components.DamageDetectionOverlay
import com.example.ui.components.PipelineStrip
import com.example.ui.components.SeverityBadge
import com.example.ui.components.SlaCountdownView
import com.example.ui.theme.CivicBlue
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusRepaired

@Composable
fun ComplaintDetailDialog(
    complaint: Complaint,
    onDismiss: () -> Unit
) {
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .testTag("complaint_detail_dialog"),
            color = Color.White
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(20.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CivicBlue)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = complaint.id,
                                color = Color.White,
                                fontFamily = FontFamily.Monospace,
                                fontWeight = FontWeight.Bold,
                                fontSize = 13.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        SeverityBadge(severity = complaint.severity)
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.testTag("button_close_detail_dialog")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Slate700)
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text = complaint.title,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = CivicBlue, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "${complaint.location} (${complaint.district})",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Slate600
                        )
                    }

                    // GPS Coordinates Badge
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Pinned GPS: ${complaint.formattedCoordinates()}",
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            color = Slate800
                        )
                    }

                    // Google Map View Pinned Location
                    CitizenPinnedGpsMapView(
                        latitude = complaint.latitude,
                        longitude = complaint.longitude,
                        accuracyMeters = complaint.accuracyMeters ?: 4.0f,
                        formattedAddress = complaint.location,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // 5-step Pipeline Strip
                    PipelineStrip(currentStage = complaint.stage)

                    // Photo with AI Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(Slate900)
                    ) {
                        if (complaint.sampleDrawableId != null) {
                            Image(
                                painter = painterResource(id = complaint.sampleDrawableId),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else if (complaint.photoUri != null) {
                            AsyncImage(
                                model = complaint.photoUri,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        if (complaint.aiAnalysis != null) {
                            DamageDetectionOverlay(
                                aiAnalysis = complaint.aiAnalysis,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    // SLA and Status card
                    Card(
                        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8FAFC)),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text("Repair SLA Deadline", fontSize = 11.sp, color = Slate400, fontWeight = FontWeight.SemiBold)
                                SlaCountdownView(
                                    deadlineEpochMs = complaint.deadlineEpochMs,
                                    isRepaired = complaint.isRepaired
                                )
                            }
                            if (complaint.isRepaired) {
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(StatusRepaired)
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text("COMPLETED & VERIFIED", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }
                    }

                    // Assigned Engineer
                    if (complaint.assignedEngineer != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF0FDF4)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Engineering, contentDescription = null, tint = StatusRepaired, modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Assigned Municipal Crew", fontWeight = FontWeight.Bold, color = Slate900, fontSize = 13.sp)
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(complaint.assignedEngineer.name, fontWeight = FontWeight.Bold, color = Slate800, fontSize = 14.sp)
                                Text(complaint.assignedEngineer.role, fontSize = 12.sp, color = Slate600)
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Phone, contentDescription = null, tint = Slate400, modifier = Modifier.size(12.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(complaint.assignedEngineer.phone, fontSize = 11.sp, color = Slate600)
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Text("Vehicle: ${complaint.assignedEngineer.vehicleNumber}", fontSize = 11.sp, color = Slate600)
                                }
                            }
                        }
                    }

                    // Repair Notes (if completed)
                    if (complaint.repairNotes != null) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Column(modifier = Modifier.padding(14.dp)) {
                                Text("Closure / Verification Note:", fontWeight = FontWeight.Bold, color = CivicBlue, fontSize = 12.sp)
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(complaint.repairNotes, fontSize = 13.sp, color = Slate800)
                            }
                        }
                    }

                    // Citizen Note
                    if (complaint.note.isNotBlank()) {
                        Column {
                            Text("Citizen Observation Note:", fontWeight = FontWeight.SemiBold, fontSize = 12.sp, color = Slate600)
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(complaint.note, fontSize = 13.sp, color = Slate900)
                        }
                    }

                    // Audit Timeline
                    Text("Live Pipeline Audit Trail (${complaint.timeline.size} events)", fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Slate900)
                    complaint.timeline.forEach { event ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(10.dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            Box(
                                modifier = Modifier
                                    .padding(top = 3.dp)
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(CivicBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(event.title, fontWeight = FontWeight.Bold, fontSize = 12.sp, color = Slate900)
                                    Text(event.actor, fontSize = 10.sp, color = Slate400)
                                }
                                Text(event.description, fontSize = 11.sp, color = Slate600)
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
                }
            }
        }
    }
}
