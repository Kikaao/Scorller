package com.scorller.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp

/**
 * A soft vertical wash from the screen background into a faint tint of [tint] and
 * back, used instead of a single flat fill so screens don't read as hard color
 * blocks against each other.
 */
@Composable
fun screenBackgroundBrush(tint: Color = MaterialTheme.colorScheme.primary): Brush {
    val background = MaterialTheme.colorScheme.background
    return Brush.verticalGradient(
        colors = listOf(
            background,
            lerp(background, tint, 0.05f),
            background
        )
    )
}
