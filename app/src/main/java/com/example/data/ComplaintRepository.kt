package com.example.data

import com.example.R
import com.example.model.AiAnalysis
import com.example.model.AiBoundingBox
import com.example.model.Complaint
import com.example.model.EngineerCrew
import com.example.model.PipelineStage
import com.example.model.SampleDamagePreset
import com.example.model.Severity
import com.example.model.TimelineEvent
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

object ComplaintRepository {

    val availableEngineers = listOf(
        EngineerCrew(
            id = "ENG-101",
            name = "Eng. Sarah Chen",
            role = "Lead - Rapid Pothole Unit Alpha",
            district = "District 4 (Downtown)",
            vehicleNumber = "PW-Truck #44",
            phone = "+1 (555) 234-8901"
        ),
        EngineerCrew(
            id = "ENG-102",
            name = "Marcus Vance",
            role = "Senior Highway & Structural Tech",
            district = "District 1 & 2 (North)",
            vehicleNumber = "PW-Truck #19",
            phone = "+1 (555) 345-9012"
        ),
        EngineerCrew(
            id = "ENG-103",
            name = "Elena Rostova",
            role = "Stormwater & Drainage Specialist",
            district = "District 5 (Bayview)",
            vehicleNumber = "PW-Truck #82",
            phone = "+1 (555) 456-0123"
        ),
        EngineerCrew(
            id = "ENG-104",
            name = "David Kalu",
            role = "Emergency Asphalt Patching Crew",
            district = "District 3 (East)",
            vehicleNumber = "PW-Truck #07",
            phone = "+1 (555) 567-1234"
        )
    )

    val samplePresets = listOf(
        SampleDamagePreset(
            id = "sample_pothole_1",
            title = "Deep Roadway Pothole",
            damageType = "Severe Roadway Pothole (Grade 4 - Deep Pit)",
            severity = Severity.HIGH,
            drawableResId = R.drawable.sample_pothole,
            defaultNote = "Deep crater right in the wheel path. Several cars swerved suddenly into oncoming traffic.",
            defaultLocation = "Grand Ave & 5th St, District 4",
            latitude = 37.7749,
            longitude = -122.4194,
            confidence = 0.968f,
            hazardRating = 8,
            recommendedRepair = "Immediate cold-pour asphalt filling and roller compaction"
        ),
        SampleDamagePreset(
            id = "sample_crack_1",
            title = "Longitudinal Fissure",
            damageType = "Longitudinal Structural Asphalt Fissure",
            severity = Severity.MEDIUM,
            drawableResId = R.drawable.sample_fissure,
            defaultNote = "Deep structural crack propagating along the lane divider over 30 feet.",
            defaultLocation = "Pine Blvd & 12th St, District 2",
            latitude = 37.7892,
            longitude = -122.4014,
            confidence = 0.942f,
            hazardRating = 5,
            recommendedRepair = "Hot-pour elastomeric crack sealant injection"
        ),
        SampleDamagePreset(
            id = "sample_sinkhole_1",
            title = "Storm Drain Sinkhole",
            damageType = "Subsurface Void & Collapsed Catch Basin",
            severity = Severity.CRITICAL,
            drawableResId = R.drawable.sample_pothole,
            defaultNote = "Pavement edge collapsing near storm sewer drain. Hazardous for cyclists.",
            defaultLocation = "Market St & 8th, District 1",
            latitude = 37.7785,
            longitude = -122.4148,
            confidence = 0.984f,
            hazardRating = 10,
            recommendedRepair = "Emergency perimeter barricade & structural base grouting"
        )
    )

    private val now = System.currentTimeMillis()

