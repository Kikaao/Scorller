package com.scorller.ui.deck

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scorller.data.ServiceLocator
import com.scorller.data.model.Item
import kotlinx.coroutines.launch

@Composable
fun SwipeDeckScreen(
    sessionId: String,
    onDone: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<SwipeDeckViewModel>(
        factory = remember {
            SwipeDeckViewModelFactory(
                ServiceLocator.sessionRepository,
                ServiceLocator.identityManager(context),
                sessionId
            )
        }
    )
    val items by viewModel.items.collectAsStateWithLifecycle()
    val currentIndex by viewModel.currentIndex.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            "${currentIndex.coerceAtMost(items.size)} / ${items.size} swiped",
            style = MaterialTheme.typography.labelLarge
        )
        Spacer(Modifier.padding(4.dp))

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            when {
                items.isEmpty() -> Text("No items yet. Add some from the session hub.")
                currentIndex >= items.size -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("You've swiped everything!", style = MaterialTheme.typography.headlineSmall)
                    Spacer(Modifier.padding(8.dp))
                    Button(onClick = onDone) { Text("See results") }
                }
                else -> {
                    val item = items[currentIndex]
                    SwipeableCard(
                        key = item.id,
                        item = item,
                        onSwiped = { isRight -> viewModel.swipe(item, isRight) }
                    )
                }
            }
        }

        Spacer(Modifier.padding(4.dp))
        Button(
            onClick = { viewModel.goBack() },
            enabled = currentIndex > 0,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Go back / re-vote previous")
        }
    }
}

@Composable
private fun SwipeableCard(
    key: String,
    item: Item,
    onSwiped: (isRight: Boolean) -> Unit
) {
    val offsetX = remember(key) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var cardWidthPx by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(key) {
        offsetX.snapTo(0f)
    }

    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .onSizeChanged { cardWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = (offsetX.value / cardWidthPx) * 25f
            }
            .pointerInput(key) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                    },
                    onDragEnd = {
                        val threshold = size.width * 0.4f
                        scope.launch {
                            if (offsetX.value > threshold) {
                                offsetX.animateTo(size.width.toFloat() * 1.5f, tween(250))
                                onSwiped(true)
                            } else if (offsetX.value < -threshold) {
                                offsetX.animateTo(-size.width.toFloat() * 1.5f, tween(250))
                                onSwiped(false)
                            } else {
                                offsetX.animateTo(0f, tween(250))
                            }
                        }
                    }
                )
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(20.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(item.title, style = MaterialTheme.typography.headlineSmall)
            item.notes?.let {
                Spacer(Modifier.padding(4.dp))
                Text(it, style = MaterialTheme.typography.bodyMedium)
            }
            item.link?.let {
                Spacer(Modifier.padding(4.dp))
                Text(it, style = MaterialTheme.typography.bodySmall)
            }
            Spacer(Modifier.padding(16.dp))
            Text("← swipe left for no · swipe right for yes →", style = MaterialTheme.typography.labelMedium)
        }
    }
}
