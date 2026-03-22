package com.example.loophabittracker.presentation.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                for (i in 6 downTo 0) {
                    val date = currentDate.minusDays(i.toLong())
                    val dayStr = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()).uppercase()
                    val dateNumStr = date.dayOfMonth.toString()
                    
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.width(42.dp).padding(end = 4.dp)
                    ) {
                        Text(
                            text = dayStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = dateNumStr,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            LazyColumn {
                items(habitsWithRecords) { item ->
                    HabitCard(
                        model = item,
                        onToggle = { daysAgo -> 
                            if (item.habit.isMeasurable) {
                                showInputDialog = Triple(item.habit.id, daysAgo, item.habit)
                            } else {
                                viewModel.toggleHabit(item.habit.id, daysAgo) 
                            }
                        },
                        onClick = { onNavigateToStatistics(item.habit.id) }
                    )
                }
            }
        }
    }

    if (showInputDialog != null) {
        val (habitId, daysAgo, habit) = showInputDialog!!
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
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(vertical = 12.dp, horizontal = 8.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(model.habit.color))
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = model.habit.name,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(model.habit.color),
                modifier = Modifier.weight(1f)
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
                        if (model.habit.isMeasurable) {
                            if (record != null && record.value > 0f) {
                                val metTarget = record.value >= model.habit.target
                                val textColor = if (metTarget) Color(model.habit.color) else MaterialTheme.colorScheme.onSurfaceVariant
                                val weight = if (metTarget) FontWeight.Bold else FontWeight.Normal
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    val displayVal = if (record.value % 1.0f == 0f) record.value.toInt().toString() else record.value.toString()
                                    Text(text = displayVal, fontSize = 12.sp, color = textColor, fontWeight = weight)
                                    Text(text = model.habit.unit, fontSize = 8.sp, color = textColor, fontWeight = weight)
                                }
                            } else {
                                Text(text = "0\n${model.habit.unit}", fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f), textAlign = TextAlign.Center, lineHeight = 10.sp)
                            }
                        } else {
                            if (record != null && record.isCompleted) {
                                Icon(
                                    Icons.Default.Check,
                                    contentDescription = null,
                                    tint = Color(model.habit.color),
                                    modifier = Modifier.size(20.dp)
                                )
                            } else if (record != null && !record.isCompleted) {
                                Icon(
                                    Icons.Default.Close,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha=0.5f),
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                            // If record is null, show nothing.
                        }
                    }
                }
            }
        }
    }
}