    // Complaint #2041 from the prompt/deck, plus other realistic district reports
    private val initialComplaints = listOf(
        Complaint(
            id = "#2041",
            title = "Severe Roadway Pothole & Edge Breakdown",
            location = "Grand Ave & 5th St, District 4",
            district = "Downtown Metro (Dist. 4)",
            latitude = 37.7749,
            longitude = -122.4194,
            sampleDrawableId = R.drawable.sample_pothole,
            note = "Deep hole right next to crosswalk. Vehicles swerving into oncoming traffic during peak rush hour.",
            stage = PipelineStage.DISPATCH,
            severity = Severity.HIGH,
            aiAnalysis = AiAnalysis(
                damageType = "Severe Roadway Pothole (Grade 4 - Deep Pit)",
                severity = Severity.HIGH,
                confidence = 0.968f,
                hazardRating = 8,
                depthEstimateCm = 9.4f,
                areaSqFt = 4.1f,
                recommendedRepair = "Immediate cold-pour asphalt leveling & pneumatic roller compaction",
                boundingBox = AiBoundingBox(0.24f, 0.32f, 0.76f, 0.74f, "Pothole Defect #P4"),
                aiModelUsed = "Civic Vision AI v4.2 / Gemini 2.5 Vision"
            ),
            assignedEngineer = availableEngineers[0], // Eng. Sarah Chen
            createdAtEpochMs = now - (35 * 60 * 1000L),
            deadlineEpochMs = now + (2 * 3600 * 1000L + 42 * 60 * 1000L), // 2h 42m remaining
            timeline = listOf(
                TimelineEvent(
                    epochMs = now - (35 * 60 * 1000L),
                    stage = PipelineStage.CAPTURE,
                    title = "Complaint Logged",
                    description = "Citizen uploaded geo-tagged photograph and field observation notes.",
                    actor = "Citizen (App User)"
                ),
                TimelineEvent(
                    epochMs = now - (34 * 60 * 1000L),
                    stage = PipelineStage.INGEST,
                    title = "Telemetry Ingested & Indexed",
                    description = "Server assigned ID #2041, matched location to District 4 GIS grid.",
                    actor = "CivicSync Server"
                ),
                TimelineEvent(
                    epochMs = now - (32 * 60 * 1000L),
                    stage = PipelineStage.DETECT,
                    title = "AI Vision Detected Pothole",
                    description = "Model classified Grade 4 pit (96.8% confidence). High severity hazard flag raised.",
                    actor = "AI Triage Engine"
                ),
                TimelineEvent(
                    epochMs = now - (20 * 60 * 1000L),
                    stage = PipelineStage.DISPATCH,
                    title = "Dispatched to Rapid Unit Alpha",
                    description = "Assigned to Eng. Sarah Chen. Work order #WO-9912 generated. SLA timer initiated.",
                    actor = "Municipal Dispatcher"
                )
            )
        ),
        Complaint(
            id = "#2039",
            title = "Collapsed Utility Trench & Void",
            location = "Market St & 8th, District 1",
            district = "Civic Center (Dist. 1)",
            latitude = 37.7785,
            longitude = -122.4148,
            sampleDrawableId = R.drawable.sample_pothole,
            note = "Cavity under pavement near storm drain. Pavement flexing when buses pass.",
            stage = PipelineStage.DISPATCH,
            severity = Severity.CRITICAL,
            aiAnalysis = AiAnalysis(
                damageType = "Subsurface Void & Collapsed Trench",
                severity = Severity.CRITICAL,
                confidence = 0.984f,
                hazardRating = 10,
                depthEstimateCm = 28.0f,
                areaSqFt = 8.5f,
                recommendedRepair = "Immediate safety barrier, structural soil grouting & base rebuild",
                boundingBox = AiBoundingBox(0.20f, 0.25f, 0.80f, 0.75f, "Critical Void #S1"),
                aiModelUsed = "Civic Vision AI v4.2"
            ),
            assignedEngineer = availableEngineers[1], // Marcus Vance
            createdAtEpochMs = now - (90 * 60 * 1000L),
            deadlineEpochMs = now + (48 * 60 * 1000L), // 48m remaining (Critical!)
            timeline = listOf(
                TimelineEvent(now - 90 * 60 * 1000L, PipelineStage.CAPTURE, "Logged", "Citizen reported via mobile app", "Citizen"),
                TimelineEvent(now - 88 * 60 * 1000L, PipelineStage.INGEST, "Ingested", "Geo-verified on bus corridor", "System"),
                TimelineEvent(now - 85 * 60 * 1000L, PipelineStage.DETECT, "AI Detected Sinkhole", "Critical void detected (98.4% confidence)", "AI Triage"),
                TimelineEvent(now - 60 * 60 * 1000L, PipelineStage.DISPATCH, "Dispatched to Marcus Vance", "Priority work order generated", "Dispatcher")
            )
        ),
        Complaint(
            id = "#2040",
            title = "Longitudinal Thermal Crack",
            location = "Pine Blvd & 12th St, District 2",
            district = "Financial North (Dist. 2)",
            latitude = 37.7892,
            longitude = -122.4014,
            sampleDrawableId = R.drawable.sample_fissure,
            note = "Cracking along asphalt seam.",
            stage = PipelineStage.DETECT,
            severity = Severity.MEDIUM,
            aiAnalysis = AiAnalysis(
                damageType = "Longitudinal Structural Asphalt Fissure",
                severity = Severity.MEDIUM,
                confidence = 0.942f,
                hazardRating = 5,
                depthEstimateCm = 3.8f,
                areaSqFt = 12.5f,
                recommendedRepair = "Hot-pour elastomeric sealant injection",
                boundingBox = AiBoundingBox(0.15f, 0.25f, 0.85f, 0.80f, "Structural Crack #C2"),
                aiModelUsed = "Civic Vision AI v4.2"
            ),
            assignedEngineer = null,
            createdAtEpochMs = now - (150 * 60 * 1000L),
            deadlineEpochMs = now + (31 * 3600 * 1000L),
            timeline = listOf(
                TimelineEvent(now - 150 * 60 * 1000L, PipelineStage.CAPTURE, "Logged", "Reported by commuter", "Citizen"),
                TimelineEvent(now - 149 * 60 * 1000L, PipelineStage.INGEST, "Ingested", "District 2 registry assigned", "System"),
                TimelineEvent(now - 145 * 60 * 1000L, PipelineStage.DETECT, "AI Classified Crack", "Medium severity pavement fissure", "AI Triage")
            )
        ),
        Complaint(
            id = "#2038",
            title = "Drainage Clog & Aquaplaning Risk",
            location = "Bayview Parkway & 3rd, District 5",
            district = "Bayview South (Dist. 5)",
            latitude = 37.7612,
            longitude = -122.3920,
            sampleDrawableId = R.drawable.sample_fissure,
            note = "Rainwater overflowing curb into roadway lanes.",
            stage = PipelineStage.DISPATCH,
            severity = Severity.HIGH,
            aiAnalysis = AiAnalysis(
                damageType = "Surface Water Retention & Aquaplaning Hazard",
                severity = Severity.HIGH,
                confidence = 0.951f,
                hazardRating = 8,
                depthEstimateCm = 12.0f,
                areaSqFt = 40.0f,
                recommendedRepair = "Vacuum storm sewer flush & grating clearing",
                boundingBox = AiBoundingBox(0.10f, 0.30f, 0.90f, 0.88f, "Water Retention"),
                aiModelUsed = "Civic Vision AI v4.2"
            ),
            assignedEngineer = availableEngineers[2], // Elena Rostova
            createdAtEpochMs = now - (14 * 3600 * 1000L),
            deadlineEpochMs = now - (22 * 60 * 1000L), // SLA OVERDUE by 22m!
            timeline = listOf(
                TimelineEvent(now - 14 * 3600 * 1000L, PipelineStage.CAPTURE, "Logged", "Reported by resident", "Citizen"),
                TimelineEvent(now - 13 * 3600 * 1000L, PipelineStage.INGEST, "Ingested", "Queued in stormwater log", "System"),
                TimelineEvent(now - 13 * 3600 * 1000L, PipelineStage.DETECT, "AI Detected Water Hazard", "High priority drainage hazard", "AI Triage"),
                TimelineEvent(now - 12 * 3600 * 1000L, PipelineStage.DISPATCH, "Dispatched to Elena Rostova", "Work order #WO-9844 active", "Dispatcher")
            )
        ),
        Complaint(
            id = "#2035",
            title = "Dislodged Curb & Broken Walkway",
            location = "Oak Ridge & 3rd St, District 3",
            district = "Mission East (Dist. 3)",
            latitude = 37.7580,
            longitude = -122.4210,
            sampleDrawableId = R.drawable.sample_pothole,
            note = "Curbstone popped up after garbage truck clipped edge.",
            stage = PipelineStage.VERIFY,
            severity = Severity.LOW,
            aiAnalysis = AiAnalysis(
                damageType = "Localized Curb Dislodgement",
                severity = Severity.LOW,
                confidence = 0.930f,
                hazardRating = 3,
                depthEstimateCm = 5.0f,
                areaSqFt = 2.0f,
                recommendedRepair = "Precast curb realignment and mortar bonding",
                boundingBox = AiBoundingBox(0.2f, 0.3f, 0.8f, 0.7f, "Curb"),
                aiModelUsed = "Civic Vision AI v4.2"
            ),
            assignedEngineer = availableEngineers[3], // David Kalu
            createdAtEpochMs = now - (48 * 3600 * 1000L),
            deadlineEpochMs = now - (24 * 3600 * 1000L),
            isRepaired = true,
            repairedAtEpochMs = now - (6 * 3600 * 1000L),
            repairNotes = "Precast curb replaced, quick-set mortar applied, inspected by district supervisor.",
            timeline = listOf(
                TimelineEvent(now - 48 * 3600 * 1000L, PipelineStage.CAPTURE, "Logged", "Reported", "Citizen"),
                TimelineEvent(now - 47 * 3600 * 1000L, PipelineStage.INGEST, "Ingested", "Queued", "System"),
                TimelineEvent(now - 47 * 3600 * 1000L, PipelineStage.DETECT, "AI Detected", "Low severity repair", "AI Triage"),
                TimelineEvent(now - 40 * 3600 * 1000L, PipelineStage.DISPATCH, "Dispatched", "Assigned to David Kalu", "Dispatcher"),
                TimelineEvent(now - 6 * 3600 * 1000L, PipelineStage.VERIFY, "Work Verified & Repaired", "Completed and signed off by supervisor.", "Eng. David Kalu")
            )
        )
    )

