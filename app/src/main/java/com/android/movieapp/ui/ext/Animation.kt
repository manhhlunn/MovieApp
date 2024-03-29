package com.android.movieapp.ui.ext

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring


val springAnimation = spring(
    dampingRatio = Spring.DampingRatioMediumBouncy,
    stiffness = Spring.StiffnessLow,
    visibilityThreshold = 0.001f,
)

