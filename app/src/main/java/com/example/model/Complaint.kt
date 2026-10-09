package com.example.model

import com.example.R

enum class PipelineStage(
    val stepNumber: Int,
    val title: String,
    val description: String,
    val tag: String
) {
    CAPTURE(1, "Capture", "Citizen photo & geotag recorded", "CAP"),
    INGEST(2, "Ingest", "Municipal backend indexed & queued", "ING"),
    DETECT(3, "Detect", "AI computer vision classification", "DET"),
    DISPATCH(4, "Dispatch", "Crew assigned & repair SLA active", "DSP"),
    VERIFY(5, "Verify", "Work verified & ticket resolved", "VER")
}

enum class Severity(
    val label: String,
    val defaultSlaHours: Int,
    val priorityScore: Int
) {
    LOW("Low", 72, 1),
    MEDIUM("Medium", 36, 2),
    HIGH("High", 12, 3),
    CRITICAL("Critical", 4, 4)
}

data class AiBoundingBox(
    val left: Float,
    val top: Float,
    val right: Float,
    val bottom: Float,
    val label: String
)

data class AiAnalysis(
    val damageType: String,
    val severity: Severity,
    val confidence: Float, // e.g. 0.965f
    val hazardRating: Int, // 1 - 10
    val depthEstimateCm: Float = 8.5f,
    val areaSqFt: Float = 3.2f,
    val recommendedRepair: String = "Cold-mix asphalt patch + pneumatic tamp",
    val boundingBox: AiBoundingBox? = null,
    val aiModelUsed: String = "Gemini Vision Multimodal / Civic-CV Edge"
)

data class TimelineEvent(
    val epochMs: Long,
    val stage: PipelineStage,
    val title: String,
    val description: String,
    val actor: String
)

data class EngineerCrew(
    val id: String,
    val name: String,
    val role: String,
    val district: String,
    val vehicleNumber: String,
    val phone: String
)

data class Complaint(
    val id: String, // e.g. "#2041"
    val title: String,
    val location: String,
    val district: String,
    val latitude: Double,
    val longitude: Double,
    val photoUri: String? = null,
    val sampleDrawableId: Int? = null,
    val note: String = "",
    val stage: PipelineStage = PipelineStage.CAPTURE,
    val severity: Severity = Severity.MEDIUM,
    val aiAnalysis: AiAnalysis? = null,
    val assignedEngineer: EngineerCrew? = null,
    val deadlineEpochMs: Long = System.currentTimeMillis() + (12 * 3600 * 1000L),
    val createdAtEpochMs: Long = System.currentTimeMillis(),
    val isRepaired: Boolean = false,
    val repairedAtEpochMs: Long? = null,
    val repairNotes: String? = null,
    val accuracyMeters: Float? = null,
    val timeline: List<TimelineEvent> = emptyList()
) {
    fun formattedCoordinates(): String {
        val latDir = if (latitude >= 0) "N" else "S"
        val lonDir = if (longitude >= 0) "E" else "W"
        return String.format("%.5f° %s, %.5f° %s", kotlin.math.abs(latitude), latDir, kotlin.math.abs(longitude), lonDir)
    }
}

data class SampleDamagePreset(
    val id: String,
    val title: String,
    val damageType: String,
    val severity: Severity,
    val drawableResId: Int,
    val defaultNote: String,
    val defaultLocation: String,
    val latitude: Double,
    val longitude: Double,
    val confidence: Float,
    val hazardRating: Int,
    val recommendedRepair: String
)
