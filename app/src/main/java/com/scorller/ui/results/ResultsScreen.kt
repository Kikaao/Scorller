package com.scorller.ui.results

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.EmojiEvents
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.scorller.data.ServiceLocator
import com.scorller.ui.theme.screenBackgroundBrush
import com.scorller.ui.theme.Gold
import com.scorller.ui.theme.GoldLight
import com.scorller.ui.theme.InkTealBright

@Composable
fun ResultsScreen(sessionId: String) {
    val viewModel = viewModel<ResultsViewModel>(
        factory = remember {
            ResultsViewModelFactory(ServiceLocator.sessionRepository, sessionId)
        }
    )
    val rankedItems by viewModel.rankedItems.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackgroundBrush())
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(24.dp))
        Text(
            "Results",
            style = MaterialTheme.typography.headlineLarge,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            "Updates live as votes come in",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(20.dp))

        if (rankedItems.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No items yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            return@Column
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            itemsIndexed(rankedItems, key = { _, ranked -> ranked.item.id }) { index, ranked ->
                ResultRow(rank = index + 1, ranked = ranked)
            }
            item { Spacer(Modifier.height(24.dp)) }
        }
    }
}

@Composable
private fun ResultRow(rank: Int, ranked: RankedItem) {
    val item = ranked.item
    val ratio = if (ranked.totalParticipants > 0) {
        item.yesCount.toFloat() / ranked.totalParticipants.toFloat()
    } else 0f
    val unanimous = item.isUnanimous(ranked.totalParticipants)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .background(MaterialTheme.colorScheme.surface)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        RankBadge(rank)
        Spacer(Modifier.size(12.dp))
        Thumbnail(item.photoUrls.firstOrNull { it.isNotBlank() })
        Spacer(Modifier.size(14.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    item.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                if (unanimous) {
                    Spacer(Modifier.size(6.dp))
                    UnanimousBadge()
                }
            }
            Spacer(Modifier.height(8.dp))
            YesRatioBar(ratio)
            Spacer(Modifier.height(4.dp))
            Text(
                "${item.yesCount} / ${ranked.totalParticipants} yes",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun RankBadge(rank: Int) {
    val brush = when (rank) {
        1 -> Brush.linearGradient(listOf(Gold, GoldLight))
        2 -> Brush.linearGradient(listOf(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.outline))
        3 -> Brush.linearGradient(listOf(MaterialTheme.colorScheme.primary, InkTealBright))
        else -> Brush.linearGradient(
            listOf(MaterialTheme.colorScheme.surfaceVariant, MaterialTheme.colorScheme.surfaceVariant)
        )
    }
    val textColor = if (rank <= 3) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.onSurfaceVariant
    Box(
        modifier = Modifier
            .size(32.dp)
            .clip(MaterialTheme.shapes.small)
            .background(brush),
        contentAlignment = Alignment.Center
    ) {
        Text("$rank", color = textColor, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
    }
}

@Composable
private fun Thumbnail(url: String?) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (url != null) {
            AsyncImage(
                model = url,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            Icon(
                Icons.Rounded.Image,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun YesRatioBar(ratio: Float) {
    val animatedRatio by animateFloatAsState(
        targetValue = ratio.coerceIn(0f, 1f),
        animationSpec = tween(400),
        label = "yesRatio"
    )
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(MaterialTheme.shapes.extraSmall)
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth(animatedRatio)
                .fillMaxHeight()
                .clip(MaterialTheme.shapes.extraSmall)
                .background(Brush.horizontalGradient(listOf(InkTealBright, MaterialTheme.colorScheme.primary)))
        )
    }
}

@Composable
private fun UnanimousBadge() {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.extraSmall)
            .background(Gold.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Rounded.EmojiEvents, contentDescription = null, tint = Gold, modifier = Modifier.size(12.dp))
        Spacer(Modifier.size(2.dp))
        Text("Unanimous", style = MaterialTheme.typography.labelMedium, color = Gold)
    }
}
