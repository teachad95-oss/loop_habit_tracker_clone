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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Loop Habit Tracker") })
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddHabit) {
                Icon(Icons.Default.Add, contentDescription = "Add Habit")
            }
        }
    ) { padding ->
        Column(modifier = Modifier.padding(padding).fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.End
            ) {
                for (i in 6 downTo 0) {
                    val date = currentDate.minusDays(i.toLong())
                    Text(
                        text = date.dayOfWeek.getDisplayName(TextStyle.NARROW, Locale.getDefault()),
                        modifier = Modifier.width(32.dp).padding(end = 4.dp),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            LazyColumn {
                items(habitsWithRecords) { item ->
                    HabitCard(
                        model = item,
                        onToggle = { daysAgo -> viewModel.toggleHabit(item.habit.id, daysAgo) },
                        onClick = { onNavigateToStatistics(item.habit.id) }
                    )
                }
            }
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
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(16.dp)
                    .clip(CircleShape)
                    .background(Color(model.habit.color))
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = model.habit.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )

            Row(horizontalArrangement = Arrangement.End) {
                model.recentRecords.forEachIndexed { index, isCompleted ->
                    val daysAgo = 6 - index
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(
                                if (isCompleted) Color(model.habit.color)
                                else MaterialTheme.colorScheme.surfaceVariant
                            )
                            .clickable { onToggle(daysAgo) }
                    ) {
                        if (isCompleted) {
                            Icon(
                                Icons.Default.Check,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(16.dp).align(Alignment.Center)
                            )
                        }
                    }
                }
            }
        }
    }
}
