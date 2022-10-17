package com.example.finito.core.presentation.util.preview

import androidx.compose.ui.tooling.preview.Devices
import androidx.compose.ui.tooling.preview.Preview

@Preview(
    name = "portrait",
    group = "orientation",
)
@Preview(
    name = "landscape",
    group = "orientation",
    device = Devices.AUTOMOTIVE_1024p,
    widthDp = 720,
    heightDp = 360
)
annotation class OrientationPreviews
