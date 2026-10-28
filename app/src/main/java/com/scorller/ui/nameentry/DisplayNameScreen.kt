package com.scorller.ui.nameentry

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scorller.data.ServiceLocator

@Composable
fun DisplayNameScreen(
    sessionId: String,
    onConfirmed: () -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<DisplayNameViewModel>(
        factory = remember {
            DisplayNameViewModelFactory(
                ServiceLocator.sessionRepository,
                ServiceLocator.identityManager(context),
                sessionId
            )
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val prefillName by viewModel.prefillName.collectAsStateWithLifecycle()
    var name by remember { mutableStateOf("") }

    LaunchedEffect(prefillName) {
        if (name.isBlank() && prefillName.isNotBlank()) name = prefillName
    }

    LaunchedEffect(uiState) {
        if (uiState is DisplayNameUiState.Confirmed) onConfirmed()
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("What should we call you?", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.padding(12.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Display name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(8.dp))
        Button(
            onClick = { viewModel.confirmName(name) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is DisplayNameUiState.Loading
        ) {
            Text("Continue")
        }
        if (uiState is DisplayNameUiState.Error) {
            Spacer(Modifier.padding(8.dp))
            Text(
                (uiState as DisplayNameUiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
