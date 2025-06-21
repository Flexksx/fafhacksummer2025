package com.penguinsoftmd.nismoktt.data.activities.model


data class Activity {
    val id: String,
    val name: String,
    val description: String,
    val icon: String,
    val isEnabled: Boolean = true


}