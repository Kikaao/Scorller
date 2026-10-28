package com.scorller.ui.results

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
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
fun ResultsScreen(sessionId: String) {
    val viewModel = viewModel<ResultsViewModel>(
        factory = remember {
            ResultsViewModelFactory(ServiceLocator.sessionRepository, sessionId)
        }
    )
    val rankedItems by viewModel.rankedItems.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Results", style = MaterialTheme.typography.headlineMedium)
        if (rankedItems.isEmpty()) {
            Text("No items yet.", modifier = Modifier.padding(top = 16.dp))
            return@Column
        }
        LazyColumn(modifier = Modifier.fillMaxSize().padding(top = 12.dp)) {
            items(rankedItems, key = { it.item.id }) { ranked ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp)) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ranked.item.title, style = MaterialTheme.typography.titleMedium)
                            if (ranked.item.isUnanimous(ranked.totalParticipants)) {
                                AssistChip(onClick = {}, label = { Text("Unanimous") })
                            }
                        }
                        Text(
                            "${ranked.item.yesCount} / ${ranked.totalParticipants} yes",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
