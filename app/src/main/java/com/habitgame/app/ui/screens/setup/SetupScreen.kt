package com.habitgame.app.ui.screens.setup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.KeyboardOptions
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
fun SetupScreen(
    viewModel: MainViewModel,
    onChallengeCreated: () -> Unit
) {
    var step by remember { mutableIntStateOf(0) }

    var addictionName by remember { mutableStateOf("") }
    var penaltyPercent by remember { mutableStateOf("10") }

    var commitmentsList by remember {
        mutableStateOf(listOf<SetupCommitment>())
    }
    var temptationsList by remember {
        mutableStateOf(listOf<SetupTemptation>())
    }

    var dayCloseHour by remember { mutableIntStateOf(23) }
    var dayCloseMinute by remember { mutableIntStateOf(0) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Setup Challenge - Step ${step + 1}/6") }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            when (step) {
                0 -> StepAddiction(
                    addictionName = addictionName,
                    onNameChange = { addictionName = it },
                    penaltyPercent = penaltyPercent,
                    onPercentChange = { penaltyPercent = it },
                    onNext = { if (addictionName.isNotBlank() && penaltyPercent.toIntOrNull() != null) step = 1 }
                )
                1 -> StepCommitments(
                    commitments = commitmentsList,
                    onAdd = { commitmentsList = commitmentsList + it },
                    onRemove = { idx -> commitmentsList = commitmentsList.filterIndexed { i, _ -> i != idx } },
                    onNext = { if (commitmentsList.isNotEmpty()) step = 2 },
                    onBack = { step = 0 }
                )
                2 -> StepTemptations(
                    temptations = temptationsList,
                    onAdd = { temptationsList = temptationsList + it },
                    onRemove = { idx -> temptationsList = temptationsList.filterIndexed { i, _ -> i != idx } },
                    onNext = { if (temptationsList.isNotEmpty()) step = 3 },
                    onBack = { step = 1 }
                )
                3 -> StepDayClose(
                    hour = dayCloseHour,
                    minute = dayCloseMinute,
                    onHourChange = { dayCloseHour = it },
                    onMinuteChange = { dayCloseMinute = it },
                    onNext = { step = 4 },
                    onBack = { step = 2 }
                )
                4 -> StepReview(
                    addictionName = addictionName,
                    penaltyPercent = penaltyPercent.toIntOrNull() ?: 10,
                    commitments = commitmentsList,
                    temptations = temptationsList,
                    dayCloseHour = dayCloseHour,
                    dayCloseMinute = dayCloseMinute,
                    onStart = {
                        viewModel.createChallenge(
                            addictionName = addictionName,
                            penaltyPercent = penaltyPercent.toIntOrNull() ?: 10,
                            dayCloseHour = dayCloseHour,
                            dayCloseMinute = dayCloseMinute,
                            commitments = commitmentsList.map { c ->
                                c.name to Triple(c.pointValue, c.type, c.maxCount)
                            },
                            temptations = temptationsList.map { t ->
                                Triple(t.name, t.threshold, t.penalty)
                            }
                        )
                        onChallengeCreated()
                    },
                    onBack = { step = 3 }
                )
            }
        }
    }
}

data class SetupCommitment(
    val name: String,
    val pointValue: Int,
    val type: CommitmentType,
    val maxCount: Int
)

data class SetupTemptation(
    val name: String,
    val threshold: Int,
    val penalty: Int
)

