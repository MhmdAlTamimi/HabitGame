package com.habitgame.app.ui.screens.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitgame.app.data.model.*
import com.habitgame.app.domain.DateUtils
import com.habitgame.app.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: MainViewModel,
    onNavigateToHistory: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val challenge by viewModel.challenge.collectAsState()
    val commitments by viewModel.commitments.collectAsState()
    val temptations by viewModel.temptations.collectAsState()
    val dailyPoints by viewModel.dailyPoints.collectAsState()
    val todayActions by viewModel.todayActions.collectAsState()
    val isReady by viewModel.isReady.collectAsState()

    val c = challenge

    if (c == null || !isReady) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Habit RPG") },
                actions = {
                    TextButton(onClick = onNavigateToHistory) { Text("History") }
                    TextButton(onClick = onNavigateToSettings) { Text("Settings") }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
            contentPadding = PaddingValues(bottom = 16.dp)
        ) {
            item(key = "score_header") {
                ScoreHeader(
                    totalScore = c.totalScore,
                    dailyPoints = dailyPoints,
                    dayCloseHour = c.dayCloseHour,
                    dayCloseMinute = c.dayCloseMinute
                )
            }

            item(key = "commitments_header") {
                Text(
                    "Commitments",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(commitments, key = { "commitment_${it.id}" }) { commitment ->
                val action = remember(todayActions, commitment.id) {
                    todayActions.find {
                        it.actionType == ActionType.COMMITMENT && it.refId == commitment.id
                    }
                }
                CommitmentItem(
                    commitment = commitment,
                    action = action,
                    onToggle = { viewModel.toggleCommitment(commitment) },
                    onCountChange = { count -> viewModel.setCommitmentCount(commitment, count) }
                )
            }

            item(key = "temptations_header") {
                Text(
                    "Temptations",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            items(temptations, key = { "temptation_${it.id}" }) { temptation ->
                val isUnlocked = remember(c.totalScore, temptation.unlockThreshold) {
                    c.totalScore >= temptation.unlockThreshold
                }
                val slipCount = remember(todayActions, temptation.id) {
                    todayActions.count {
                        it.actionType == ActionType.TEMPTATION_SLIP && it.refId == temptation.id
                    }
                }
                TemptationItem(
                    temptation = temptation,
                    isUnlocked = isUnlocked,
                    slipCount = slipCount,
                    onSlip = { viewModel.recordTemptationSlip(temptation) },
                    onUndoSlip = { viewModel.removeTemptationSlip(temptation) }
                )
            }

            item(key = "addiction_slip") {
                val addictionSlipCount = remember(todayActions) {
                    todayActions.count { it.actionType == ActionType.ADDICTION_SLIP }
                }
                Spacer(Modifier.height(8.dp))
                AddictionSlipSection(
                    addictionName = c.addictionName,
                    slipCount = addictionSlipCount,
                    onSlip = { viewModel.recordAddictionSlip() },
                    onUndoSlip = { viewModel.removeAddictionSlip() }
                )
            }
        }
    }
}

@Composable
private fun ScoreHeader(
    totalScore: Int,
    dailyPoints: Int,
    dayCloseHour: Int,
    dayCloseMinute: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Total Score", style = MaterialTheme.typography.labelLarge)
            Text(
                totalScore.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Today: ${if (dailyPoints >= 0) "+" else ""}$dailyPoints",
                style = MaterialTheme.typography.titleMedium,
                color = if (dailyPoints >= 0) Color(0xFF2E7D32) else Color(0xFFC62828)
            )
            Spacer(Modifier.height(4.dp))
            Text(
                "Day closes at ${DateUtils.formatTime(dayCloseHour, dayCloseMinute)}",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}

@Composable
private fun CommitmentItem(
    commitment: Commitment,
    action: TodayAction?,
    onToggle: () -> Unit,
    onCountChange: (Int) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(commitment.name, style = MaterialTheme.typography.bodyLarge)
                val pointsText = if (commitment.type == CommitmentType.COUNT) {
                    val currentCount = action?.count ?: 0
                    "+${commitment.pointValue}/ea (${currentCount * commitment.pointValue} pts)"
                } else {
                    "+${commitment.pointValue} pts"
                }
                Text(pointsText, style = MaterialTheme.typography.bodySmall)
            }

            if (commitment.type == CommitmentType.BINARY) {
                Checkbox(
                    checked = action != null,
                    onCheckedChange = { onToggle() }
                )
            } else {
                val currentCount = action?.count ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledTonalButton(
                        onClick = { onCountChange(currentCount - 1) },
                        enabled = currentCount > 0,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("-")
                    }
                    Text(
                        "$currentCount",
                        modifier = Modifier.padding(horizontal = 12.dp),
                        style = MaterialTheme.typography.titleMedium
                    )
                    FilledTonalButton(
                        onClick = { onCountChange(currentCount + 1) },
                        enabled = currentCount < commitment.maxCount,
                        contentPadding = PaddingValues(0.dp),
                        modifier = Modifier.size(36.dp)
                    ) {
                        Text("+")
                    }
                    Text(
                        "/${commitment.maxCount}",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 4.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun TemptationItem(
    temptation: Temptation,
    isUnlocked: Boolean,
    slipCount: Int,
    onSlip: () -> Unit,
    onUndoSlip: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(temptation.name, style = MaterialTheme.typography.bodyLarge)
                if (isUnlocked) {
                    Text(
                        "UNLOCKED",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFF2E7D32)
                    )
                } else {
                    Text(
                        "LOCKED (unlocks at ${temptation.unlockThreshold})",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color(0xFFC62828)
                    )
                }
            }

            if (!isUnlocked) {
                Column(horizontalAlignment = Alignment.End) {
                    Button(
                        onClick = onSlip,
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) {
                        Text("Slip (-${temptation.slipPenalty})")
                    }
                    if (slipCount > 0) {
                        TextButton(onClick = onUndoSlip) {
                            Text("Undo ($slipCount slips)")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AddictionSlipSection(
    addictionName: String,
    slipCount: Int,
    onSlip: () -> Unit,
    onUndoSlip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Button(
                onClick = onSlip,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("I slipped on $addictionName")
            }
            if (slipCount > 0) {
                Text(
                    "Slipped $slipCount time(s) today",
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
                TextButton(onClick = onUndoSlip) {
                    Text("Undo last slip")
                }
            }
        }
    }
}
