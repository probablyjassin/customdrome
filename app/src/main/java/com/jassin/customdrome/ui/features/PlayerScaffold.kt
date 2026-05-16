package com.jassin.customdrome.ui.features

import androidx.activity.compose.BackHandler
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.jassin.customdrome.ui.common.TabsBar
import com.jassin.customdrome.ui.common.TopBar
import kotlinx.coroutines.launch
import kotlin.math.pow

// Height constants shared between scaffold and content padding
private val TopBarHeight = 64.dp
private val MiniPlayerHeight = 72.dp
private val BottomNavHeight = 80.dp

private fun Float.powCurve(exponent: Float): Float =
    this
        .coerceIn(0f, 1f)
        .toDouble()
        .pow(exponent.toDouble())
        .toFloat()

@Composable
fun PlayerScaffold(
    navController: NavHostController,
    showNavBars: Boolean,
    content: @Composable (PaddingValues) -> Unit,
) {
    val scope = rememberCoroutineScope()

    var isSongPlaying by remember { mutableStateOf(true) } // TODO: wire to real player

    val dismissOffsetY = remember { Animatable(0f) }

    // animation logic for the miniplayer -> fullscreen player transition
    // 0 = collapsed, 1 = expanded
    // snapTo() on every drag frame so the sheet follows the finger
    val expandProgress = remember { Animatable(0f, Float.VectorConverter) }

    // top nav
    if (showNavBars) {
        TopBar(onGoToSettings = { navController.navigate(route = "settings") })
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val density = LocalDensity.current
        val screenHeightPx = constraints.maxHeight.toFloat()

        val miniPlayerHeightPx = with(density) { MiniPlayerHeight.toPx() }
        val bottomNavHeightPx = with(density) { BottomNavHeight.toPx() }

        // total travel distance of the sheet's top edge
        val travelPx =
            screenHeightPx - miniPlayerHeightPx - bottomNavHeightPx - with(density) { 15.dp.toPx() }

        // main content
        content(
            PaddingValues(
                top = if (showNavBars) TopBarHeight else 0.dp,
                bottom = BottomNavHeight,
            ),
        )

        // bottom navbar
        if (showNavBars) {
            Box(
                modifier =
                    Modifier
                        .align(Alignment.BottomCenter),
            ) {
                TabsBar(navController)
            }

            // player surface
            if (isSongPlaying) {
                val progress = expandProgress.value

                val topCurve = 1f
                val heightCurve = 0.72f

                val topP = progress.powCurve(topCurve)
                val heightP = progress.powCurve(heightCurve)

                val playerTopPx = travelPx * (1f - topP)
                val playerHeightPx =
                    miniPlayerHeightPx + heightP * (screenHeightPx - miniPlayerHeightPx)

                val cornerRadius = (16.dp * (1f - progress)).coerceAtLeast(0.dp)

                var startProgress by remember { mutableFloatStateOf(0f) }
                var startedFromCollapsed by remember { mutableStateOf(false) }
                var didDownwardDismiss by remember { mutableStateOf(false) }

                // BackHandler re-registered on every navigation change
                val currentEntry by navController.currentBackStackEntryAsState()
                key(currentEntry) {
                    BackHandler(enabled = expandProgress.value > 0.5f) {
                        scope.launch {
                            expandProgress.animateTo(
                                0f,
                                tween(300),
                            )
                        }
                    }
                }

                PlayerSurface(
                    progress = progress,
                    playerHeightPx = playerHeightPx,
                    playerTopPx = playerTopPx,
                    cornerRadius = cornerRadius,
                    dismissOffsetYPx = dismissOffsetY.value,
                    onCollapse = {
                        scope.launch {
                            expandProgress.animateTo(
                                0f,
                                tween(300),
                            )
                        }
                    },
                    modifier =
                        Modifier
                            .pointerInput(Unit) {
                                detectVerticalDragGestures(
                                    onDragStart = {
                                        scope.launch { expandProgress.stop() }
                                        startProgress = expandProgress.value
                                        startedFromCollapsed = expandProgress.value < 0.15f
                                        didDownwardDismiss = false
                                    },
                                    onVerticalDrag = { change, dragAmount ->
                                        change.consume()
                                        when {
                                            // A gesture that starts collapsed and moves downward is a dismissal attempt.
                                            startedFromCollapsed && dragAmount > 0f -> {
                                                didDownwardDismiss = true
                                                scope.launch {
                                                    dismissOffsetY.snapTo(
                                                        (dismissOffsetY.value + dragAmount).coerceAtLeast(0f),
                                                    )
                                                }
                                            }

                                            // If the user reverses a dismissal gesture, just return to the mini-player.
                                            startedFromCollapsed && didDownwardDismiss && dragAmount < 0f -> {
                                                scope.launch {
                                                    dismissOffsetY.snapTo(
                                                        (dismissOffsetY.value + dragAmount).coerceAtLeast(0f),
                                                    )
                                                }
                                            }

                                            // Upward drag from the collapsed state should expand the player.
                                            startedFromCollapsed && dragAmount < 0f -> {
                                                scope.launch {
                                                    val delta = -dragAmount / travelPx
                                                    expandProgress.snapTo(
                                                        (expandProgress.value + delta).coerceIn(0f, 1f),
                                                    )
                                                }
                                            }

                                            // Downward drag while not collapsed keeps the sheet behavior stable.
                                            else -> {
                                                val delta = -dragAmount / travelPx
                                                scope.launch {
                                                    expandProgress.snapTo(
                                                        (expandProgress.value + delta).coerceIn(0f, 1f),
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onDragEnd = {
                                        if (startedFromCollapsed) {
                                            // decide whether to dismiss based on vertical travel
                                            val threshold = with(density) { 20.dp.toPx() }
                                            if (didDownwardDismiss) {
                                                if (dismissOffsetY.value > threshold) {
                                                    // animate off-screen to bottom-right then mark not playing
                                                    val targetY = screenHeightPx + 400f
                                                    scope.launch {
                                                        dismissOffsetY.animateTo(targetY, tween(300))
                                                        // hide player (user will add actual stop later)
                                                        isSongPlaying = false
                                                        // reset offsets just in case
                                                        dismissOffsetY.snapTo(0f)
                                                    }
                                                } else {
                                                    // animate back to original position
                                                    scope.launch {
                                                        dismissOffsetY.animateTo(0f, tween(200))
                                                    }
                                                }
                                            } else {
                                                val target = if (expandProgress.value > startProgress) 1f else 0f
                                                scope.launch {
                                                    expandProgress.animateTo(
                                                        target,
                                                        tween(60, easing = LinearEasing),
                                                    )
                                                }
                                            }
                                        } else {
                                            val target = if (expandProgress.value > startProgress) 1f else 0f
                                            scope.launch {
                                                expandProgress.animateTo(
                                                    target,
                                                    tween(60, easing = LinearEasing),
                                                )
                                            }
                                        }
                                    },
                                    onDragCancel = {
                                        // reset any dismissal offsets
                                        scope.launch {
                                            dismissOffsetY.animateTo(0f, tween(200))
                                            startedFromCollapsed = false
                                            didDownwardDismiss = false
                                        }
                                    },
                                )
                            }.clickable(
                                enabled = progress < 0.5f,
                                // This tracks the interaction state (press, drag, etc.)
                                interactionSource = remember { MutableInteractionSource() },
                                // This defines the visual effect. Setting it to null removes the circle/ripple.
                                indication = null,
                            ) {
                                scope.launch { expandProgress.animateTo(1f, tween(150)) }
                            },
                )
            }
        }
    }
}