    private val _complaints = MutableStateFlow<List<Complaint>>(initialComplaints)
    val complaints: StateFlow<List<Complaint>> = _complaints.asStateFlow()

    private var nextComplaintNumber = 2042

    fun addComplaint(complaint: Complaint) {
        _complaints.update { current ->
            listOf(complaint) + current
        }
    }

    fun generateNextId(): String {
        val id = "#$nextComplaintNumber"
        nextComplaintNumber++
        return id
    }

    fun assignEngineer(complaintId: String, engineer: EngineerCrew, customSlaHours: Int? = null) {
        val nowMs = System.currentTimeMillis()
        _complaints.update { list ->
            list.map { comp ->
                if (comp.id == complaintId) {
                    val slaHours = customSlaHours ?: comp.severity.defaultSlaHours
                    val newDeadline = nowMs + (slaHours * 3600 * 1000L)
                    val newTimeline = comp.timeline + TimelineEvent(
                        epochMs = nowMs,
                        stage = PipelineStage.DISPATCH,
                        title = "Dispatched to ${engineer.name}",
                        description = "Work order assigned to ${engineer.role}. SLA deadline set to $slaHours hours.",
                        actor = "Municipal Dispatcher"
                    )
                    comp.copy(
                        stage = PipelineStage.DISPATCH,
                        assignedEngineer = engineer,
                        deadlineEpochMs = newDeadline,
                        timeline = newTimeline
                    )
                } else {
                    comp
                }
            }
        }
    }

