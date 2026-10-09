package com.example.ui.citizen

import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.AddAPhoto
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Engineering
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Send
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.R
import com.example.ai.GeminiDamageClassifier
import com.example.data.ComplaintRepository
import com.example.model.AiAnalysis
import com.example.model.Complaint
import com.example.model.PipelineStage
import com.example.model.SampleDamagePreset
import com.example.model.TimelineEvent
import com.example.ui.components.CameraCaptureDialog
import com.example.ui.components.CitizenPinnedGpsMapView
import com.example.ui.components.DamageDetectionOverlay
import com.example.ui.components.LocationPermissionBanner
import com.example.ui.components.PipelineStrip
import com.example.ui.components.SeverityBadge
import com.example.ui.components.SlaCountdownView
import com.example.ui.components.StageStatusChip
import com.example.ui.components.rememberLocationPermissionState
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.example.util.LocationHelper
import com.example.ui.theme.CivicBlue
import com.example.ui.theme.CivicNavy
import com.example.ui.theme.Slate100
import com.example.ui.theme.Slate200
import com.example.ui.theme.Slate400
import com.example.ui.theme.Slate600
import com.example.ui.theme.Slate700
import com.example.ui.theme.Slate800
import com.example.ui.theme.Slate900
import com.example.ui.theme.StatusRepaired
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun CitizenScreen(
    onNavigateToComplaintDetail: (Complaint) -> Unit = {}
) {
    var selectedTab by remember { mutableIntStateOf(0) } // 0 = New Report, 1 = My Complaints

    val complaints by ComplaintRepository.complaints.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF8FAFC))
    ) {
        // Tab Row: Report Damage / My Complaints
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor = Color.White,
            contentColor = CivicBlue,
            modifier = Modifier.fillMaxWidth().testTag("citizen_tab_row")
        ) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = {
                    Text(
                        "Report Road Damage",
                        fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Medium
                    )
                },
                modifier = Modifier.testTag("tab_report_damage")
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            "My Complaints",
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(if (selectedTab == 1) CivicBlue else Slate200)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "${complaints.size}",
                                color = if (selectedTab == 1) Color.White else Slate700,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                },
                modifier = Modifier.testTag("tab_my_complaints")
            )
        }

        if (selectedTab == 0) {
            NewReportFlow(
                onReportSubmitted = {
                    selectedTab = 1 // Switch to My Complaints after submission
                }
            )
        } else {
            MyComplaintsFlow(
                complaints = complaints,
                onSelectComplaint = onNavigateToComplaintDetail
            )
        }
    }
}

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun NewReportFlow(
    onReportSubmitted: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Pipeline Stage for the new submission
    var currentPipelineStage by remember { mutableStateOf(PipelineStage.CAPTURE) }

    // Photo selection state
    var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }
    var selectedPreset by remember { mutableStateOf<SampleDamagePreset?>(ComplaintRepository.samplePresets.first()) }
    var isAnalyzingAi by remember { mutableStateOf(false) }
    var aiAnalysisResult by remember { mutableStateOf<AiAnalysis?>(null) }
    var showCameraDialog by remember { mutableStateOf(false) }

    // Form inputs & GPS Coordinates
    var noteText by remember { mutableStateOf("") }
    var locationText by remember { mutableStateOf("Grand Ave & 5th St, District 4") }
    var pinnedLatitude by remember { mutableDoubleStateOf(37.77492) }
    var pinnedLongitude by remember { mutableDoubleStateOf(-122.41941) }
    var pinnedAccuracy by remember { mutableFloatStateOf(4.5f) }
    var isFetchingGps by remember { mutableStateOf(false) }

    fun refreshGpsLocation() {
        coroutineScope.launch {
            isFetchingGps = true
            val gps = LocationHelper.getCurrentGpsLocation(context)
            pinnedLatitude = gps.latitude
            pinnedLongitude = gps.longitude
            pinnedAccuracy = gps.accuracyMeters
            locationText = gps.formattedAddress
            isFetchingGps = false
        }
    }

    // Accompanist permissions state for Fine & Coarse Location
    val locationPermissionsState = rememberLocationPermissionState(
        onPermissionsGranted = {
            refreshGpsLocation()
        }
    )

    fun requestGpsPinning() {
        if (locationPermissionsState.allPermissionsGranted || LocationHelper.hasLocationPermission(context)) {
            refreshGpsLocation()
        } else {
            locationPermissionsState.launchMultiplePermissionRequest()
        }
    }

    // Photo picker launcher with automated GPS coordinate pinning
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedPhotoUri = uri
            selectedPreset = null
            requestGpsPinning()
            triggerAiDetection(
                context = context,
                uri = uri,
                preset = null,
                coroutineScope = coroutineScope,
                onStart = {
                    isAnalyzingAi = true
                    currentPipelineStage = PipelineStage.INGEST
                },
                onComplete = { result ->
                    aiAnalysisResult = result
                    isAnalyzingAi = false
                    currentPipelineStage = PipelineStage.DETECT
                }
            )
        }
    }

    // Automatically trigger initial AI check on the default sample preset
    LaunchedEffect(Unit) {
        if (aiAnalysisResult == null && selectedPreset != null) {
            triggerAiDetection(
                context = context,
                uri = null,
                preset = selectedPreset,
                coroutineScope = coroutineScope,
                onStart = {
                    isAnalyzingAi = true
                    currentPipelineStage = PipelineStage.INGEST
                },
                onComplete = { result ->
                    aiAnalysisResult = result
                    isAnalyzingAi = false
                    currentPipelineStage = PipelineStage.DETECT
                }
            )
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("new_report_list"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Pipeline Strip above the phone
        item {
            PipelineStrip(
                currentStage = currentPipelineStage,
                modifier = Modifier.fillMaxWidth()
            )
        }

        // 2. Photo Selection Section
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "1. Damage Photograph",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Slate900
                    )
                    Text(
                        text = "Take or choose a photo of the road hazard, or pick a sample defect below.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Slate600
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Buttons to take / pick photo
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Button(
                            onClick = { showCameraDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = CivicBlue),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("button_open_camera")
                        ) {
                            Icon(Icons.Default.PhotoCamera, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Camera", fontSize = 12.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                photoPickerLauncher.launch(
                                    PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("button_pick_photo")
                        ) {
                            Icon(Icons.Default.PhotoLibrary, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Gallery", fontSize = 12.sp, maxLines = 1)
                        }

                        OutlinedButton(
                            onClick = {
                                // Cycle to next sample preset
                                val presets = ComplaintRepository.samplePresets
                                val curIdx = presets.indexOf(selectedPreset)
                                val nextPreset = presets[(curIdx + 1).coerceAtLeast(0) % presets.size]
                                selectedPreset = nextPreset
                                selectedPhotoUri = null
                                locationText = nextPreset.defaultLocation
                                noteText = nextPreset.defaultNote
                                pinnedLatitude = nextPreset.latitude
                                pinnedLongitude = nextPreset.longitude
                                pinnedAccuracy = 3.5f

                                triggerAiDetection(
                                    context = context,
                                    uri = null,
                                    preset = nextPreset,
                                    coroutineScope = coroutineScope,
                                    onStart = {
                                        isAnalyzingAi = true
                                        currentPipelineStage = PipelineStage.INGEST
                                    },
                                    onComplete = { res ->
                                        aiAnalysisResult = res
                                        isAnalyzingAi = false
                                        currentPipelineStage = PipelineStage.DETECT
                                    }
                                )
                            },
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 8.dp),
                            modifier = Modifier
                                .weight(1f)
                                .testTag("button_cycle_sample")
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Sample", fontSize = 12.sp, maxLines = 1)
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // Sample Presets Carousel
                    Text(
                        text = "Quick Sample Presets:",
                        style = MaterialTheme.typography.labelSmall,
                        color = Slate600,
                        fontWeight = FontWeight.SemiBold
                    )
                    Spacer(modifier = Modifier.height(6.dp))

                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        items(ComplaintRepository.samplePresets) { preset ->
                            val isSelected = selectedPreset?.id == preset.id && selectedPhotoUri == null
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) CivicBlue else Slate200,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .background(if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC))
                                    .clickable {
                                        selectedPreset = preset
                                        selectedPhotoUri = null
                                        locationText = preset.defaultLocation
                                        noteText = preset.defaultNote
                                        pinnedLatitude = preset.latitude
                                        pinnedLongitude = preset.longitude
                                        pinnedAccuracy = 3.5f
                                        triggerAiDetection(
                                            context = context,
                                            uri = null,
                                            preset = preset,
                                            coroutineScope = coroutineScope,
                                            onStart = {
                                                isAnalyzingAi = true
                                                currentPipelineStage = PipelineStage.INGEST
                                            },
                                            onComplete = { res ->
                                                aiAnalysisResult = res
                                                isAnalyzingAi = false
                                                currentPipelineStage = PipelineStage.DETECT
                                            }
                                        )
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                                    .testTag("sample_chip_${preset.id}")
                            ) {
                                Text(
                                    text = preset.title,
                                    fontSize = 12.sp,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSelected) CivicBlue else Slate800
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Image Display Box with Detection Reticle Overlay
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(220.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate900)
                            .testTag("damage_photo_preview_box")
                    ) {
                        when {
                            selectedPhotoUri != null -> {
                                AsyncImage(
                                    model = selectedPhotoUri,
                                    contentDescription = "User uploaded damage photo",
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            selectedPreset != null -> {
                                Image(
                                    painter = painterResource(id = selectedPreset!!.drawableResId),
                                    contentDescription = selectedPreset!!.title,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }

                        // AI Reticle / Bounding box overlay
                        if (aiAnalysisResult != null && !isAnalyzingAi) {
                            DamageDetectionOverlay(
                                aiAnalysis = aiAnalysisResult!!,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                        // Scanning Indicator Overlay
                        if (isAnalyzingAi) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    CircularProgressIndicator(
                                        color = Color.White,
                                        modifier = Modifier.size(36.dp)
                                    )
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = "AI SCANNING DAMAGE...",
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontFamily = FontFamily.Monospace,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 3. Simulated AI Check Result Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("ai_check_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                Icons.Default.AutoAwesome,
                                contentDescription = null,
                                tint = CivicBlue,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "2. AI Damage Inspection",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900
                            )
                        }

                        if (aiAnalysisResult != null) {
                            val confPct = (aiAnalysisResult!!.confidence * 100).toInt()
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(Color(0xFFEFF6FF))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = "$confPct% Confidence",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = CivicBlue
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (isAnalyzingAi) {
                        Column(modifier = Modifier.padding(vertical = 12.dp)) {
                            LinearProgressIndicator(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(RoundedCornerShape(3.dp)),
                                color = CivicBlue
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Extracting defect topology & evaluating roadway hazard...",
                                style = MaterialTheme.typography.bodySmall,
                                color = Slate600
                            )
                        }
                    } else if (aiAnalysisResult != null) {
                        val ai = aiAnalysisResult!!
                        // Name of Damage Type & Severity
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Color(0xFFF8FAFC))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "CLASSIFIED DEFECT",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Slate400,
                                    letterSpacing = 1.sp
                                )
                                SeverityBadge(severity = ai.severity)
                            }

                            Spacer(modifier = Modifier.height(4.dp))

                            Text(
                                text = ai.damageType,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Slate900,
                                modifier = Modifier.testTag("ai_damage_type_text")
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            // Grid of AI metrics
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                MetricColumn(
                                    label = "Hazard Score",
                                    value = "${ai.hazardRating}/10"
                                )
                                MetricColumn(
                                    label = "Est. Depth",
                                    value = "${ai.depthEstimateCm} cm"
                                )
                                MetricColumn(
                                    label = "Impact Area",
                                    value = "${ai.areaSqFt} sq ft"
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider(color = Slate200)
                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "Recommended Municipal Action:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = Slate700
                            )
                            Text(
                                text = ai.recommendedRepair,
                                fontSize = 12.sp,
                                color = Slate600
                            )

                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "Model: ${ai.aiModelUsed}",
                                fontSize = 10.sp,
                                color = Slate400,
                                fontFamily = FontFamily.Monospace
                            )
                        }
                    }
                }
            }
        }

        // 4. Notes, GPS Pinning & Location Card
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = Color.White),
                shape = RoundedCornerShape(16.dp),
                elevation = CardDefaults.cardElevation(2.dp),
                modifier = Modifier.fillMaxWidth().testTag("location_and_notes_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "3. GPS Geotag & Citizen Note",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Slate900
                        )

                        // Button to re-fetch GPS
                        OutlinedButton(
                            onClick = { requestGpsPinning() },
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .height(32.dp)
                                .testTag("button_refresh_gps")
                        ) {
                            if (isFetchingGps) {
                                CircularProgressIndicator(color = CivicBlue, modifier = Modifier.size(12.dp))
                            } else {
                                Icon(Icons.Default.MyLocation, contentDescription = null, tint = CivicBlue, modifier = Modifier.size(14.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Auto-Pin GPS", color = CivicBlue, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    // Accompanist Location Permission Banner (shown if permission is not yet granted)
                    LocationPermissionBanner(
                        permissionState = locationPermissionsState,
                        onRequestPermission = { locationPermissionsState.launchMultiplePermissionRequest() },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Embedded Google Maps SDK View pinning the coordinates
                    CitizenPinnedGpsMapView(
                        latitude = pinnedLatitude,
                        longitude = pinnedLongitude,
                        accuracyMeters = pinnedAccuracy,
                        formattedAddress = locationText,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Location address input
                    OutlinedTextField(
                        value = locationText,
                        onValueChange = { locationText = it },
                        label = { Text("Geocoded Location") },
                        supportingText = {
                            Text(
                                text = String.format("Pinned: %.5f° N, %.5f° W (±%.1fm)", kotlin.math.abs(pinnedLatitude), kotlin.math.abs(pinnedLongitude), pinnedAccuracy),
                                fontFamily = FontFamily.Monospace,
                                fontSize = 10.sp,
                                color = Slate600
                            )
                        },
                        leadingIcon = {
                            Icon(Icons.Default.LocationOn, contentDescription = null, tint = CivicBlue)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_location"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CivicBlue,
                            unfocusedBorderColor = Slate200
                        )
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    // Note input
                    OutlinedTextField(
                        value = noteText,
                        onValueChange = { noteText = it },
                        label = { Text("Add Notes / Observations") },
                        placeholder = { Text("e.g. Deep pothole right next to bike lane, cars are swerving into oncoming traffic...") },
                        minLines = 3,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("input_note"),
                        shape = RoundedCornerShape(10.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = CivicBlue,
                            unfocusedBorderColor = Slate200
                        )
                    )
                }
            }
        }

        // 5. Submit Button
        item {
            Button(
                onClick = {
                    val ai = aiAnalysisResult ?: GeminiDamageClassifier.simulateAiDetection(selectedPreset?.damageType)
                    val generatedId = ComplaintRepository.generateNextId()
                    val nowMs = System.currentTimeMillis()

                    val newComplaint = Complaint(
                        id = generatedId,
                        title = ai.damageType,
                        location = locationText.ifBlank { "Grand Ave & 5th St, District 4" },
                        district = "District 4 (Downtown)",
                        latitude = pinnedLatitude,
                        longitude = pinnedLongitude,
                        accuracyMeters = pinnedAccuracy,
                        photoUri = selectedPhotoUri?.toString(),
                        sampleDrawableId = if (selectedPhotoUri == null) (selectedPreset?.drawableResId ?: R.drawable.sample_pothole) else null,
                        note = noteText.ifBlank { "Reported by citizen with automated AI damage classification and GPS geotag." },
                        stage = PipelineStage.DISPATCH, // Immediately dispatched to municipal queue
                        severity = ai.severity,
                        aiAnalysis = ai,
                        createdAtEpochMs = nowMs,
                        deadlineEpochMs = nowMs + (ai.severity.defaultSlaHours * 3600 * 1000L),
                        timeline = listOf(
                            TimelineEvent(nowMs, PipelineStage.CAPTURE, "Photo & GPS Captured", "Citizen recorded photo & pinned ${String.format("%.5f, %.5f", pinnedLatitude, pinnedLongitude)}", "Citizen"),
                            TimelineEvent(nowMs + 1000L, PipelineStage.INGEST, "Ingested", "Processed by municipal queue", "System"),
                            TimelineEvent(nowMs + 2000L, PipelineStage.DETECT, "AI Detected ${ai.damageType}", "Classified ${ai.severity.label} severity", "AI Engine"),
                            TimelineEvent(nowMs + 3000L, PipelineStage.DISPATCH, "Work Order Created", "Queued for engineer assignment", "Dispatcher")
                        )
                    )

                    ComplaintRepository.addComplaint(newComplaint)
                    currentPipelineStage = PipelineStage.DISPATCH
                    onReportSubmitted()
                },
                enabled = !isAnalyzingAi && aiAnalysisResult != null,
                colors = ButtonDefaults.buttonColors(containerColor = CivicBlue),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("button_submit_report")
            ) {
                Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Submit Complaint to Public Works",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showCameraDialog) {
        CameraCaptureDialog(
            onDismiss = { showCameraDialog = false },
            onPhotoCaptured = { uri ->
                selectedPhotoUri = uri
                selectedPreset = null
                requestGpsPinning()
                triggerAiDetection(
                    context = context,
                    uri = uri,
                    preset = null,
                    coroutineScope = coroutineScope,
                    onStart = {
                        isAnalyzingAi = true
                        currentPipelineStage = PipelineStage.INGEST
                    },
                    onComplete = { result ->
                        aiAnalysisResult = result
                        isAnalyzingAi = false
                        currentPipelineStage = PipelineStage.DETECT
                    }
                )
            }
        )
    }
}

private fun triggerAiDetection(
    context: android.content.Context,
    uri: Uri?,
    preset: SampleDamagePreset?,
    coroutineScope: kotlinx.coroutines.CoroutineScope,
    onStart: () -> Unit,
    onComplete: (AiAnalysis) -> Unit
) {
    onStart()
    coroutineScope.launch {
        // Quick realistic delay to simulate pipeline ingestion & detector
        delay(600L)
        val result = GeminiDamageClassifier.analyzeDamage(
            context = context,
            imageUri = uri,
            bitmap = null,
            sampleHint = preset?.damageType
        )
        onComplete(result)
    }
}

@Composable
private fun MetricColumn(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 10.sp, color = Slate400, fontWeight = FontWeight.SemiBold)
        Text(text = value, fontSize = 13.sp, color = Slate800, fontWeight = FontWeight.Bold)
    }
}

@Composable
fun MyComplaintsFlow(
    complaints: List<Complaint>,
    onSelectComplaint: (Complaint) -> Unit
) {
    // Finds Complaint #2041 or the first complaint
    val complaint2041 = complaints.firstOrNull { it.id == "#2041" } ?: complaints.firstOrNull()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .testTag("my_complaints_list"),
        contentPadding = PaddingValues(vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Spotlight Card: Complaint #2041 Live Status Tracker
        if (complaint2041 != null) {
            item {
                FeaturedComplaintTrackerCard(
                    complaint = complaint2041,
                    onClick = { onSelectComplaint(complaint2041) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "All Reported Incidents (${complaints.size})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Slate900
                )
                Text(
                    text = "Live Sync",
                    style = MaterialTheme.typography.labelSmall,
                    color = CivicBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // List of all citizen complaints
        items(complaints, key = { it.id }) { item ->
            CitizenComplaintItemCard(
                complaint = item,
                onClick = { onSelectComplaint(item) }
            )
        }
    }
}

@Composable
fun FeaturedComplaintTrackerCard(
    complaint: Complaint,
    onClick: () -> Unit
) {
    var expandedTimeline by remember { mutableStateOf(false) }

    Card(
        colors = CardDefaults.cardColors(containerColor = Slate900),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(6.dp),
        modifier = Modifier
            .fillMaxWidth()
            .testTag("featured_complaint_2041_card")
    ) {
        Column(
            modifier = Modifier
                .padding(16.dp)
                .animateContentSize()
        ) {
            // Top Badge Row
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
                    Text(
                        text = "FEATURED TRACKER",
                        color = Color(0xFF38BDF8),
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                SeverityBadge(severity = complaint.severity)
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = complaint.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 4.dp)
            ) {
                Icon(
                    Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Slate400,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = complaint.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate400
                )
            }

            // GPS Coordinates Tag
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Icon(
                    Icons.Default.GpsFixed,
                    contentDescription = null,
                    tint = Color(0xFF38BDF8),
                    modifier = Modifier.size(12.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = complaint.formattedCoordinates(),
                    color = Color(0xFF38BDF8),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Pipeline Strip embedded inside the card
            PipelineStrip(
                currentStage = complaint.stage,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Assigned Engineer & SLA Countdown Row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Slate800)
                    .padding(12.dp),
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
                        text = complaint.assignedEngineer?.name ?: "Pending Dispatch",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    if (complaint.assignedEngineer != null) {
                        Text(
                            text = complaint.assignedEngineer.role,
                            fontSize = 10.sp,
                            color = Color(0xFF38BDF8)
                        )
                    }
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

            // Expand Timeline button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { expandedTimeline = !expandedTimeline }
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (expandedTimeline) "Hide Live Audit Log" else "View Live Audit Log (${complaint.timeline.size} events)",
                    color = CivicBlue,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
                Icon(
                    imageVector = if (expandedTimeline) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = null,
                    tint = CivicBlue,
                    modifier = Modifier.size(18.dp)
                )
            }

            AnimatedVisibility(visible = expandedTimeline) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                ) {
                    complaint.timeline.forEach { event ->
                        TimelineEventRow(event)
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun CitizenComplaintItemCard(
    complaint: Complaint,
    onClick: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = RoundedCornerShape(14.dp),
        elevation = CardDefaults.cardElevation(2.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .testTag("complaint_item_${complaint.id.removePrefix("#")}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Thumbnail
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Slate100)
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
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Slate200),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.AddAPhoto, contentDescription = null, tint = Slate400)
                    }
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Details
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = complaint.id,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        color = Slate600
                    )
                    SeverityBadge(severity = complaint.severity)
                }

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = complaint.title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = Slate900,
                    maxLines = 1
                )

                Text(
                    text = complaint.location,
                    style = MaterialTheme.typography.bodySmall,
                    color = Slate600,
                    maxLines = 1
                )

                Text(
                    text = complaint.formattedCoordinates(),
                    fontFamily = FontFamily.Monospace,
                    fontSize = 10.sp,
                    color = CivicBlue,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    StageStatusChip(stage = complaint.stage)
                    SlaCountdownView(
                        deadlineEpochMs = complaint.deadlineEpochMs,
                        isRepaired = complaint.isRepaired,
                        compact = true
                    )
                }
            }

            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Slate400,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
fun TimelineEventRow(event: TimelineEvent) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Slate800.copy(alpha = 0.6f))
            .padding(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .padding(top = 2.dp)
                .size(6.dp)
                .clip(CircleShape)
                .background(if (event.stage == PipelineStage.VERIFY) StatusRepaired else CivicBlue)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = event.title,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = event.actor,
                    fontSize = 10.sp,
                    color = Slate400
                )
            }
            Text(
                text = event.description,
                fontSize = 11.sp,
                color = Slate200
            )
        }
    }
}
