package com.scorller.ui.deck

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CameraAlt
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Replay
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.scorller.data.ServiceLocator
import com.scorller.data.model.Item
import com.scorller.ui.theme.Gold
import com.scorller.ui.theme.InkTealBright
import com.scorller.ui.theme.LikeGreen
import com.scorller.ui.theme.NopeRed
import com.scorller.ui.theme.screenBackgroundBrush
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
    var pendingSwipe by remember { mutableStateOf<Boolean?>(null) }
    var galleryItem by remember { mutableStateOf<Item?>(null) }

    Column(modifier = Modifier.fillMaxSize().background(screenBackgroundBrush())) {
        Spacer(Modifier.height(16.dp))
        ProgressDots(total = items.size, completed = currentIndex.coerceAtMost(items.size))
        Spacer(Modifier.height(12.dp))

        Box(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentAlignment = Alignment.Center
        ) {
            when {
                items.isEmpty() -> EmptyDeckMessage()
                currentIndex >= items.size -> DeckFinishedMessage(onDone)
                else -> {
                    val visible = items.subList(currentIndex, minOf(currentIndex + 3, items.size))
                    visible.asReversed().forEachIndexed { reversedIdx, item ->
                        val stackPos = visible.size - 1 - reversedIdx
                        if (stackPos == 0) {
                            SwipeableCard(
                                key = item.id,
                                item = item,
                                pendingSwipe = pendingSwipe,
                                onSwipeConsumed = { pendingSwipe = null },
                                onSwiped = { isRight -> viewModel.swipe(item, isRight) },
                                onOpenGallery = { galleryItem = item }
                            )
                        } else {
                            StackedCardBehind(item = item, depth = stackPos)
                        }
                    }
                }
            }
        }

        if (items.isNotEmpty() && currentIndex < items.size) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 20.dp),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundActionButton(
                    icon = Icons.Rounded.Replay,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    background = MaterialTheme.colorScheme.surfaceVariant,
                    size = 48.dp,
                    enabled = currentIndex > 0,
                    onClick = { viewModel.goBack() }
                )
                Spacer(Modifier.size(24.dp))
                RoundActionButton(
                    icon = Icons.Rounded.Close,
                    tint = Color.White,
                    background = NopeRed,
                    size = 64.dp,
                    onClick = { pendingSwipe = false }
                )
                Spacer(Modifier.size(20.dp))
                RoundActionButton(
                    icon = Icons.Rounded.Favorite,
                    tint = Color.White,
                    background = LikeGreen,
                    size = 64.dp,
                    onClick = { pendingSwipe = true }
                )
            }
        }
    }

    galleryItem?.let { item ->
        PhotoGalleryDialog(item = item, onDismiss = { galleryItem = null })
    }
}

@Composable
private fun ProgressDots(total: Int, completed: Int) {
    if (total == 0) return
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        repeat(total) { i ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(4.dp)
                    .clip(MaterialTheme.shapes.extraSmall)
                    .background(
                        if (i < completed) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant
                    )
            )
        }
    }
}

@Composable
private fun EmptyDeckMessage() {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            Icons.Rounded.Image,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(48.dp)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            "No items yet",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            "Add some from the session hub",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DeckFinishedMessage(onDone: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .background(Gold.copy(alpha = 0.18f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.EmojiEvents,
                contentDescription = null,
                tint = Gold,
                modifier = Modifier.size(36.dp)
            )
        }
        Spacer(Modifier.height(20.dp))
        Text(
            "You've swiped everything",
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Head to results to see how the group voted",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))
        RoundActionButton(
            icon = Icons.Rounded.Favorite,
            tint = MaterialTheme.colorScheme.onPrimary,
            background = MaterialTheme.colorScheme.primary,
            size = 56.dp,
            onClick = onDone
        )
    }
}

@Composable
private fun RoundActionButton(
    icon: ImageVector,
    tint: Color,
    background: Color,
    size: Dp,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    IconButton(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .size(size)
            .background(if (enabled) background else background.copy(alpha = 0.4f), shape = CircleShape),
        colors = IconButtonDefaults.iconButtonColors(contentColor = tint)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(size * 0.42f))
    }
}

@Composable
private fun StackedCardBehind(item: Item, depth: Int) {
    val scale = 1f - depth * 0.05f
    val yOffset = depth * 14
    Box(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .aspectRatio(0.72f)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
                translationY = yOffset.dp.toPx()
                alpha = 1f - depth * 0.25f
            }
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    )
}

