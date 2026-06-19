package com.habitgame.app.ui.screens.history

import androidx.compose.foundation.clickable
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
import com.habitgame.app.data.model.DailyLog
import com.habitgame.app.domain.DateUtils
import com.habitgame.app.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onLogClick: (Int) -> Unit
) {
    val closedLogs by viewModel.closedLogs.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (closedLogs.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
                Text("No completed days yet.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(closedLogs, key = { "log_${it.id}" }) { log ->
                    DailyLogCard(log = log, onClick = { onLogClick(log.id) })
                }
            }
        }
    }
}

@Composable
private fun DailyLogCard(log: DailyLog, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    DateUtils.formatDate(log.date),
                    style = MaterialTheme.typography.bodyLarge
                )
                Text(
                    "Daily: ${if (log.dailyPoints >= 0) "+" else ""}${log.dailyPoints}",
                    style = MaterialTheme.typography.bodySmall,
                    color = if (log.dailyPoints >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
                )
            }
            Text(
                "Total: ${log.totalAfterClose}",
                style = MaterialTheme.typography.titleMedium
            )
        }
    }
}
