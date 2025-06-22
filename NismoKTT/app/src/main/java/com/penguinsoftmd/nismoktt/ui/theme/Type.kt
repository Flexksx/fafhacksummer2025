package com.penguinsoftmd.nismoktt.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.penguinsoftmd.nismoktt.R

// 1. Define the FontFamily using the new, correct filename
val fredoka = FontFamily(
    Font(R.font.fredoka_regular, FontWeight.Normal)
)

// 2. Update your Typography to use this font family
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    // Update the style that your title will use
    headlineLarge = TextStyle(
        fontFamily = fredoka, // Use the new FontFamily
        fontWeight = FontWeight.Normal,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = 0.sp
    ),
    /* Other default text styles */
)