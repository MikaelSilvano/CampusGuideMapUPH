package com.example.campusguide.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.campusguide.R

@Composable
fun AppBackground(
    modifier: Modifier = Modifier,
    imageAlignment: Alignment = Alignment.Center,
    overlayAlpha: Float = 0.82f,
    content: @Composable BoxScope.() -> Unit
) {
    Box(modifier.fillMaxSize()) {
        Image(
            painter = painterResource(R.drawable.uph_building_background),
            contentDescription = null,
            modifier = Modifier.matchParentSize(),
            contentScale = ContentScale.Crop,
            alignment = imageAlignment
        )

        Box(
            Modifier
                .matchParentSize()
                .background(Color.White.copy(alpha = overlayAlpha))
        )
        content()
    }
}
