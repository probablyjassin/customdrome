package com.jassin.customdrome

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlin.math.roundToInt

// Height constants shared between scaffold and content padding
private val MiniPlayerHeight = 72.dp
private val BottomNavHeight = 80.dp

@Composable
fun PlayerScaffold(
    navController: NavHostController,
    content: @Composable (PaddingValues) -> Unit,
) {
    val isSongPlaying = true // ← swap for real state later

    var isExpanded by remember { mutableStateOf(false) }
    // Accumulates drag distance within a single gesture; reset on drag end
    var gestureDragPx by remember { mutableFloatStateOf(0f) }

    val expandProgress by animateFloatAsState(
        targetValue = if (isExpanded) 1f else 0f,
        animationSpec =
            spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessMedium,
            ),
        label = "playerExpand",
    )

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenHeightPx = constraints.maxHeight.toFloat()

        val miniPlayerHeightPx = with(density) { MiniPlayerHeight.toPx() }
        val bottomNavHeightPx = with(density) { BottomNavHeight.toPx() }

        // ── Main content ──────────────────────────────────────────────────────
        val bottomPadding = BottomNavHeight
        content(PaddingValues(bottom = bottomPadding))

        // ── Bottom navigation bar ─────────────────────────────────────────────
        // Translates downward (out of screen) as the player expands.
        val navYOffsetPx = (expandProgress * bottomNavHeightPx).roundToInt()
        Box(
            modifier =
                Modifier
                    .align(Alignment.BottomCenter)
                    .offset { IntOffset(0, navYOffsetPx) },
        ) {
            BottomBar(navController)
        }

        // ── Player surface ────────────────────────────────────────────────────
        if (isSongPlaying) {
            // Collapsed top: rests just above the bottom nav.
            // Expanded top: 0 (fills the entire screen).
            val collapsedTopPx = screenHeightPx - miniPlayerHeightPx - bottomNavHeightPx - 55
            val playerTopPx = collapsedTopPx * (1f - expandProgress)
            val playerHeightPx = miniPlayerHeightPx + expandProgress * (screenHeightPx - miniPlayerHeightPx)

            // Corner radius fades to 0 as the sheet goes full screen
            val cornerRadius = (16.dp * (1f - expandProgress)).coerceAtLeast(0.dp)

            Box(
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .height(with(density) { playerHeightPx.toDp() })
                        .offset { IntOffset(0, playerTopPx.roundToInt()) }
                        .background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(topStart = cornerRadius, topEnd = cornerRadius),
                        )
                        // Drag up to expand, drag down to collapse
                        .pointerInput(Unit) {
                            detectVerticalDragGestures(
                                onDragEnd = {
                                    if (!isExpanded && gestureDragPx < -80f) {
                                        isExpanded = true
                                    } else if (isExpanded && gestureDragPx > 80f) {
                                        isExpanded = false
                                    }
                                    gestureDragPx = 0f
                                },
                                onVerticalDrag = { change, amount ->
                                    change.consume()
                                    gestureDragPx += amount
                                },
                            )
                        }
                        // Tap the mini bar to expand; disabled while already expanded
                        .clickable(enabled = !isExpanded) { isExpanded = true },
            ) {
                // Mini player: visible during the first half of expansion, then fades out
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = (1f - expandProgress * 2f).coerceIn(0f, 1f) },
                ) {
                    MiniPlayerContent()
                }

                // Full player: fades in during the second half of expansion
                Box(
                    modifier =
                        Modifier
                            .fillMaxSize()
                            .graphicsLayer { alpha = ((expandProgress - 0.5f) * 2f).coerceIn(0f, 1f) },
                ) {
                    FullPlayerContent(onCollapse = { isExpanded = false })
                }
            }
        }
    }
}

// ── Mini player ───────────────────────────────────────────────────────────────

@Composable
private fun MiniPlayerContent() {
    Row(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Song Title",
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
            )
            Text(
                text = "Artist Name",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
            )
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = {}) {
                Icon(Icons.Default.SkipPrevious, contentDescription = "Previous")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.PlayArrow, contentDescription = "Play / Pause")
            }
            IconButton(onClick = {}) {
                Icon(Icons.Default.SkipNext, contentDescription = "Next")
            }
        }
    }
}

// ── Full screen player ────────────────────────────────────────────────────────

@Composable
private fun FullPlayerContent(onCollapse: () -> Unit) {
    Column(
        modifier =
            Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Drag handle / collapse chevron
        IconButton(
            onClick = onCollapse,
            modifier = Modifier.padding(top = 8.dp),
        ) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Collapse player")
        }

        // Album art placeholder
        Box(
            modifier =
                Modifier
                    .size(280.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primary,
                        shape = RoundedCornerShape(20.dp),
                    ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Default.MusicNote,
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.onPrimary,
            )
        }

        // Song info
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "Song Title",
                style = MaterialTheme.typography.headlineSmall,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Artist Name",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        // Playback controls
        Row(
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(bottom = 48.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = {}, modifier = Modifier.size(56.dp)) {
                Icon(
                    imageVector = Icons.Default.SkipPrevious,
                    contentDescription = "Previous",
                    modifier = Modifier.fillMaxSize(),
                )
            }
            FilledIconButton(onClick = {}, modifier = Modifier.size(72.dp)) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Play / Pause",
                    modifier = Modifier.size(36.dp),
                )
            }
            IconButton(onClick = {}, modifier = Modifier.size(56.dp)) {
                Icon(
                    imageVector = Icons.Default.SkipNext,
                    contentDescription = "Next",
                    modifier = Modifier.fillMaxSize(),
                )
            }
        }
    }
}
