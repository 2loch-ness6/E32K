package com.justcallmekoko.maraudercontroller.ui.theme

import androidx.compose.ui.graphics.Color

// Nexus Theme Colors
val NexusPrimary = Color(0xFF00E5FF)    // Cyan Accent
val NexusSecondary = Color(0xFF76FF03)  // Neon Green
val NexusTertiary = Color(0xFFFF4081)   // Pink Accent

val NexusBackground = Color(0xFF0A0A0A) // Deep Black
val NexusSurface = Color(0xFF1C1C1C)    // Dark Gray Surface
val NexusSurfaceVariant = Color(0xFF2D2D2D) // Lighter Gray
val NexusOnSurface = Color(0xFFEEEEEE)  // White Text
val NexusOnSurfaceVariant = Color(0xFFAAAAAA) // Gray Text

val NexusError = Color(0xFFFF5252)      // Red
val NexusWarning = Color(0xFFFFAB40)    // Orange
val NexusSuccess = Color(0xFF69F0AE)    // Green

val NexusTerminalBg = Color(0xFF000000)
val NexusTerminalText = Color(0xFF00FF00)

// Legacy compatibility (if needed, but prefer Nexus colors)
val MarauderPrimary = NexusPrimary
val MarauderSecondary = NexusSecondary
val MarauderBackground = NexusBackground
val MarauderSurface = NexusSurface
val MarauderDanger = NexusError
