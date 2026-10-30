package com.scorller.ui.nameentry

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.scorller.data.ServiceLocator
import com.scorller.ui.components.ErrorBanner
import com.scorller.ui.theme.Gold
import com.scorller.ui.theme.InkTealBright
import com.scorller.ui.theme.screenBackgroundBrush

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
        modifier = Modifier
            .fillMaxSize()
            .background(screenBackgroundBrush())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        NameAvatar(name)
        Spacer(Modifier.height(20.dp))
        Text(
            "Who's swiping?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(Modifier.height(6.dp))
        Text(
            "Your name labels your votes for the group",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(24.dp))
        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            placeholder = { Text("Your name") },
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(16.dp))
        Button(
            onClick = { viewModel.confirmName(name) },
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = MaterialTheme.shapes.extraLarge,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ),
            enabled = uiState !is DisplayNameUiState.Loading
        ) {
            if (uiState is DisplayNameUiState.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(20.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Text("Continue", fontWeight = FontWeight.SemiBold)
            }
        }
        if (uiState is DisplayNameUiState.Error) {
            Spacer(Modifier.height(16.dp))
            ErrorBanner((uiState as DisplayNameUiState.Error).message)
        }
    }
}

@Composable
private fun NameAvatar(name: String) {
    val initial = name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
    Box(
        modifier = Modifier
            .size(84.dp)
            .background(
                Brush.linearGradient(listOf(InkTealBright, Gold)),
                shape = MaterialTheme.shapes.extraLarge
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            initial,
            style = MaterialTheme.typography.displaySmall,
            color = Color.White
        )
    }
}
