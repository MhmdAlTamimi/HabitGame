package com.habitgame.app.ui.screens.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.habitgame.app.data.model.*
import com.habitgame.app.domain.DateUtils
import com.habitgame.app.ui.viewmodels.MainViewModel

private val PositiveGreen = Color(0xFF2E7D32)
private val NegativeRed = Color(0xFFC62828)
private val LockedRed = Color(0xFFE53935)
private val UnlockedGreen = Color(0xFF43A047)

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
                title = {
                    Text(
                        "Habit RPG",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    actionIconContentColor = MaterialTheme.colorScheme.onPrimary
                ),
                actions = {
                    IconButton(onClick = onNavigateToHistory) {
                        Icon(Icons.Default.History, "History")
                    }
                    IconButton(onClick = onNavigateToSettings) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
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
                SectionHeader(title = "Commitments", count = commitments.size)
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
                SectionHeader(title = "Temptations", count = temptations.size)
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
                    totalScore = c.totalScore,
                    onSlip = { viewModel.recordTemptationSlip(temptation) },
                    onUndoSlip = { viewModel.removeTemptationSlip(temptation) }
                )
            }

            item(key = "addiction_slip") {
                val addictionSlipCount = remember(todayActions) {
                    todayActions.count { it.actionType == ActionType.ADDICTION_SLIP }
                }
                AddictionSlipSection(
                    addictionName = c.addictionName,
                    slipCount = addictionSlipCount,
                    onSlip = { viewModel.recordAddictionSlip() },
                    onUndoSlip = { viewModel.removeAddictionSlip() }
                )
            }

            item(key = "bottom_spacer") {
                Spacer(Modifier.height(8.dp))
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
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.verticalGradient(
                        colors = listOf(
                            Color(0xFF4A148C),
                            Color(0xFF7B1FA2)
                        )
                    ),
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "TOTAL SCORE",
                    style = MaterialTheme.typography.labelMedium,
                    color = Color.White.copy(alpha = 0.7f),
                    letterSpacing = 2.sp
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    totalScore.toString(),
                    fontSize = 56.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(Modifier.height(8.dp))

                Surface(
                    color = if (dailyPoints >= 0) PositiveGreen.copy(alpha = 0.3f)
                    else NegativeRed.copy(alpha = 0.3f),
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        "Today  ${if (dailyPoints >= 0) "+" else ""}$dailyPoints",
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 6.dp),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Color.White
                    )
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Day closes at ${DateUtils.formatTime(dayCloseHour, dayCloseMinute)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String, count: Int) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(Modifier.width(8.dp))
        Surface(
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        ) {
            Text(
                count.toString(),
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimaryContainer
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
    val isDone = action != null
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isDone) Color(0xFFE8F5E9) else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (commitment.type == CommitmentType.BINARY) {
                Surface(
                    onClick = onToggle,
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDone) PositiveGreen else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (isDone) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = "Done",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            } else {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isDone) PositiveGreen.copy(alpha = 0.15f)
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "${action?.count ?: 0}",
                            fontWeight = FontWeight.Bold,
                            color = if (isDone) PositiveGreen else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    commitment.name,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                val pointsText = if (commitment.type == CommitmentType.COUNT) {
                    val currentCount = action?.count ?: 0
                    "+${commitment.pointValue} each  |  ${currentCount * commitment.pointValue} pts earned"
                } else {
                    "+${commitment.pointValue} pts"
                }
                Text(
                    pointsText,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            if (commitment.type == CommitmentType.COUNT) {
                val currentCount = action?.count ?: 0
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilledIconButton(
                        onClick = { onCountChange(currentCount - 1) },
                        enabled = currentCount > 0,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Icon(Icons.Default.Remove, "Decrease", modifier = Modifier.size(16.dp))
                    }
                    Text(
                        "$currentCount/${commitment.maxCount}",
                        modifier = Modifier.padding(horizontal = 8.dp),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold
                    )
                    FilledIconButton(
                        onClick = { onCountChange(currentCount + 1) },
                        enabled = currentCount < commitment.maxCount,
                        modifier = Modifier.size(32.dp),
                        colors = IconButtonDefaults.filledIconButtonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(Icons.Default.Add, "Increase", modifier = Modifier.size(16.dp))
                    }
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
    totalScore: Int,
    onSlip: () -> Unit,
    onUndoSlip: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isUnlocked) Color(0xFFE8F5E9) else Color(0xFFFFF3E0)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = if (isUnlocked) UnlockedGreen else LockedRed,
                    modifier = Modifier.size(40.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            if (isUnlocked) Icons.Default.LockOpen else Icons.Default.Lock,
                            contentDescription = if (isUnlocked) "Unlocked" else "Locked",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        temptation.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Medium
                    )
                    if (isUnlocked) {
                        Text(
                            "Unlocked — no penalty",
                            style = MaterialTheme.typography.bodySmall,
                            color = UnlockedGreen
                        )
                    } else {
                        val remaining = temptation.unlockThreshold - totalScore
                        Text(
                            "$remaining more pts to unlock  |  -${temptation.slipPenalty} penalty",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            if (!isUnlocked) {
                Spacer(Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { (totalScore.toFloat() / temptation.unlockThreshold).coerceIn(0f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.tertiary,
                    trackColor = MaterialTheme.colorScheme.surfaceVariant,
                )
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    if (slipCount > 0) {
                        TextButton(onClick = onUndoSlip) {
                            Text("Undo ($slipCount)")
                        }
                        Spacer(Modifier.width(4.dp))
                    }
                    Button(
                        onClick = onSlip,
                        colors = ButtonDefaults.buttonColors(containerColor = LockedRed),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text("Slipped  -${temptation.slipPenalty}")
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
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.Warning,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(28.dp)
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = onSlip,
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "I slipped on $addictionName",
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
            if (slipCount > 0) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "Slipped $slipCount time(s) today",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onErrorContainer
                )
                TextButton(onClick = onUndoSlip) {
                    Text("Undo last slip")
                }
            }
        }
    }
}
