package com.scorller.ui.additem

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scorller.data.ServiceLocator

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
                sessionId
            )
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    var title by rememberSaveable { mutableStateOf("") }
    var link by rememberSaveable { mutableStateOf("") }
    var notes by rememberSaveable { mutableStateOf("") }
    val photoUrls = remember { mutableListOf("").toMutableStateList() }

    LaunchedEffect(uiState) {
        if (uiState is AddItemUiState.Saved) {
            onItemAdded()
            viewModel.resetState()
        }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize().padding(24.dp)
    ) {
        item {
            Text("Add an item", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.padding(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Title") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.padding(8.dp))
            Text("Photo URLs (optional)", style = MaterialTheme.typography.labelLarge)
        }
        items(photoUrls.size) { index ->
            Row(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                OutlinedTextField(
                    value = photoUrls[index],
                    onValueChange = { photoUrls[index] = it },
                    label = { Text("Photo URL ${index + 1}") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        item {
            TextButton(onClick = { photoUrls.add("") }) {
                Text("+ Add another photo URL")
            }
            Spacer(Modifier.padding(8.dp))
            OutlinedTextField(
                value = link,
                onValueChange = { link = it },
                label = { Text("Link (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.padding(8.dp))
            OutlinedTextField(
                value = notes,
                onValueChange = { notes = it },
                label = { Text("Notes (optional)") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.padding(16.dp))
            Button(
                onClick = { viewModel.addItem(title, photoUrls, link, notes) },
                modifier = Modifier.fillMaxWidth(),
                enabled = uiState !is AddItemUiState.Saving
            ) {
                Text("Add item")
            }
            if (uiState is AddItemUiState.Error) {
                Spacer(Modifier.padding(8.dp))
                Text(
                    (uiState as AddItemUiState.Error).message,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
