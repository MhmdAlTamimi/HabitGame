package com.habitgame.app.ui.screens.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.habitgame.app.data.model.CommitmentType
import com.habitgame.app.ui.viewmodels.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onNavigateBack: () -> Unit,
    onChallengeEnded: () -> Unit
) {
    val challenge by viewModel.challenge.collectAsState()
    val commitments by viewModel.commitments.collectAsState()
    val temptations by viewModel.temptations.collectAsState()

    val c = challenge
    if (c == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    var penaltyText by remember(c.id) { mutableStateOf(c.addictionPenaltyPercent.toString()) }
    var hourText by remember(c.id) { mutableStateOf(c.dayCloseHour.toString()) }
    var minuteText by remember(c.id) { mutableStateOf(c.dayCloseMinute.toString()) }

    var showAddCommitment by remember { mutableStateOf(false) }
    var showAddTemptation by remember { mutableStateOf(false) }
    var showEndConfirm by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
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
            item(key = "addiction_section") {
                Text("Addiction", style = MaterialTheme.typography.titleMedium)
                Text(c.addictionName, style = MaterialTheme.typography.bodyLarge)
                Spacer(Modifier.height(4.dp))
                OutlinedTextField(
                    value = penaltyText,
                    onValueChange = { penaltyText = it },
                    label = { Text("Penalty %") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Button(
                    onClick = {
                        penaltyText.toIntOrNull()?.let { viewModel.updatePenaltyPercent(it) }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Penalty %")
                }
            }

            item(key = "dayclose_section") {
                Spacer(Modifier.height(8.dp))
                Text("Day-Close Time", style = MaterialTheme.typography.titleMedium)
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { hourText = it },
                        label = { Text("Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                    Text(":")
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { minuteText = it },
                        label = { Text("Min") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        singleLine = true
                    )
                }
                Button(
                    onClick = {
                        val h = hourText.toIntOrNull() ?: return@Button
                        val m = minuteText.toIntOrNull() ?: return@Button
                        if (h in 0..23 && m in 0..59) viewModel.updateDayCloseTime(h, m)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Update Day-Close Time")
                }
            }

            item(key = "commitments_header") {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Commitments", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { showAddCommitment = true }) { Text("+ Add") }
                }
            }

            items(commitments, key = { "commitment_${it.id}" }) { commitment ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(commitment.name)
                            Text(
                                "+${commitment.pointValue} pts (${commitment.type.name}${if (commitment.type == CommitmentType.COUNT) ", max ${commitment.maxCount}" else ""})",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = { viewModel.deactivateCommitment(commitment) }) {
                            Text("Remove")
                        }
                    }
                }
            }

            item(key = "temptations_header") {
                Spacer(Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Temptations", style = MaterialTheme.typography.titleMedium)
                    TextButton(onClick = { showAddTemptation = true }) { Text("+ Add") }
                }
            }

            items(temptations, key = { "temptation_${it.id}" }) { temptation ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(temptation.name)
                            Text(
                                "Unlocks at ${temptation.unlockThreshold}, penalty ${temptation.slipPenalty}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = { viewModel.deactivateTemptation(temptation) }) {
                            Text("Remove")
                        }
                    }
                }
            }

            item(key = "end_challenge") {
                Spacer(Modifier.height(24.dp))
                OutlinedButton(
                    onClick = { showEndConfirm = true },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) {
                    Text("End Challenge")
                }
            }
        }
    }

    if (showAddCommitment) {
        AddCommitmentDialog(
            onDismiss = { showAddCommitment = false },
            onAdd = { name, points, type, maxCount ->
                viewModel.addCommitment(name, points, type, maxCount)
                showAddCommitment = false
            }
        )
    }

    if (showAddTemptation) {
        AddTemptationDialog(
            onDismiss = { showAddTemptation = false },
            onAdd = { name, threshold, penalty ->
                viewModel.addTemptation(name, threshold, penalty)
                showAddTemptation = false
            }
        )
    }

    if (showEndConfirm) {
        AlertDialog(
            onDismissRequest = { showEndConfirm = false },
            title = { Text("End Challenge?") },
            text = { Text("This will end your current challenge. You can start a new one afterward.") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.endChallenge()
                    showEndConfirm = false
                    onChallengeEnded()
                }) { Text("End") }
            },
            dismissButton = {
                TextButton(onClick = { showEndConfirm = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun AddCommitmentDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, CommitmentType, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var isBinary by remember { mutableStateOf(true) }
    var maxCount by remember { mutableStateOf("3") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Commitment") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = points,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) points = it },
                    label = { Text("Point value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    FilterChip(selected = isBinary, onClick = { isBinary = true }, label = { Text("Binary") })
                    Spacer(Modifier.width(8.dp))
                    FilterChip(selected = !isBinary, onClick = { isBinary = false }, label = { Text("Count") })
                }
                if (!isBinary) {
                    OutlinedTextField(
                        value = maxCount,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) maxCount = it },
                        label = { Text("Max count") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val p = points.toIntOrNull() ?: return@TextButton
                    if (name.isBlank() || p <= 0) return@TextButton
                    val mc = if (isBinary) 1 else (maxCount.toIntOrNull() ?: 1).coerceAtLeast(1)
                    onAdd(name, p, if (isBinary) CommitmentType.BINARY else CommitmentType.COUNT, mc)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun AddTemptationDialog(
    onDismiss: () -> Unit,
    onAdd: (String, Int, Int) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("") }
    var penalty by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Temptation") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = threshold,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) threshold = it },
                    label = { Text("Unlock threshold") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                OutlinedTextField(
                    value = penalty,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) penalty = it },
                    label = { Text("Slip penalty") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val t = threshold.toIntOrNull() ?: return@TextButton
                    val p = penalty.toIntOrNull() ?: return@TextButton
                    if (name.isBlank() || p <= 0) return@TextButton
                    onAdd(name, t, p)
                }
            ) { Text("Add") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}
