package com.pgstorm148.protapclub.ui.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

// Light theme colors - keep these as they're working well
val LightColors = lightColorScheme(
    primary = Color(0xFF3F51B5),       // Indigo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1D9FF),
    onPrimaryContainer = Color(0xFF001A6B),
    secondary = Color(0xFF03A9F4),     // Light Blue
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),    // Light gray
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E)
)

// Updated dark theme colors to match light theme colors
val DarkColors = darkColorScheme(
    // Using the same colors as Light mode
    primary = Color(0xFF3F51B5),       // Indigo
    onPrimary = Color.White,
    primaryContainer = Color(0xFFD1D9FF),
    onPrimaryContainer = Color(0xFF001A6B),
    secondary = Color(0xFF03A9F4),     // Light Blue
    onSecondary = Color.White,
    background = Color(0xFFF5F5F5),    // Light gray
    onBackground = Color(0xFF1A1C1E),
    surface = Color.White,
    onSurface = Color(0xFF1A1C1E),

    // Adding defaults for other required fields in darkColorScheme
    error = Color(0xFFB3261E),
    onError = Color.White,
    surfaceVariant = Color(0xFFE7E0EC),
    onSurfaceVariant = Color(0xFF49454F)
)