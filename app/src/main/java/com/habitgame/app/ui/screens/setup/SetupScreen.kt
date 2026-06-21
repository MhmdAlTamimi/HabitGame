package com.habitgame.app.ui.screens.setup

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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
                title = {
                    Text("New Challenge", fontWeight = FontWeight.Bold)
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            StepIndicator(currentStep = step, totalSteps = 5)

            Column(
                modifier = Modifier
                    .fillMaxSize()
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
                                commitments = commitmentsList.map { ch ->
                                    ch.name to Triple(ch.pointValue, ch.type, ch.maxCount)
                                },
                                temptations = temptationsList.map { t ->
                                    Triple(t.name, t.threshold, t.penalty)
                                },
                                onComplete = onChallengeCreated
                            )
                        },
                        onBack = { step = 3 }
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(currentStep: Int, totalSteps: Int) {
    val stepLabels = listOf("Addiction", "Commitments", "Temptations", "Schedule", "Review")
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        MaterialTheme.colorScheme.primary,
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.85f)
                    )
                )
            )
            .padding(horizontal = 16.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (i in 0 until totalSteps) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Surface(
                    shape = CircleShape,
                    color = when {
                        i < currentStep -> Color(0xFF00C853)
                        i == currentStep -> Color.White
                        else -> Color.White.copy(alpha = 0.3f)
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text(
                            "${i + 1}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = when {
                                i < currentStep -> Color.White
                                i == currentStep -> MaterialTheme.colorScheme.primary
                                else -> Color.White
                            }
                        )
                    }
                }
                Spacer(Modifier.height(4.dp))
                Text(
                    stepLabels[i],
                    fontSize = 10.sp,
                    color = if (i <= currentStep) Color.White else Color.White.copy(alpha = 0.5f),
                    textAlign = TextAlign.Center
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
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Name your Addiction",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "What habit are you fighting?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = addictionName,
                onValueChange = onNameChange,
                label = { Text("Addiction name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                "Set Penalty Percentage",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "How much of your total score is lost per addiction slip?",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            OutlinedTextField(
                value = penaltyPercent,
                onValueChange = { if (it.all { ch -> ch.isDigit() }) onPercentChange(it) },
                label = { Text("Penalty % (e.g. 10)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(12.dp)
            )
        }
        Button(
            onClick = onNext,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            enabled = addictionName.isNotBlank() && (penaltyPercent.toIntOrNull() ?: 0) > 0,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Next", fontWeight = FontWeight.Bold)
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
        Text(
            "Add Commitments",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Daily tasks you commit to. At least one required.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Commitment name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = points,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) points = it },
                    label = { Text("Point value") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Type:", style = MaterialTheme.typography.bodyMedium)
                    FilterChip(
                        selected = isBinary,
                        onClick = { isBinary = true },
                        label = { Text("Binary") },
                        shape = RoundedCornerShape(8.dp)
                    )
                    FilterChip(
                        selected = !isBinary,
                        onClick = { isBinary = false },
                        label = { Text("Count-based") },
                        shape = RoundedCornerShape(8.dp)
                    )
                }

                if (!isBinary) {
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = maxCount,
                        onValueChange = { if (it.all { ch -> ch.isDigit() }) maxCount = it },
                        label = { Text("Max count per day") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = RoundedCornerShape(12.dp)
                    )
                }

                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        val pv = points.toIntOrNull() ?: return@FilledTonalButton
                        if (name.isBlank() || pv <= 0) return@FilledTonalButton
                        val mc = if (isBinary) 1 else (maxCount.toIntOrNull() ?: 1).coerceAtLeast(1)
                        onAdd(SetupCommitment(name, pv, if (isBinary) CommitmentType.BINARY else CommitmentType.COUNT, mc))
                        name = ""
                        points = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Commitment")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(commitments) { idx, c ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(c.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(
                                buildString {
                                    append("+${c.pointValue} pts")
                                    if (c.type == CommitmentType.COUNT) append(" x${c.maxCount}")
                                    append(" (${c.type.name})")
                                },
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onRemove(idx) }) {
                            Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        NavigationButtons(
            onBack = onBack,
            onNext = onNext,
            nextEnabled = commitments.isNotEmpty()
        )
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
        Text(
            "Add Temptations",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            "Things you resist. They unlock at a score threshold. At least one required.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(Modifier.height(12.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Temptation name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = threshold,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) threshold = it },
                    label = { Text("Unlock threshold (score needed)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = penalty,
                    onValueChange = { if (it.all { ch -> ch.isDigit() }) penalty = it },
                    label = { Text("Slip penalty (points lost)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp)
                )
                Spacer(Modifier.height(12.dp))
                FilledTonalButton(
                    onClick = {
                        val t = threshold.toIntOrNull() ?: return@FilledTonalButton
                        val p = penalty.toIntOrNull() ?: return@FilledTonalButton
                        if (name.isBlank() || t < 0 || p <= 0) return@FilledTonalButton
                        onAdd(SetupTemptation(name, t, p))
                        name = ""
                        threshold = ""
                        penalty = ""
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("Add Temptation")
                }
            }
        }

        Spacer(Modifier.height(12.dp))

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            itemsIndexed(temptations) { idx, t ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFFFF3E0))
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(t.name, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                            Text(
                                "Unlocks at ${t.threshold} | Penalty: ${t.penalty}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = { onRemove(idx) }) {
                            Icon(Icons.Default.Close, "Remove", tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(8.dp))
        NavigationButtons(
            onBack = onBack,
            onNext = onNext,
            nextEnabled = temptations.isNotEmpty()
        )
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
    Column(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                "Set Day-Close Time",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                "When does your day end? Daily points will be committed at this time.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
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
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                        Text(":", style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        OutlinedTextField(
                            value = minute.toString(),
                            onValueChange = {
                                val m = it.toIntOrNull() ?: return@OutlinedTextField
                                if (m in 0..59) onMinuteChange(m)
                            },
                            label = { Text("Minute (0-59)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            shape = RoundedCornerShape(12.dp)
                        )
                    }
                    Spacer(Modifier.height(16.dp))
                    Surface(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Text(
                            "Day closes at ${String.format("%02d:%02d", hour, minute)}",
                            modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }

        NavigationButtons(onBack = onBack, onNext = onNext, nextEnabled = true)
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
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    "Review & Start",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    "Make sure everything looks right before starting.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(4.dp))
            }

            item {
                ReviewCard(title = "Addiction") {
                    Text(
                        "$addictionName — ${penaltyPercent}% penalty per slip",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                ReviewCard(title = "Day-Close Time") {
                    Text(
                        String.format("%02d:%02d", dayCloseHour, dayCloseMinute),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }

            item {
                ReviewCard(title = "Commitments (${commitments.size})") {
                    commitments.forEach { c ->
                        Text(
                            "  - ${c.name}: +${c.pointValue} pts (${c.type.name}${if (c.type == CommitmentType.COUNT) ", max ${c.maxCount}" else ""})",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }

            item {
                ReviewCard(title = "Temptations (${temptations.size})") {
                    temptations.forEach { t ->
                        Text(
                            "  - ${t.name}: unlocks at ${t.threshold}, penalty ${t.penalty}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Back", fontWeight = FontWeight.Bold)
            }
            Button(
                onClick = onStart,
                modifier = Modifier.weight(1f).height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF00C853))
            ) {
                Text("Start Challenge", fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
    }
}

@Composable
private fun ReviewCard(title: String, content: @Composable () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.height(8.dp))
            content()
        }
    }
}

@Composable
private fun NavigationButtons(
    onBack: () -> Unit,
    onNext: () -> Unit,
    nextEnabled: Boolean
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.weight(1f).height(52.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Back", fontWeight = FontWeight.Bold)
        }
        Button(
            onClick = onNext,
            modifier = Modifier.weight(1f).height(52.dp),
            enabled = nextEnabled,
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Next", fontWeight = FontWeight.Bold)
        }
    }
}