    fun markRepaired(complaintId: String, notes: String) {
        val nowMs = System.currentTimeMillis()
        _complaints.update { list ->
            list.map { comp ->
                if (comp.id == complaintId) {
                    val newTimeline = comp.timeline + TimelineEvent(
                        epochMs = nowMs,
                        stage = PipelineStage.VERIFY,
                        title = "Repaired & Verified",
                        description = notes.ifBlank { "Damage repaired according to municipal standards. Surface inspected and verified." },
                        actor = comp.assignedEngineer?.name ?: "Municipal Inspector"
                    )
                    comp.copy(
                        stage = PipelineStage.VERIFY,
                        isRepaired = true,
                        repairedAtEpochMs = nowMs,
                        repairNotes = notes,
                        timeline = newTimeline
                    )
                } else {
                    comp
                }
            }
        }
    }

    fun advanceStage(complaintId: String, nextStage: PipelineStage, reason: String = "") {
        val nowMs = System.currentTimeMillis()
        _complaints.update { list ->
            list.map { comp ->
                if (comp.id == complaintId) {
                    val newTimeline = comp.timeline + TimelineEvent(
                        epochMs = nowMs,
                        stage = nextStage,
                        title = "Advanced to ${nextStage.title}",
                        description = reason.ifBlank { nextStage.description },
                        actor = "CivicSync Pipeline"
                    )
                    comp.copy(
                        stage = nextStage,
                        timeline = newTimeline
                    )
                } else {
                    comp
                }
            }
        }
    }

    fun resetToDefaults() {
        _complaints.value = initialComplaints
        nextComplaintNumber = 2042
    }
}
