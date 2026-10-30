package com.scorller.ui.additem

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Notes
import androidx.compose.material.icons.rounded.AddLink
import androidx.compose.material.icons.rounded.AddPhotoAlternate
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Link
import androidx.compose.material.icons.rounded.Title
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.scorller.data.ServiceLocator
import com.scorller.data.ShareIntentHolder
import com.scorller.ui.components.ErrorBanner
import com.scorller.ui.theme.screenBackgroundBrush

@Composable
fun AddItemScreen(
    sessionId: String,
    onItemAdded: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<AddItemViewModel>(
        factory = remember {
            AddItemViewModelFactory(
                ServiceLocator.sessionRepository,
                ServiceLocator.identityManager(context),
                ServiceLocator.linkPreviewFetcher,
                sessionId
            )
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val previewState by viewModel.previewState.collectAsStateWithLifecycle()

    var title by rememberSaveable { mutableStateOf("") }
    var link by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val photoUrls = remember { mutableListOf("").toMutableStateList() }

    LaunchedEffect(Unit) {
        val shared = ShareIntentHolder.consume() ?: return@LaunchedEffect
        link = shared
        viewModel.fetchPreview(shared)
    }

    LaunchedEffect(uiState) {
        if (uiState is AddItemUiState.Saved) {
            onItemAdded()
            viewModel.resetState()
        }
    }

    LaunchedEffect(previewState) {
        val found = (previewState as? PreviewFetchState.Found)?.preview ?: return@LaunchedEffect
        if (title.isBlank()) found.title?.let { title = it }
        if (notes.isBlank()) found.description?.let { notes = it }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackgroundBrush())
            .padding(24.dp)
    ) {
        item {
            Text(
                "What's on the table?",
                style = MaterialTheme.typography.headlineMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Paste a link and we'll try to pull in a photo and description",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(24.dp))

            OutlinedTextField(
                value = link,
                onValueChange = {
                    link = it
                    if (previewState !is PreviewFetchState.Idle) viewModel.clearPreview()
                },
                placeholder = { Text("Paste a link (optional)") },
                leadingIcon = { Icon(Icons.Rounded.Link, contentDescription = null) },
                trailingIcon = {
                    when (previewState) {
                        is PreviewFetchState.Loading -> CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp
                        )
                        else -> if (link.isNotBlank()) {
                            IconButton(onClick = { viewModel.fetchPreview(link) }) {
                                Icon(Icons.Rounded.AutoAwesome, contentDescription = "Fetch preview")
                            }
                        }
                    }
                },
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            when (val state = previewState) {
                is PreviewFetchState.Found -> {
                    Spacer(Modifier.height(10.dp))
                    LinkPreviewCard(
                        title = state.preview.title,
                        imageUrls = state.preview.imageUrls,
                        onClear = { viewModel.clearPreview() }
                    )
                }
                is PreviewFetchState.NotFound -> {
                    Spacer(Modifier.height(10.dp))
                    Text(
                        "Couldn't fetch a preview — add details manually below",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {}
            }

            Spacer(Modifier.height(20.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                placeholder = { Text("Title") },
                leadingIcon = { Icon(Icons.Rounded.Title, contentDescription = null) },
                shape = MaterialTheme.shapes.medium,
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(20.dp))
            Text(
                "PHOTOS",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
        }
        items(photoUrls.size) { index ->
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = photoUrls[index],
                    onValueChange = { photoUrls[index] = it },
                    placeholder = { Text("Photo URL") },
                    leadingIcon = { Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null) },
                    shape = MaterialTheme.shapes.medium,
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            TextButton(onClick = { photoUrls.add("") }) {
                Text("+ Add another photo", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold)
            }
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                placeholder = { Text("Notes (optional)") },
                leadingIcon = { Icon(Icons.AutoMirrored.Rounded.Notes, contentDescription = null) },
                shape = MaterialTheme.shapes.medium,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(24.dp))
            Button(
                onClick = { viewModel.addItem(title, photoUrls, link, notes) },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = MaterialTheme.shapes.extraLarge,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondary,
                    contentColor = MaterialTheme.colorScheme.onSecondary
                ),
                enabled = uiState !is AddItemUiState.Saving
            ) {
                if (uiState is AddItemUiState.Saving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = MaterialTheme.colorScheme.onSecondary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(Icons.Rounded.AddLink, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.size(8.dp))
                    Text("Add to deck", fontWeight = FontWeight.SemiBold)
                }
            }
            if (uiState is AddItemUiState.Error) {
                Spacer(Modifier.height(16.dp))
                ErrorBanner((uiState as AddItemUiState.Error).message)
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun LinkPreviewCard(title: String?, imageUrls: List<String>, onClear: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.medium)
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                title ?: "Preview found",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onClear, modifier = Modifier.size(28.dp).clip(CircleShape)) {
                Icon(Icons.Rounded.Close, contentDescription = "Clear preview", modifier = Modifier.size(16.dp))
            }
        }
        if (imageUrls.isEmpty()) {
            Text(
                "No photos found on this page",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            Text(
                "${imageUrls.size} photo${if (imageUrls.size > 1) "s" else ""} will be added",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(Modifier.height(8.dp))
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(imageUrls) { url ->
                    AsyncImage(
                        model = url,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(64.dp)
                            .clip(MaterialTheme.shapes.small)
                            .background(MaterialTheme.colorScheme.surface)
                    )
                }
            }
        }
    }
}
