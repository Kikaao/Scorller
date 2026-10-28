package com.scorller.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scorller.data.ServiceLocator

@Composable
fun HomeScreen(
    onSessionReady: (sessionId: String) -> Unit
) {
    val context = LocalContext.current
    val viewModel = viewModel<HomeViewModel>(
        factory = remember {
            HomeViewModelFactory(
                ServiceLocator.sessionRepository,
                ServiceLocator.identityManager(context)
            )
        }
    )
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var title by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(uiState) {
        val state = uiState
        if (state is HomeUiState.Success) {
            onSessionReady(state.sessionId)
            viewModel.resetState()
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Scorller", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.padding(12.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("New session title") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(4.dp))
        Button(
            onClick = { viewModel.createSession(title) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is HomeUiState.Loading
        ) {
            Text("Create session")
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 24.dp))

        OutlinedTextField(
            value = code,
            onValueChange = { code = it.uppercase() },
            label = { Text("Join code") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.padding(4.dp))
        Button(
            onClick = { viewModel.joinSession(code) },
            modifier = Modifier.fillMaxWidth(),
            enabled = uiState !is HomeUiState.Loading
        ) {
            Text("Join session")
        }

        if (uiState is HomeUiState.Error) {
            Spacer(Modifier.padding(8.dp))
            Text(
                (uiState as HomeUiState.Error).message,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}
