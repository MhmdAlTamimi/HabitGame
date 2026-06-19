package com.habitgame.app.ui.screens.history

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.habitgame.app.data.model.EntryType
import com.habitgame.app.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryDetailScreen(
    viewModel: MainViewModel,
    logId: Int,
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(logId) {
        viewModel.loadLogEntries(logId)
    }

    val entries by viewModel.logEntries.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Day Details") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("No entries recorded this day.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(entries) { entry ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(entry.refName, style = MaterialTheme.typography.bodyLarge)
                                Text(
                                    when (entry.entryType) {
                                        EntryType.COMMITMENT_DONE -> "Commitment completed"
                                        EntryType.TEMPTATION_SLIP -> "Temptation slip"
                                        EntryType.ADDICTION_SLIP -> "Addiction slip"
                                    },
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                            Text(
                                "${if (entry.pointDelta >= 0) "+" else ""}${entry.pointDelta}",
                                style = MaterialTheme.typography.titleMedium,
                                color = if (entry.pointDelta >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                            )
                        }
                    }
                }
            }
        }
    }
}