@Composable
private fun StepAddiction(
    addictionName: String,
    onNameChange: (String) -> Unit,
    penaltyPercent: String,
    onPercentChange: (String) -> Unit,
    onNext: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Name your Addiction", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = addictionName,
            onValueChange = onNameChange,
            label = { Text("Addiction name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Text("Set Penalty Percentage", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = penaltyPercent,
            onValueChange = { if (it.all { c -> c.isDigit() }) onPercentChange(it) },
            label = { Text("Penalty % (e.g. 10)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        Spacer(Modifier.weight(1f))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = addictionName.isNotBlank() && (penaltyPercent.toIntOrNull() ?: 0) > 0
        ) {
            Text("Next")
        }
    }
}

@Composable
private fun StepCommitments(
    commitments: List<SetupCommitment>,
    onAdd: (SetupCommitment) -> Unit,
    onRemove: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var points by remember { mutableStateOf("") }
    var isBinary by remember { mutableStateOf(true) }
    var maxCount by remember { mutableStateOf("3") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Add Commitments", style = MaterialTheme.typography.headlineSmall)
        Text("At least one required", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Commitment name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = points,
            onValueChange = { if (it.all { c -> c.isDigit() }) points = it },
            label = { Text("Point value") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Type: ")
            FilterChip(
                selected = isBinary,
                onClick = { isBinary = true },
                label = { Text("Binary") }
            )
            Spacer(Modifier.width(8.dp))
            FilterChip(
                selected = !isBinary,
                onClick = { isBinary = false },
                label = { Text("Count-based") }
            )
        }

        if (!isBinary) {
            OutlinedTextField(
                value = maxCount,
                onValueChange = { if (it.all { c -> c.isDigit() }) maxCount = it },
                label = { Text("Max count per day") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        Button(
            onClick = {
                val pv = points.toIntOrNull() ?: return@Button
                if (name.isBlank() || pv <= 0) return@Button
                val mc = if (isBinary) 1 else (maxCount.toIntOrNull() ?: 1).coerceAtLeast(1)
                onAdd(SetupCommitment(name, pv, if (isBinary) CommitmentType.BINARY else CommitmentType.COUNT, mc))
                name = ""
                points = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Commitment")
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(commitments) { idx, c ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(c.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                buildString {
                                    append("+${c.pointValue} pts")
                                    if (c.type == CommitmentType.COUNT) append(" x${c.maxCount}")
                                    append(" (${c.type.name})")
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = { onRemove(idx) }) { Text("Remove") }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = commitments.isNotEmpty()
            ) { Text("Next") }
        }
    }
}

@Composable
private fun StepTemptations(
    temptations: List<SetupTemptation>,
    onAdd: (SetupTemptation) -> Unit,
    onRemove: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var threshold by remember { mutableStateOf("") }
    var penalty by remember { mutableStateOf("") }

    Column(modifier = Modifier.fillMaxSize()) {
        Text("Add Temptations", style = MaterialTheme.typography.headlineSmall)
        Text("At least one required", style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Temptation name") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = threshold,
            onValueChange = { if (it.all { c -> c.isDigit() }) threshold = it },
            label = { Text("Unlock threshold (score needed)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = penalty,
            onValueChange = { if (it.all { c -> c.isDigit() }) penalty = it },
            label = { Text("Slip penalty (points lost)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Button(
            onClick = {
                val t = threshold.toIntOrNull() ?: return@Button
                val p = penalty.toIntOrNull() ?: return@Button
                if (name.isBlank() || t < 0 || p <= 0) return@Button
                onAdd(SetupTemptation(name, t, p))
                name = ""
                threshold = ""
                penalty = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Add Temptation")
        }

        Spacer(Modifier.height(8.dp))

        LazyColumn(modifier = Modifier.weight(1f)) {
            itemsIndexed(temptations) { idx, t ->
                Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(t.name, style = MaterialTheme.typography.bodyLarge)
                            Text(
                                "Unlocks at ${t.threshold} | Penalty: ${t.penalty}",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = { onRemove(idx) }) { Text("Remove") }
                    }
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
            Button(
                onClick = onNext,
                modifier = Modifier.weight(1f),
                enabled = temptations.isNotEmpty()
            ) { Text("Next") }
        }
    }
}

@Composable
private fun StepDayClose(
    hour: Int,
    minute: Int,
    onHourChange: (Int) -> Unit,
    onMinuteChange: (Int) -> Unit,
    onNext: () -> Unit,
    onBack: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Set Day-Close Time", style = MaterialTheme.typography.headlineSmall)
        Text("When does your day end? Daily points will be committed at this time.")

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            OutlinedTextField(
                value = hour.toString(),
                onValueChange = {
                    val h = it.toIntOrNull() ?: return@OutlinedTextField
                    if (h in 0..23) onHourChange(h)
                },
                label = { Text("Hour (0-23)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
            Text(":", style = MaterialTheme.typography.headlineMedium)
            OutlinedTextField(
                value = minute.toString(),
                onValueChange = {
                    val m = it.toIntOrNull() ?: return@OutlinedTextField
                    if (m in 0..59) onMinuteChange(m)
                },
                label = { Text("Minute (0-59)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.weight(1f),
                singleLine = true
            )
        }

        Text("Day closes at ${String.format("%02d:%02d", hour, minute)}", style = MaterialTheme.typography.bodyLarge)

        Spacer(Modifier.weight(1f))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
            Button(onClick = onNext, modifier = Modifier.weight(1f)) { Text("Next") }
        }
    }
}

@Composable
private fun StepReview(
    addictionName: String,
    penaltyPercent: Int,
    commitments: List<SetupCommitment>,
    temptations: List<SetupTemptation>,
    dayCloseHour: Int,
    dayCloseMinute: Int,
    onStart: () -> Unit,
    onBack: () -> Unit
) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        item {
            Text("Review & Start", style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.height(16.dp))

            Text("Addiction", style = MaterialTheme.typography.titleMedium)
            Text("$addictionName (${penaltyPercent}% penalty)")
            Spacer(Modifier.height(12.dp))

            Text("Day-Close Time", style = MaterialTheme.typography.titleMedium)
            Text(String.format("%02d:%02d", dayCloseHour, dayCloseMinute))
            Spacer(Modifier.height(12.dp))

            Text("Commitments (${commitments.size})", style = MaterialTheme.typography.titleMedium)
        }
        itemsIndexed(commitments) { _, c ->
            Text("  - ${c.name}: +${c.pointValue} pts (${c.type.name}${if (c.type == CommitmentType.COUNT) ", max ${c.maxCount}" else ""})")
        }
        item {
            Spacer(Modifier.height(12.dp))
            Text("Temptations (${temptations.size})", style = MaterialTheme.typography.titleMedium)
        }
        itemsIndexed(temptations) { _, t ->
            Text("  - ${t.name}: unlocks at ${t.threshold}, penalty ${t.penalty}")
        }
        item {
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedButton(onClick = onBack, modifier = Modifier.weight(1f)) { Text("Back") }
                Button(onClick = onStart, modifier = Modifier.weight(1f)) { Text("Start Challenge") }
            }
        }
    }
}
