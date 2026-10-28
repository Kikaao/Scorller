package com.scorller.ui.sessionhub

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scorller.data.ServiceLocator

@Composable
fun SessionHubScreen(
    sessionId: String,
    onAddItem: () -> Unit,
    onSwipe: () -> Unit,
    onResults: () -> Unit
) {
    val viewModel = viewModel<SessionHubViewModel>(
        factory = remember {
            SessionHubViewModelFactory(ServiceLocator.sessionRepository, sessionId)
        }
    )
    val session by viewModel.session.collectAsStateWithLifecycle()

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(session?.title ?: "Session", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.padding(4.dp))
        Text("Join code: ${session?.joinCode ?: "…"}", style = MaterialTheme.typography.bodyLarge)
        Spacer(Modifier.padding(24.dp))
        Button(onClick = onAddItem, modifier = Modifier.fillMaxWidth()) { Text("Add item") }
        Spacer(Modifier.padding(8.dp))
        Button(onClick = onSwipe, modifier = Modifier.fillMaxWidth()) { Text("Start swiping") }
        Spacer(Modifier.padding(8.dp))
        Button(onClick = onResults, modifier = Modifier.fillMaxWidth()) { Text("View results") }
    }
}
