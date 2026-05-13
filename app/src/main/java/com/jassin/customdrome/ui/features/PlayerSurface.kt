package com.jassin.customdrome.ui.features

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import com.jassin.customdrome.FullPlayerContent
import com.jassin.customdrome.MiniPlayerContent
import kotlin.math.roundToInt

@Composable
fun PlayerSurface(
    progress: Float,
    playerHeightPx: Float,
    playerTopPx: Float,
    cornerRadius: Dp,
    modifier: Modifier = Modifier, // This contains the gestures/clickable
    onCollapse: () -> Unit,
) {
    val density = LocalDensity.current

    Box(
        modifier =
            Modifier // Start with a fresh Modifier
                .fillMaxWidth()
                // 1. Set Height and Offset FIRST so the coordinate system is updated
                .height(with(density) { playerHeightPx.toDp() })
                .offset { IntOffset(0, playerTopPx.roundToInt()) }
                // 2. Apply the passed-in modifier (Gestures) LAST
                .then(modifier)
                .background(
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
                ),
    ) {
        // --- Mini Player View ---
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = (1f - progress * 2f).coerceIn(0f, 1f)
                    },
        ) {
            MiniPlayerContent()
        }

        // --- Full Player View ---
        Box(
            modifier =
                Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        alpha = ((progress - 0.5f) * 2f).coerceIn(0f, 1f)
                    },
        ) {
            FullPlayerContent(onCollapse = onCollapse)
        }
    }
}
