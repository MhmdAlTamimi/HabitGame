package com.habitgame.app.ui.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.habitgame.app.data.model.EntryType
import com.habitgame.app.ui.viewmodels.MainViewModel

private val PositiveGreen = Color(0xFF2E7D32)
private val NegativeRed = Color(0xFFC62828)
private val WarningOrange = Color(0xFFE65100)

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
                title = { Text("Day Details", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        if (entries.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "No entries recorded this day.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .background(MaterialTheme.colorScheme.background)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                items(entries, key = { it.id }) { entry ->
                    val isPositive = entry.pointDelta >= 0
                    val (icon, iconColor, bgColor) = when (entry.entryType) {
                        EntryType.COMMITMENT_DONE -> Triple(
                            Icons.Default.Check,
                            PositiveGreen,
                            Color(0xFFE8F5E9)
                        )
                        EntryType.TEMPTATION_SLIP -> Triple(
                            Icons.Default.Lock,
                            WarningOrange,
                            Color(0xFFFFF3E0)
                        )
                        EntryType.ADDICTION_SLIP -> Triple(
                            Icons.Default.Warning,
                            NegativeRed,
                            Color(0xFFFFEBEE)
                        )
                    }

                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = bgColor,
                                modifier = Modifier.size(40.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        icon,
                                        contentDescription = null,
                                        tint = iconColor,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                            }

                            Spacer(Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    entry.refName,
                                    style = MaterialTheme.typography.bodyLarge,
                                    fontWeight = FontWeight.Medium
                                )
                                Text(
                                    when (entry.entryType) {
                                        EntryType.COMMITMENT_DONE -> "Commitment completed"
                                        EntryType.TEMPTATION_SLIP -> "Temptation slip"
                                        EntryType.ADDICTION_SLIP -> "Addiction slip"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Surface(
                                color = if (isPositive) PositiveGreen.copy(alpha = 0.1f)
                                else NegativeRed.copy(alpha = 0.1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Text(
                                    "${if (isPositive) "+" else ""}${entry.pointDelta}",
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (isPositive) PositiveGreen else NegativeRed
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
