package com.example.myapplication

/**
 * Data model representing a recorded speed violation.
 *
 * This class is used for:
 * - Mapping Firestore documents into Kotlin objects
 * - Displaying violation details in UI components (RecyclerView, notifications)
 *
 * @property speed The actual speed of the user at the time of violation (km/h).
 * @property limit The allowed speed limit that was exceeded (km/h).
 * @property userID Identifier of the user who committed the violation.
 * @property status Status of the violation record (e.g., "pending", "resolved").
 */
data class SpeedViolation(
    val speed: Float,
    val limit:Float,
    val userID: String ="",
    val status:String =""
)
