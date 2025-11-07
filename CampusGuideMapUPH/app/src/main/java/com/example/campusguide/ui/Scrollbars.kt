package com.example.campusguide.ui.common

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.max
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.unit.dp
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.toSize
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.foundation.layout.BoxWithConstraints

private val DefaultThumbColor = Color(0xFF16224C).copy(alpha = 0.35f)

@Composable
fun VerticalScrollbar(
    scroll: ScrollState,
    modifier: Modifier = Modifier,
    thickness: Dp = 3.dp,
    minThumb: Dp = 42.dp,
    color: Color = DefaultThumbColor
) {
    Box(
        modifier
            .width(thickness)
            .fillMaxHeight()
            .drawBehind {
                val trackW = thickness.toPx()
                val viewportH = size.height
                val totalScrollable = scroll.maxValue.toFloat()
                val contentH = viewportH + totalScrollable
                if (contentH <= viewportH + 1f) return@drawBehind

                val fracVisible = viewportH / contentH
                val thumbH = max(minThumb.toPx(), viewportH * fracVisible)
                val fracStart = if (totalScrollable <= 0f) 0f else scroll.value / totalScrollable
                val y = (viewportH - thumbH) * fracStart

                drawRoundRect(
                    color = color,
                    topLeft = Offset(size.width - trackW, y),
                    size = Size(trackW, thumbH),
                    cornerRadius = CornerRadius(trackW, trackW)
                )
            }
    )
}

@Composable
fun HorizontalScrollbar(
    scroll: ScrollState,
    modifier: Modifier = Modifier,
    thickness: Dp = 4.dp,
    cornerRadius: Dp = 2.dp,
    trackColor: Color = Color(0xFF16224C).copy(alpha = 0.12f),
    thumbColor: Color = Color(0xFF16224C).copy(alpha = 0.55f)
) {
    BoxWithConstraints(
        modifier = modifier
            .fillMaxWidth()
            .height(thickness)
    ) {
        val maxW = constraints.maxWidth.toFloat()
        val contentTotal = maxW + scroll.maxValue
        val proportion = if (contentTotal <= 0f) 1f else maxW / contentTotal
        val thumbW = maxW * proportion
        val thumbX = if (scroll.maxValue == 0)
            0f
        else
            (scroll.value.toFloat() / scroll.maxValue) * (maxW - thumbW)

        Canvas(Modifier.fillMaxSize()) {
            val r = cornerRadius.toPx()
            drawRoundRect(
                color = trackColor,
                cornerRadius = CornerRadius(r, r),
                size = Size(size.width, size.height)
            )
            drawRoundRect(
                color = thumbColor,
                topLeft = Offset(thumbX, 0f),
                size = Size(thumbW, size.height),
                cornerRadius = CornerRadius(r, r)
            )
        }
    }
}