@Composable
private fun SwipeableCard(
    key: String,
    item: Item,
    pendingSwipe: Boolean?,
    onSwipeConsumed: () -> Unit,
    onSwiped: (isRight: Boolean) -> Unit,
    onOpenGallery: () -> Unit
) {
    val offsetX = remember(key) { Animatable(0f) }
    val scope = rememberCoroutineScope()
    var cardWidthPx by remember { mutableFloatStateOf(1f) }

    LaunchedEffect(key) {
        offsetX.snapTo(0f)
    }

    LaunchedEffect(pendingSwipe, key) {
        val direction = pendingSwipe ?: return@LaunchedEffect
        val target = if (direction) cardWidthPx * 1.5f else -cardWidthPx * 1.5f
        offsetX.animateTo(target, tween(280))
        onSwiped(direction)
        onSwipeConsumed()
    }

    val dragProgress = (offsetX.value / cardWidthPx).coerceIn(-1f, 1f)

    Box(
        modifier = Modifier
            .fillMaxWidth(0.88f)
            .aspectRatio(0.72f)
            .onSizeChanged { cardWidthPx = it.width.toFloat().coerceAtLeast(1f) }
            .graphicsLayer {
                translationX = offsetX.value
                rotationZ = dragProgress * 14f
            }
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .pointerInput(key) {
                detectDragGestures(
                    onDrag = { change, dragAmount ->
                        change.consume()
                        scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                    },
                    onDragEnd = {
                        val threshold = size.width * 0.35f
                        scope.launch {
                            when {
                                offsetX.value > threshold -> {
                                    offsetX.animateTo(size.width.toFloat() * 1.5f, tween(250))
                                    onSwiped(true)
                                }
                                offsetX.value < -threshold -> {
                                    offsetX.animateTo(-size.width.toFloat() * 1.5f, tween(250))
                                    onSwiped(false)
                                }
                                else -> offsetX.animateTo(0f, tween(250))
                            }
                        }
                    }
                )
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            CardImage(item, onOpenGallery = onOpenGallery, modifier = Modifier.fillMaxWidth().weight(1f))
            Column(modifier = Modifier.padding(18.dp)) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                item.notes?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(4.dp))
                    Text(
                        it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2
                    )
                }
                item.link?.takeIf { it.isNotBlank() }?.let {
                    Spacer(Modifier.height(6.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            Icons.Rounded.Link,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(Modifier.size(4.dp))
                        Text(
                            it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        SwipeStamp(
            label = "YES",
            color = LikeGreen,
            alpha = dragProgress.coerceIn(0f, 1f),
            modifier = Modifier.align(Alignment.TopStart).padding(20.dp),
            rotation = -18f
        )
        SwipeStamp(
            label = "NOPE",
            color = NopeRed,
            alpha = (-dragProgress).coerceIn(0f, 1f),
            modifier = Modifier.align(Alignment.TopEnd).padding(20.dp),
            rotation = 18f
        )
    }
}

@Composable
private fun CardImage(item: Item, onOpenGallery: () -> Unit, modifier: Modifier = Modifier) {
    val photos = remember(item.id, item.photoUrls) { item.photoUrls.filter { it.isNotBlank() } }

    if (photos.isEmpty()) {
        Box(
            modifier = modifier.background(Brush.linearGradient(listOf(InkTealBright, Gold))),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                Icons.Rounded.Image,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.85f),
                modifier = Modifier.size(40.dp)
            )
        }
        return
    }

    Box(
        modifier = modifier.pointerInput(item.id) {
            detectTapGestures { onOpenGallery() }
        }
    ) {
        AsyncImage(
            model = photos.first(),
            contentDescription = item.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )
        if (photos.size > 1) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(10.dp)
                    .clip(MaterialTheme.shapes.extraLarge)
                    .background(Color.Black.copy(alpha = 0.45f))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
                Icon(
                    Icons.Rounded.CameraAlt,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                )
                Text(
                    "${photos.size}",
                    color = Color.White,
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}

@Composable
private fun PhotoGalleryDialog(item: Item, onDismiss: () -> Unit) {
    val photos = remember(item.id, item.photoUrls) { item.photoUrls.filter { it.isNotBlank() } }
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black)
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                item {
                    Column(modifier = Modifier.padding(20.dp)) {
                        Text(
                            item.title,
                            style = MaterialTheme.typography.headlineSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        if (photos.isNotEmpty()) {
                            Text(
                                "${photos.size} photo${if (photos.size > 1) "s" else ""}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = Color.White.copy(alpha = 0.7f)
                            )
                        }
                    }
                }
                items(photos) { photoUrl ->
                    AsyncImage(
                        model = photoUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.FillWidth,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 2.dp)
                    )
                }
                item { Spacer(Modifier.height(80.dp)) }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color.Black.copy(alpha = 0.5f), shape = CircleShape)
            ) {
                Icon(Icons.Rounded.Close, contentDescription = "Close", tint = Color.White)
            }
        }
    }
}

@Composable
private fun SwipeStamp(label: String, color: Color, alpha: Float, modifier: Modifier, rotation: Float) {
    if (alpha <= 0.02f) return
    Box(
        modifier = modifier
            .graphicsLayer { rotationZ = rotation; this.alpha = alpha }
            .border(3.dp, color, MaterialTheme.shapes.small)
            .padding(horizontal = 14.dp, vertical = 6.dp)
    ) {
        Text(label, color = color, fontWeight = FontWeight.ExtraBold, style = MaterialTheme.typography.titleMedium)
    }
}
