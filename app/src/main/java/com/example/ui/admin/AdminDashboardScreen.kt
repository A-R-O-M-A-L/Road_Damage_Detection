package com.example.ui.admin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AssignmentTurnedIn
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.ComplaintRepository
import com.example.model.Complaint
import com.example.model.EngineerCrew
import com.example.model.PipelineStage
import com.example.model.Severity
import com.example.ui.citizen.ComplaintDetailDialog
import com.example.ui.components.AdminGoogleMapHeatmapView
import com.example.ui.components.PipelineStrip
import com.example.ui.components.SeverityBadge
import com.example.ui.components.SlaCountdownView
import com.example.ui.components.StageStatusChip
import com.example.ui.theme.CivicBlue
import com.example.ui.theme.CivicBlueDark
import com.example.ui.theme.CivicNavy
import com.example.ui.theme.SeverityCritical
import com.example.ui.theme.SeverityHigh
import com.example.ui.theme.SeverityLow
import com.example.ui.theme.SeverityMedium
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.Slate950
import com.example.ui.theme.StatusDispatched
import com.example.ui.theme.StatusRepaired

@Composable
fun AdminDashboardScreen() {
    val complaints by ComplaintRepository.complaints.collectAsState()

    var severityFilter by remember { mutableStateOf<Severity?>(null) } // null = All
    var stageFilter by remember { mutableStateOf<String>("All") } // All, Active, Repaired
    var mapMode by remember { mutableStateOf("GoogleMaps") } // GoogleMaps or Canvas

    var selectedComplaintForInspect by remember { mutableStateOf<Complaint?>(null) }
    var complaintToAssignEngineer by remember { mutableStateOf<Complaint?>(null) }
    var complaintToMarkRepaired by remember { mutableStateOf<Complaint?>(null) }

    // Filter logic
    val filteredComplaints = complaints.filter { complaint ->
        val matchesSeverity = severityFilter == null || complaint.severity == severityFilter
        val matchesStage = when (stageFilter) {
            "Active" -> !complaint.isRepaired
            "Repaired" -> complaint.isRepaired
            else -> true
        }
        matchesSeverity && matchesStage
    }

    // Counts calculation
    val totalCount = complaints.size
    val dispatchedCount = complaints.count { it.stage == PipelineStage.DISPATCH && !it.isRepaired }
    val pendingTriageCount = complaints.count { it.stage == PipelineStage.CAPTURE || it.stage == PipelineStage.INGEST || it.stage == PipelineStage.DETECT }
    val repairedCount = complaints.count { it.isRepaired || it.stage == PipelineStage.VERIFY }
    val nowMs = System.currentTimeMillis()
    val overdueCount = complaints.count { !it.isRepaired && it.deadlineEpochMs < nowMs }

    // Severity breakdown counts
    val criticalCount = complaints.count { it.severity == Severity.CRITICAL }
    val highCount = complaints.count { it.severity == Severity.HIGH }
    val mediumCount = complaints.count { it.severity == Severity.MEDIUM }
    val lowCount = complaints.count { it.severity == Severity.LOW }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF1F5F9))
            .testTag("admin_dashboard_scroll"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Dashboard Top Header & Operations Banner
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(20.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_header_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(StatusRepaired)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "MUNICIPAL DISPATCH OPS",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate400,
                                letterSpacing = 1.2.sp
                            )
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(Slate800)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Live Fleet Sync",
                                color = Color(0xFF38BDF8),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "City Roadway Infrastructure Hub",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Real-time AI triage, rapid dispatch allocation, and repair SLA enforcement.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400
                    )
                }
            }
        }

        // 2. Summary Counts Cards
        item {
            Text(
                text = "Operational Metrics",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSummaryCard(
                    title = "Total Complaints",
                    count = totalCount,
                    accentColor = CivicBlue,
                    modifier = Modifier.weight(1f),
                    tag = "metric_total"
                )
                MetricSummaryCard(
                    title = "In Dispatch",
                    count = dispatchedCount,
                    accentColor = StatusDispatched,
                    modifier = Modifier.weight(1f),
                    tag = "metric_dispatched"
                )
                MetricSummaryCard(
                    title = "Pending Triage",
                    count = pendingTriageCount,
                    accentColor = SeverityMedium,
                    modifier = Modifier.weight(1f),
                    tag = "metric_pending"
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                MetricSummaryCard(
                    title = "Repaired / Closed",
                    count = repairedCount,
                    accentColor = StatusRepaired,
                    modifier = Modifier.weight(1f),
                    tag = "metric_repaired"
                )
                MetricSummaryCard(
                    title = "SLA Alerts",
                    count = overdueCount,
                    accentColor = SeverityCritical,
                    isAlert = overdueCount > 0,
                    modifier = Modifier.weight(1f),
                    tag = "metric_overdue"
                )
            }
        }

        // 3. Severity Breakdown Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth().testTag("severity_breakdown_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Severity Distribution",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )
                        Text(
                            text = "$totalCount Active Tickets",
                            fontSize = 11.sp,
                            color = Slate600
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Multi-colored distribution bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(14.dp)
                            .clip(RoundedCornerShape(7.dp))
                            .background(Slate200)
                    ) {
                        if (totalCount > 0) {
                            if (criticalCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(criticalCount.toFloat())
                                        .fillMaxSize()
                                        .background(SeverityCritical)
                                )
                            }
                            if (highCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(highCount.toFloat())
                                        .fillMaxSize()
                                        .background(SeverityHigh)
                                )
                            }
                            if (mediumCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(mediumCount.toFloat())
                                        .fillMaxSize()
                                        .background(SeverityMedium)
                                )
                            }
                            if (lowCount > 0) {
                                Box(
                                    modifier = Modifier
                                        .weight(lowCount.toFloat())
                                        .fillMaxSize()
                                        .background(SeverityLow)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Severity breakdown badges with counts
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        SeverityStatItem("Critical", criticalCount, SeverityCritical)
                        SeverityStatItem("High", highCount, SeverityHigh)
                        SeverityStatItem("Medium", mediumCount, SeverityMedium)
                        SeverityStatItem("Low", lowCount, SeverityLow)
                    }
                }
            }
        }

        // 4. Interactive Heatmap / City District Map Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Slate900),
                shape = RoundedCornerShape(18.dp),
                elevation = CardDefaults.cardElevation(4.dp),
                modifier = Modifier.fillMaxWidth().testTag("admin_map_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Map, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "City District GIS Heatmap",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (mapMode == "GoogleMaps") CivicBlue else Slate800)
                                    .clickable { mapMode = "GoogleMaps" }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("toggle_map_google")
                            ) {
                                Text(
                                    text = "Google Maps",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (mapMode == "Canvas") CivicBlue else Slate800)
                                    .clickable { mapMode = "Canvas" }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("toggle_map_canvas")
                            ) {
                                Text(
                                    text = "Heatmap",
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Text(
                        text = "Real-time geolocated defect coordinates. Tap any marker to inspect & dispatch.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate400,
                        modifier = Modifier.padding(top = 2.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    if (mapMode == "GoogleMaps") {
                        AdminGoogleMapHeatmapView(
                            complaints = complaints,
                            selectedComplaint = selectedComplaintForInspect,
                            onSelectComplaint = { selectedComplaintForInspect = it }
                        )
                    } else {
                        InteractiveCityHeatmapCanvas(
                            complaints = complaints,
                            onSelectComplaint = { selectedComplaintForInspect = it }
                        )
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Text(
                        text = "GEOTAGGED DEFECT COORDINATES (GPS)",
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF38BDF8),
                        letterSpacing = 0.8.sp
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(complaints) { c ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Slate800)
                                    .clickable { selectedComplaintForInspect = c }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("coord_chip_${c.id.removePrefix("#")}")
                            ) {
                                Text(
                                    text = "${c.id}: ${c.formattedCoordinates()}",
                                    color = Slate200,
                                    fontSize = 10.sp,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                        }
                    }
                }
            }
        }

        // 5. AI Municipal Triage Insights
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFFEFF6FF)),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFBFDBFE)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = CivicBlue, modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "AI Triage & Route Recommendations",
                            fontWeight = FontWeight.Bold,
                            color = CivicNavy,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "• Cluster Detected: Complaint #2041 and #2039 lie within 0.6 miles on the downtown bus corridor. Grouping repair crew dispatch saves 38 minutes.",
                        fontSize = 12.sp,
                        color = Slate800
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "• Weather Correlation: Pothole defect in Dist. 4 has water ingress vulnerability. Urgent patching prioritized to avoid structural road base scour.",
                        fontSize = 12.sp,
                        color = Slate800
                    )
                }
            }
        }

        // 6. Work-Order Table Header & Filter Chips
        item {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Work Orders & Repair Deadlines",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Showing ${filteredComplaints.size} of $totalCount",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Severity Filter Row
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    item {
                        FilterChip(
                            selected = severityFilter == null,
                            onClick = { severityFilter = null },
                            label = { Text("All Severity") },
                            modifier = Modifier.testTag("filter_all_severity")
                        )
                    }
                    items(Severity.values()) { sev ->
                        FilterChip(
                            selected = severityFilter == sev,
                            onClick = { severityFilter = if (severityFilter == sev) null else sev },
                            label = { Text(sev.label) },
                            modifier = Modifier.testTag("filter_${sev.name.lowercase()}")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Status Filter Row
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf("All", "Active", "Repaired").forEach { status ->
                        FilterChip(
                            selected = stageFilter == status,
                            onClick = { stageFilter = status },
                            label = { Text(status) },
                            modifier = Modifier.testTag("filter_status_${status.lowercase()}")
                        )
                    }
                }
            }
        }

        // 7. Work-Order Cards / Table Rows
        items(filteredComplaints, key = { it.id }) { complaint ->
            AdminWorkOrderCard(
                complaint = complaint,
                onAssignEngineer = { complaintToAssignEngineer = complaint },
                onMarkRepaired = { complaintToMarkRepaired = complaint },
                onInspect = { selectedComplaintForInspect = complaint }
            )
        }
    }

    // Dialog: Inspect Complaint
    selectedComplaintForInspect?.let { comp ->
        ComplaintDetailDialog(
            complaint = comp,
            onDismiss = { selectedComplaintForInspect = null }
        )
    }

    // Dialog: Assign Engineer
    complaintToAssignEngineer?.let { comp ->
        AssignEngineerDialog(
            complaint = comp,
            onDismiss = { complaintToAssignEngineer = null },
            onConfirmAssign = { engineer, slaHours ->
                ComplaintRepository.assignEngineer(comp.id, engineer, slaHours)
                complaintToAssignEngineer = null
            }
        )
    }

    // Dialog: Mark Repaired
    complaintToMarkRepaired?.let { comp ->
        MarkRepairedDialog(
            complaint = comp,
            onDismiss = { complaintToMarkRepaired = null },
            onConfirmRepaired = { notes ->
                ComplaintRepository.markRepaired(comp.id, notes)
                complaintToMarkRepaired = null
            }
        )
    }
}

