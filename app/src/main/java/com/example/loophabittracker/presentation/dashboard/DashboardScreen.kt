package com.example.loophabittracker.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.example.loophabittracker.domain.model.Habit
import java.time.format.TextStyle
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel = hiltViewModel(),
    onNavigateToAddHabit: () -> Unit,
    onNavigateToStatistics: (Int) -> Unit
) {
    val habitsWithRecords by viewModel.habitsWithRecords.collectAsState()
    val dailySummaries by viewModel.dailySummaries.collectAsState()
    val currentDate by viewModel.currentDate.collectAsState()

    var showInputDialog by remember { mutableStateOf<Triple<Int, Int, Habit>?>(null) } // habitId, daysAgo, habit

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Habits") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddHabit) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                horizontalArrangement = Arrangement.End
            ) {
                for (i in 6 downTo 0) {
                    val date = currentDate.minusDays(i.toLong())
                    val dayStr = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
                    val dateNumStr = date.dayOfMonth.toString()
                    val summary = dailySummaries.getOrNull(6 - i)
                    val missed = summary?.missedCount ?: 0
                    val penaltySum = summary?.totalPenalty ?: 0f
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(42.dp).padding(end = 4.dp)
                    ) {
                        Text(text = "$missed Missed", fontSize = 7.sp, color = if(missed > 0) Color.Red else Color.Gray)
                        Text(text = "-$penaltySum", fontSize = 7.sp, color = if(penaltySum > 0f) Color.Red else Color.Gray)
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = dayStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                        Text(
                            text = dateNumStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontSize = 9.sp
                        )
                    }
                }
            }

            LazyColumn {
                items(habitsWithRecords) { item ->
                    HabitCard(
                        model = item,
                        onToggle = { daysAgo -> 
                            showInputDialog = Triple(item.habit.id, daysAgo, item.habit)
                        },
                        onClick = { onNavigateToStatistics(item.habit.id) }
                    )
                }
            }
        }
    }

    if (showInputDialog != null) {
        val (habitId, daysAgo, habit) = showInputDialog!!
        
        if (habit.isMeasurable) {
            var inputValue by remember { mutableStateOf("") }
            AlertDialog(
                onDismissRequest = { showInputDialog = null },
                title = { Text("Log ${habit.name}") },
                text = {
                    OutlinedTextField(
                        value = inputValue,
                        onValueChange = { inputValue = it },
                        label = { Text("Amount (${habit.unit})") }
                    )
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val floatVal = inputValue.toFloatOrNull() ?: 0f
                            viewModel.updateHabitValue(habitId, daysAgo, floatVal)
                            showInputDialog = null
                        }
                    ) { Text("Save") }
                },
                dismissButton = {
                    TextButton(onClick = { showInputDialog = null }) { Text("Cancel") }
                }
            )
        } else {
            AlertDialog(
                onDismissRequest = { showInputDialog = null },
                title = { Text("Log ${habit.name}") },
                text = { Text("Did you complete this habit on this day?") },
                confirmButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateHabitValue(habitId, daysAgo, 1f) // 1f = Yes
                            showInputDialog = null
                        }
                    ) { Text("Yes") }
                },
                dismissButton = {
                    TextButton(
                        onClick = {
                            viewModel.updateHabitValue(habitId, daysAgo, 0f) // 0f = No
                            showInputDialog = null
                        }
                    ) { Text("No") }
                }
            )
        }
    }
}

@Composable
fun HabitCard(
    model: HabitUiModel,
    onToggle: (Int) -> Unit,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 0.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .clip(CircleShape)
                    .background(Color(model.habit.color))
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = model.habit.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(model.habit.color),
                modifier = Modifier.weight(1f),
                fontSize = 12.sp
            )

            Row(horizontalArrangement = Arrangement.End) {
                model.recentRecords.forEachIndexed { index, record ->
                    val daysAgo = 6 - index
                    Box(
                        modifier = Modifier
                            .width(42.dp)
                            .height(42.dp)
                            .padding(end = 4.dp)
                            .clickable { onToggle(daysAgo) },
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            if (model.habit.isMeasurable) {
                                if (record != null && record.value > 0f) {
                                    val metTarget = record.value >= model.habit.target
                                    val textColor = if (metTarget) Color(model.habit.color) else MaterialTheme.colorScheme.onSurfaceVariant
                                    val weight = if (metTarget) FontWeight.Bold else FontWeight.Normal
                                    
                                    val displayVal = if (record.value % 1.0f == 0f) record.value.toInt().toString() else record.value.toString()
                                    Text(text = displayVal, fontSize = 10.sp, color = textColor, fontWeight = weight)
                                    Text(text = model.habit.unit, fontSize = 7.sp, color = textColor, fontWeight = weight)
                                } else {
                                    Text(text = "-\n${model.habit.unit}", fontSize = 9.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f), textAlign = TextAlign.Center, lineHeight = 9.sp)
                                }
                            } else {
                                if (record != null && record.isCompleted) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(model.habit.color),
                                        modifier = Modifier.size(16.dp)
                                    )
                                } else if (record != null && !record.isCompleted) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f),
                                        modifier = Modifier.size(14.dp)
                                    )
                                } else {
                                    Text("-", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f))
                                }
                            }
                            
                            // Penalty logic
                            val isMissedRecord = record != null && !record.isCompleted
                            val penaltyAmount = if (isMissedRecord) model.habit.penalty else 0f
                            
                            if (penaltyAmount > 0f) {
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(text = "-$penaltyAmount", color = Color.Red, fontSize = 7.sp)
                            } else if (record != null) {
                                Spacer(modifier = Modifier.height(1.dp))
                                Text(text = "0", color = Color.Gray, fontSize = 7.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}
