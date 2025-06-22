package com.penguinsoftmd.nismoktt.data.activities

import com.penguinsoftmd.nismoktt.domain.dsm5.Dsm5SpectrumCategory

data class Activity(
    val id: String,
    val domain: Dsm5SpectrumCategory,
    val type: ActivityType, // e.g., "At-Home", "Outdoor", "General Strategy"
    val title: String,
    val description: String,
    val aiCue: String?, // AI Cue is optional for general strategies
    val isHighImpact: Boolean = false // Optional: To highlight key activities
)