@Composable
fun MetricSummaryCard(
    title: String,
    count: Int,
    accentColor: Color,
    modifier: Modifier = Modifier,
    isAlert: Boolean = false,
    tag: String
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = modifier.testTag(tag)
    ) {
        Column(
            modifier = Modifier
                .padding(12.dp)
                .fillMaxWidth()
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(accentColor)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "$count",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = if (isAlert) SeverityCritical else Slate900
            )
            Text(
                text = title,
                style = MaterialTheme.typography.bodySmall,
                color = Slate600,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1
            )
        }
    }
}

@Composable
fun SeverityStatItem(label: String, count: Int, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$label: ",
            fontSize = 11.sp,
            color = Slate600,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = "$count",
            fontSize = 11.sp,
            color = Slate900,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InteractiveCityHeatmapCanvas(
    complaints: List<Complaint>,
    onSelectComplaint: (Complaint) -> Unit
) {
    // Map bounds simulation around San Francisco coordinates
    // Latitude range: ~ 37.750 to 37.795
    // Longitude range: ~ -122.430 to -122.385
    val minLat = 37.750
    val maxLat = 37.795
    val minLon = -122.430
    val maxLon = -122.385

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(240.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Slate950)
            .pointerInput(complaints) {
                detectTapGestures { tapOffset ->
                    // Find nearest complaint to tap
                    val tapW = size.width
                    val tapH = size.height
                    var nearestComplaint: Complaint? = null
                    var minDistanceSq = 50f * 50f // 50px radius

                    complaints.forEach { c ->
                        val normX = ((c.longitude - minLon) / (maxLon - minLon)).toFloat().coerceIn(0.1f, 0.9f)
                        val normY = (1f - ((c.latitude - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0.1f, 0.9f)
                        val pinX = normX * tapW
                        val pinY = normY * tapH
                        val distSq = (tapOffset.x - pinX) * (tapOffset.x - pinX) + (tapOffset.y - pinY) * (tapOffset.y - pinY)
                        if (distSq < minDistanceSq) {
                            minDistanceSq = distSq
                            nearestComplaint = c
                        }
                    }

                    nearestComplaint?.let { onSelectComplaint(it) }
                }
            }
            .testTag("interactive_heatmap_canvas")
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height

            // 1. Draw Grid lines (City avenues & streets)
            val gridColor = Color(0xFF1E293B)
            val streetColor = Color(0xFF334155)

            for (i in 1..8) {
                val y = h * (i / 9f)
                drawLine(
                    color = if (i % 3 == 0) streetColor else gridColor,
                    start = Offset(0f, y),
                    end = Offset(w, y),
                    strokeWidth = if (i % 3 == 0) 2f else 1f
                )
            }
            for (i in 1..8) {
                val x = w * (i / 9f)
                drawLine(
                    color = if (i % 3 == 0) streetColor else gridColor,
                    start = Offset(x, 0f),
                    end = Offset(x, h),
                    strokeWidth = if (i % 3 == 0) 2f else 1f
                )
            }

            // 2. Diagonal Expressway Corridor
            drawLine(
                color = Color(0xFF475569),
                start = Offset(w * 0.1f, h * 0.9f),
                end = Offset(w * 0.9f, h * 0.2f),
                strokeWidth = 3f
            )

            // 3. Draw Heat Glow Gradients for each complaint
            complaints.forEach { c ->
                val normX = ((c.longitude - minLon) / (maxLon - minLon)).toFloat().coerceIn(0.1f, 0.9f)
                val normY = (1f - ((c.latitude - minLat) / (maxLat - minLat)).toFloat()).coerceIn(0.1f, 0.9f)
                val pinX = normX * w
                val pinY = normY * h

                val heatColor = when (c.severity) {
                    Severity.CRITICAL -> SeverityCritical
                    Severity.HIGH -> SeverityHigh
                    Severity.MEDIUM -> SeverityMedium
                    Severity.LOW -> SeverityLow
                }

                // Heat dispersion circle
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(heatColor.copy(alpha = 0.45f), Color.Transparent),
                        center = Offset(pinX, pinY),
                        radius = 48.dp.toPx()
                    ),
                    radius = 48.dp.toPx(),
                    center = Offset(pinX, pinY)
                )

                // Pin Outer Ring
                drawCircle(
                    color = heatColor.copy(alpha = 0.3f),
                    radius = 12.dp.toPx(),
                    center = Offset(pinX, pinY)
                )

                // Pin Solid Core
                drawCircle(
                    color = if (c.isRepaired) StatusRepaired else heatColor,
                    radius = 6.dp.toPx(),
                    center = Offset(pinX, pinY)
                )
                drawCircle(
                    color = Color.White,
                    radius = 2.dp.toPx(),
                    center = Offset(pinX, pinY)
                )
            }
        }

        // Map Legend overlay at bottom
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(8.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Slate900.copy(alpha = 0.85f))
                .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SeverityCritical))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Critical", color = Slate200, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(SeverityHigh))
            Spacer(modifier = Modifier.width(4.dp))
            Text("High", color = Slate200, fontSize = 10.sp)
            Spacer(modifier = Modifier.width(8.dp))

            Box(modifier = Modifier.size(6.dp).clip(CircleShape).background(StatusRepaired))
            Spacer(modifier = Modifier.width(4.dp))
            Text("Repaired", color = Slate200, fontSize = 10.sp)
        }
    }
}

@Composable
fun AdminWorkOrderCard(
    complaint: Complaint,
    onAssignEngineer: () -> Unit,
    onMarkRepaired: () -> Unit,
    onInspect: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("admin_work_order_${complaint.id.removePrefix("#")}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header Row: ID, Severity, Stage
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
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    SeverityBadge(severity = complaint.severity)
                }

                StageStatusChip(stage = complaint.stage)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = complaint.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Slate900
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(Icons.Default.LocationOn, contentDescription = null, tint = Slate400, modifier = Modifier.size(14.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${complaint.location} (${complaint.district})",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )
            }

            // GPS Coordinates
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(Icons.Default.GpsFixed, contentDescription = null, tint = Color(0xFF0284C7), modifier = Modifier.size(12.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "GPS: ${complaint.formattedCoordinates()}",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF0284C7)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Assigned Engineer & Deadline Countdown Bar
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color(0xFFF8FAFC))
                    .padding(10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Assigned Crew:",
                        fontSize = 10.sp,
                        color = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = complaint.assignedEngineer?.name ?: "Unassigned (Pending Dispatch)",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (complaint.assignedEngineer != null) Slate900 else SeverityHigh
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Repair Deadline:",
                        fontSize = 10.sp,
                        color = Slate400,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    SlaCountdownView(
                        deadlineEpochMs = complaint.deadlineEpochMs,
                        isRepaired = complaint.isRepaired,
                        compact = true
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Action Buttons: Assign Engineer, Mark Repaired, Inspect
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Assign Engineer button
                OutlinedButton(
                    onClick = onAssignEngineer,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("button_assign_engineer_${complaint.id.removePrefix("#")}")
                ) {
                    Icon(Icons.Default.PersonAdd, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Assign", fontSize = 12.sp)
                }

                // Mark Repaired button
                if (!complaint.isRepaired) {
                    Button(
                        onClick = onMarkRepaired,
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRepaired),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .weight(1.3f)
                            .testTag("button_mark_repaired_${complaint.id.removePrefix("#")}")
                    ) {
                        Icon(Icons.Default.CheckCircle, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Mark Repaired", fontSize = 12.sp)
                    }
                } else {
                    OutlinedButton(
                        onClick = onInspect,
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1.3f)
                    ) {
                        Text("Verified ✓", color = StatusRepaired, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                // View Details Button
                IconButton(
                    onClick = onInspect,
                    modifier = Modifier.testTag("button_inspect_${complaint.id.removePrefix("#")}")
                ) {
                    Icon(Icons.Default.TableChart, contentDescription = "View Details", tint = CivicBlue)
                }
            }
        }
    }
}

@Composable
fun AssignEngineerDialog(
    complaint: Complaint,
    onDismiss: () -> Unit,
    onConfirmAssign: (EngineerCrew, Int) -> Unit
) {
    var selectedEngineer by remember { mutableStateOf(ComplaintRepository.availableEngineers.first()) }
    var selectedSlaHours by remember { mutableStateOf(complaint.severity.defaultSlaHours) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("assign_engineer_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "Assign Repair Engineer",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Work Order for ${complaint.id}: ${complaint.title}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Select Municipal Crew Unit:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                ComplaintRepository.availableEngineers.forEach { engineer ->
                    val isSelected = selectedEngineer.id == engineer.id
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .border(
                                width = if (isSelected) 2.dp else 1.dp,
                                color = if (isSelected) CivicBlue else Slate200,
                                shape = RoundedCornerShape(10.dp)
                            )
                            .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                            .clickable { selectedEngineer = engineer }
                            .padding(12.dp)
                            .testTag("engineer_option_${engineer.id}")
                    ) {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = engineer.name,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = if (isSelected) CivicBlue else Slate900
                                )
                                Text(
                                    text = engineer.vehicleNumber,
                                    fontSize = 11.sp,
                                    color = Slate400,
                                    fontFamily = FontFamily.Monospace
                                )
                            }
                            Text(text = engineer.role, fontSize = 11.sp, color = Slate600)
                            Text(text = "Coverage: ${engineer.district}", fontSize = 10.sp, color = Slate400)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "SLA Target Deadline:",
                    style = MaterialTheme.typography.labelSmall,
                    color = Slate600,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(4, 12, 24, 48).forEach { hrs ->
                        FilterChip(
                            selected = selectedSlaHours == hrs,
                            onClick = { selectedSlaHours = hrs },
                            label = { Text("${hrs}h") },
                            modifier = Modifier.testTag("sla_option_${hrs}h")
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirmAssign(selectedEngineer, selectedSlaHours) },
                        colors = ButtonDefaults.buttonColors(containerColor = CivicBlue),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("button_confirm_assign")
                    ) {
                        Text("Dispatch Crew")
                    }
                }
            }
        }
    }
}

@Composable
fun MarkRepairedDialog(
    complaint: Complaint,
    onDismiss: () -> Unit,
    onConfirmRepaired: (String) -> Unit
) {
    var notes by remember {
        mutableStateOf("Cold-mix asphalt patch applied, compacted with pneumatic tamper. Roadway reopened to traffic.")
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("mark_repaired_dialog")
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.AssignmentTurnedIn, contentDescription = null, tint = StatusRepaired, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Mark Ticket Repaired",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Close out ${complaint.id} and advance pipeline to VERIFY.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Repair Completion Notes") },
                    minLines = 3,
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("input_repair_notes")
                )

                Spacer(modifier = Modifier.height(20.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    OutlinedButton(onClick = onDismiss, shape = RoundedCornerShape(10.dp)) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = { onConfirmRepaired(notes) },
                        colors = ButtonDefaults.buttonColors(containerColor = StatusRepaired),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.testTag("button_confirm_mark_repaired")
                    ) {
                        Text("Confirm Closure")
                    }
                }
            }
        }
    }
}
